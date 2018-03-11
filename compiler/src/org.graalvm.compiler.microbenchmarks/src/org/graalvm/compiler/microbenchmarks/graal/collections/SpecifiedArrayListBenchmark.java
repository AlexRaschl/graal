package org.graalvm.compiler.microbenchmarks.graal.collections;

import org.graalvm.collections.list.SpecifiedArrayListImpl;
import org.graalvm.compiler.microbenchmarks.graal.GraalBenchmark;
import org.graalvm.compiler.microbenchmarks.graal.collections.ArrayListBenchmark.ThreadState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

public class SpecifiedArrayListBenchmark extends GraalBenchmark {

    private static final int N = 100;

    @State(Scope.Benchmark)
    public static class ThreadState {
        final SpecifiedArrayListImpl<Integer> list = new SpecifiedArrayListImpl<>(N);
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
    public void addIndexOfAndClear(ThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        for (int i = 0; i < N; ++i) {
            state.list.indexOf(new Integer(i));
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
        final SpecifiedArrayListImpl<Integer> list = new SpecifiedArrayListImpl<>(N);

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

    @Benchmark
    @Warmup(iterations = 20)
    public void addIndexOf(ClearedThreadState state) {
        for (int i = 0; i < N; ++i) {
            state.list.add(i);
        }
        for (int i = 0; i < N; ++i) {
            state.list.indexOf(new Integer(i));
        }
    }
}
