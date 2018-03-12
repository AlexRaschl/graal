package org.graalvm.compiler.microbenchmarks.graal.collections;

import org.graalvm.collections.list.SpecifiedArrayListImpl;
import org.graalvm.collections.list.statistics.StatisticalSpecifiedArrayListImpl;
import org.graalvm.collections.list.statistics.Statistics;
import org.graalvm.compiler.microbenchmarks.graal.GraalBenchmark;
import org.junit.AfterClass;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

public class SpecifiedArrayListBenchmark extends GraalBenchmark {

    private static final int N = 100;

    @State(Scope.Benchmark)
    public static class ThreadState {
        final SpecifiedArrayListImpl<Integer> list = new StatisticalSpecifiedArrayListImpl<>(N);
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
    public void addNullAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(null);
        }
        state.list.clear();
    }

    @State(Scope.Benchmark)
    public static class ClearedThreadState {
        final SpecifiedArrayListImpl<Integer> list = new StatisticalSpecifiedArrayListImpl<>(N);

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

    @State(Scope.Benchmark)
    public static class AddedClearedThreadState {
        final SpecifiedArrayListImpl<Integer> list = new StatisticalSpecifiedArrayListImpl<>(N);
        final Integer[] integers = new Integer[N];

        // We don't want to measure the cost of list clearing
        @Setup(Level.Invocation)
        public void beforeInvocation() {
            list.clear();
            Integer curr;
            for (int i = 0; i < N; ++i) {
                curr = new Integer(i);
                list.add(i);
                integers[i] = curr;
            }
        }

        @TearDown(Level.Trial)
        public void teardown() {
            Statistics.printOverallSummary();
        }

    }

    @Benchmark
    @Warmup(iterations = 20)
    public void IndexOf(AddedClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.indexOf(state.integers[i]);
        }

        state.list.clear();
    }

}

// @Benchmark
// @Warmup(iterations = 20)
// public void addRemoveBoxedAndClear(ThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(i);
// }
// for (int i = 0; i < N; ++i) {
// state.list.remove(new Integer(i));
// }
// state.list.clear();
// }
//
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
// public void addRemoveBoxed(ClearedThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.add(i);
// }
// for (int i = 0; i < N; ++i) {
// state.list.remove(new Integer(i));
// }
// }
//
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
