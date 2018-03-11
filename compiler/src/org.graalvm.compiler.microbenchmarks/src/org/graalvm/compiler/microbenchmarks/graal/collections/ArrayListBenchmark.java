/*
 * Copyright (c) 2017, 2017, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.microbenchmarks.graal.collections;

import java.util.ArrayList;

import org.graalvm.compiler.microbenchmarks.graal.GraalBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmarks cost of ArrayList.
 */
public class ArrayListBenchmark extends GraalBenchmark {

    private static final int N = 100;

    @State(Scope.Benchmark)
    public static class ThreadState {
        final ArrayList<Integer> list = new ArrayList<>(N);
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addBoxedAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        state.list.clear();
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addRemoveBoxedAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        for (int i = 0; i < N; ++i) {
            state.list.remove(new Integer(i));
        }
        state.list.clear();
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addNullAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(null);
        }
        state.list.clear();
    }

    @State(Scope.Benchmark)
    public static class ClearedThreadState {
        final ArrayList<Integer> list = new ArrayList<>(N);

        // We don't want to measure the cost of list clearing
        @Setup(Level.Invocation)
        public void beforeInvocation() {
            list.clear();
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addNull(ClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(null);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addRemoveBoxed(ClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }

        for (int i = 0; i < N; ++i) {
            state.list.remove(new Integer(i));
        }
    }
}
