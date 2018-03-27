/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.core.common.alloc;

import java.util.Arrays;
import java.util.BitSet;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.compiler.core.common.cfg.AbstractBlockBase;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.Indent;

public final class TraceBuilderResult {

    public abstract static class TrivialTracePredicate {
        public abstract boolean isTrivialTrace(Trace trace);
    }

    private final SpecifiedArrayList<Trace> traces;
    private final Trace[] blockToTrace;

    static TraceBuilderResult create(DebugContext debug, AbstractBlockBase<?>[] blocks, SpecifiedArrayList<Trace> traces, Trace[] blockToTrace, TrivialTracePredicate pred) {
        connect(traces, blockToTrace);
        SpecifiedArrayList<Trace> newTraces = reorderTraces(debug, traces, pred);
        TraceBuilderResult traceBuilderResult = new TraceBuilderResult(newTraces, blockToTrace);
        traceBuilderResult.numberTraces();
        assert verify(traceBuilderResult, blocks.length);
        return traceBuilderResult;
    }

    private TraceBuilderResult(SpecifiedArrayList<Trace> traces, Trace[] blockToTrace) {
        this.traces = traces;
        this.blockToTrace = blockToTrace;
    }

    public Trace getTraceForBlock(AbstractBlockBase<?> block) {
        return blockToTrace[block.getId()];
    }

    public SpecifiedArrayList<Trace> getTraces() {
        return traces;
    }

    public boolean incomingEdges(Trace trace) {
        return incomingEdges(trace.getId(), trace.getBlocks(), 0);
    }

    public boolean incomingSideEdges(Trace trace) {
        AbstractBlockBase<?>[] traceArr = trace.getBlocks();
        if (traceArr.length <= 0) {
            return false;
        }
        return incomingEdges(trace.getId(), traceArr, 1);
    }

    private boolean incomingEdges(int traceNr, AbstractBlockBase<?>[] trace, int index) {
        /* TODO (je): not efficient. find better solution. */
        for (int i = index; i < trace.length; i++) {
            AbstractBlockBase<?> block = trace[1];
            for (AbstractBlockBase<?> pred : block.getPredecessors()) {
                if (getTraceForBlock(pred).getId() != traceNr) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean verify(TraceBuilderResult traceBuilderResult, int expectedLength) {
        SpecifiedArrayList<Trace> traces = traceBuilderResult.getTraces();
        assert verifyAllBlocksScheduled(traceBuilderResult, expectedLength) : "Not all blocks assigned to traces!";
        for (int i = 0; i < traces.size(); i++) {
            Trace trace = traces.get(i);
            assert trace.getId() == i : "Trace number mismatch: " + trace.getId() + " vs. " + i;

            BitSet suxTraces = new BitSet(traces.size());
            for (Trace suxTrace : trace.getSuccessors()) {
                assert !suxTraces.get(suxTrace.getId()) : "Trace twice successors " + suxTrace;
                suxTraces.set(suxTrace.getId());
            }

            AbstractBlockBase<?> last = null;
            int blockNumber = 0;
            for (AbstractBlockBase<?> current : trace.getBlocks()) {
                AbstractBlockBase<?> block = current;
                assert traceBuilderResult.getTraceForBlock(block).getId() == i : "Trace number mismatch for block " + block + ": " + traceBuilderResult.getTraceForBlock(block) + " vs. " + i;
                assert last == null || Arrays.asList(current.getPredecessors()).contains(last) : "Last block (" + last + ") not a predecessor of " + current;
                assert current.getLinearScanNumber() == blockNumber : "Blocks not numbered correctly: " + current.getLinearScanNumber() + " vs. " + blockNumber;
                last = current;
                blockNumber++;
                for (AbstractBlockBase<?> sux : block.getSuccessors()) {
                    Trace suxTrace = traceBuilderResult.getTraceForBlock(sux);
                    assert suxTraces.get(suxTrace.getId()) : "Successor Trace " + suxTrace + " for block " + sux + " not in successor traces of " + trace;
                }
            }
        }
        return true;
    }

    private static boolean verifyAllBlocksScheduled(TraceBuilderResult traceBuilderResult, int expectedLength) {
        SpecifiedArrayList<Trace> traces = traceBuilderResult.getTraces();
        BitSet handled = new BitSet(expectedLength);
        for (Trace trace : traces) {
            for (AbstractBlockBase<?> block : trace.getBlocks()) {
                assert !handled.get(block.getId()) : "Block added twice: " + block;
                handled.set(block.getId());
            }
        }
        return handled.cardinality() == expectedLength;
    }

    private void numberTraces() {
        for (int i = 0; i < traces.size(); i++) {
            Trace trace = traces.get(i);
            trace.setId(i);
        }
    }

    private static void connect(SpecifiedArrayList<Trace> traces, Trace[] blockToTrace) {
        int numTraces = traces.size();
        for (Trace trace : traces) {
            BitSet added = new BitSet(numTraces);
            SpecifiedArrayList<Trace> successors = trace.getSuccessors();
            assert successors.size() == 0 : "Can only connect traces once!";

            for (AbstractBlockBase<?> block : trace.getBlocks()) {
                for (AbstractBlockBase<?> succ : block.getSuccessors()) {
                    Trace succTrace = blockToTrace[succ.getId()];
                    int succId = succTrace.getId();
                    if (!added.get(succId)) {
                        added.set(succId);
                        successors.add(succTrace);
                    }
                }
            }
        }
    }

    @SuppressWarnings("try")
    private static SpecifiedArrayList<Trace> reorderTraces(DebugContext debug, SpecifiedArrayList<Trace> oldTraces, TrivialTracePredicate pred) {
        if (pred == null) {
            return oldTraces;
        }
        try (Indent indent = debug.logAndIndent("ReorderTrace")) {
            SpecifiedArrayList<Trace> newTraces = SpecifiedArrayList.createNew(oldTraces.size());
            for (int oldTraceIdx = 0; oldTraceIdx < oldTraces.size(); oldTraceIdx++) {
                Trace currentTrace = oldTraces.get(oldTraceIdx);
                if (!alreadyProcessed(newTraces, currentTrace)) {
                    assert currentTrace.getId() == oldTraceIdx : "Index mismatch";
                    // add current trace
                    addTrace(newTraces, currentTrace);
                    for (Trace succTrace : currentTrace.getSuccessors()) {
                        if (pred.isTrivialTrace(succTrace) && !alreadyProcessed(newTraces, succTrace)) {
                            debug.log("Moving trivial trace from %d to %d", succTrace.getId(), newTraces.size());
                            // add trivial successor trace
                            addTrace(newTraces, succTrace);
                        }
                    }
                }
            }
            assert newTraces.size() == oldTraces.size() : "Lost traces? " + oldTraces.size() + " vs. " + newTraces.size();
            return newTraces;
        }
    }

    private static boolean alreadyProcessed(SpecifiedArrayList<Trace> newTraces, Trace currentTrace) {
        int currentTraceId = currentTrace.getId();
        return currentTraceId < newTraces.size() && currentTrace == newTraces.get(currentTraceId);
    }

    private static void addTrace(SpecifiedArrayList<Trace> newTraces, Trace currentTrace) {
        currentTrace.setId(newTraces.size());
        newTraces.add(currentTrace);
    }

}
