package org.graalvm.compiler.microbenchmarks.graal.collections;

import java.util.Iterator;
import java.util.ListIterator;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.primitives.SimpleIntSpecifiedArrayList;
import org.graalvm.collections.list.statistics.Statistics;
import org.graalvm.compiler.microbenchmarks.graal.GraalBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

public class SimpleIntSpecifiedArrayListBenchmark extends GraalBenchmark {

    private static final int N = 1000;

    @State(Scope.Benchmark)
    public static class ThreadState {
        final SimpleIntSpecifiedArrayList list = new SimpleIntSpecifiedArrayList();

        @TearDown(Level.Trial)
        public void teardown() {
            Statistics.printOverallSummary();
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void addBoxedAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        state.list.clear();
    }

// @Benchmark
// @Warmup(iterations = 20)
// public void addNullAndClear(ThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(null);
// }
// state.list.clear();
// }

    @Benchmark
    @Warmup(iterations = 20)
    public void addRemoveBoxedAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        for (int i = 0; i < N; ++i) {
            state.list.remove(i);
        }
        state.list.clear();
    }

    @State(Scope.Benchmark)
    public static class ClearedThreadState {
        final SimpleIntSpecifiedArrayList list = new SimpleIntSpecifiedArrayList();

        // We don't want to measure the cost of list clearing
        @Setup(Level.Invocation)
        public void beforeInvocation() {
            list.clear();
        }

        @TearDown(Level.Trial)
        public void teardown() {
            Statistics.printOverallSummary();
        }
    }

// @Benchmark
// @Warmup(iterations = 20)
// public void addNull(ClearedThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(null);
// }
// }

    @State(Scope.Benchmark)
    public static class AddedClearedThreadState {
        final SimpleIntSpecifiedArrayList list = new SimpleIntSpecifiedArrayList();
        final Integer[] integers = new Integer[N];

        // We don't want to measure the cost of list clearing
        @Setup(Level.Invocation)
        public void beforeInvocation() {
            list.clear();

            for (int i = 0; i < N; ++i) {
                list.add(i);
                integers[i] = i;
            }
        }

        @TearDown(Level.Trial)
        public void teardown() {

        }

    }

    @Benchmark
    @Warmup(iterations = 20)
    public void IndexOf(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.indexOf(state.integers[i]);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void removeObj(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) { // Slow because SAR is not optimized for removals at end
            state.list.remove(i);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void containsObj(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.contains(i);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void getObj(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.get(i);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void setAt(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.set(i, i - 1);
        }
    }

// @Benchmark
// @Warmup(iterations = 20)
// public void iteratorUsage(AddedClearedThreadState state) {
// Iterator<Integer> itr = state.list.iterator();
// while (itr.hasNext())
// itr.next();
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void listIteratorUsage(AddedClearedThreadState state) {
// ListIterator<Integer> itr = state.list.listIterator();
// while (itr.hasNext())
// itr.next();
// }

// @Benchmark
// @Warmup(iterations = 20)
// public void addRemoveBoxed(ClearedThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(i);
// }
// for (int i = 0; i < N; ++i) {
// state.list.remove(new Integer(i));
// }
// }

// @Benchmark
// @Warmup(iterations = 20)
// public void addIndexOfAndClear(ThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(i);
// }
// for (int i = 0; i < N; ++i) {
// state.list.indexOf(new Integer(i));
// }
// state.list.clear();
// }

// @Benchmark
// @Warmup(iterations = 20)
// public void addIndexOf(ClearedThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(i);
// }
// for (int i = 0; i < N; ++i) {
// state.list.indexOf(new Integer(i));
// }
// }
}
