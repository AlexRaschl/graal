package org.graalvm.compiler.microbenchmarks.graal.collections;

import java.io.BufferedReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.compiler.microbenchmarks.graal.GraalBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.prism.Image;

public class TypeProfileBenchmark extends GraalBenchmark {

    private static class A {
        String foo() {
            return "A";
        }
    }

    private static class B extends A {
        @Override
        String foo() {
            return "B";
        }
    }

    private static class C extends B {
        @Override
        String foo() {
            return "C";
        }
    }

    private static class D extends A {
        @Override
        String foo() {
            return "D";
        }
    }

    private static final int N = 100;

// @State(Scope.Benchmark)
// public static class ThreadState {
// A a = new A();
// B b = new B();
// C c = new C();
// D d = new D();
//
// A[] arr = new A[]{
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A(),
// new A()
// };
//
// final ArrayList<A> list = new ArrayList<>(Arrays.asList(arr));
// final SpecifiedArrayList<A> sar = new SpecifiedArrayList<>(Arrays.asList(arr));
//
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void containsPolluted(ThreadState state) {
// state.list.contains(new B());
// state.list.contains(new C());
// state.list.contains(new D());
// for (int i = 10; i < N; i++) {
// state.list.contains(new A());
// }
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void containsMonomorphic(ThreadState state) {
// for (int i = 0; i < N; i++) {
// state.list.contains(new A());
// }
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void containsPollutedSAR(ThreadState state) {
// state.sar.contains(new B());
// state.sar.contains(new C());
// state.sar.contains(new D());
// for (int i = 10; i < N; i++) {
// state.sar.contains(new A());
// }
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void containsMonomorphicSAR(ThreadState state) {
// for (int i = 0; i < N; i++) {
// state.sar.contains(new A());
// }
// }

    @State(Scope.Benchmark)
    public static class NonPollutedThreadState {
        final ArrayList<String> list = new ArrayList<>(N);

        @Setup(Level.Invocation)
        public void beforeInvocation() {
            for (int i = 0; i < N; i++) {
                list.add(Integer.toString(i));
            }
        }

        @Setup(Level.Invocation)
        public void afterInvocation() {
            list.clear();
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void IndexOf(NonPollutedThreadState state) {
        for (int i = 0; i < N; i++) {
            state.list.contains(Integer.toString(i));
        }
    }

    @State(Scope.Benchmark)
    public static class AddedPollutedThreadState {
        final ArrayList<String> list = new ArrayList<>(N);

        @Setup(Level.Invocation)
        public void beforeInvocation() {
            for (int i = 0; i < N - 1; i++) {
                list.add(Integer.toString(i));
            }
            list.add(null);

        }

        @Setup(Level.Invocation)
        public void afterInvocation() {
            list.clear();
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void IndexOfPolluted(AddedPollutedThreadState state) {
        for (int i = 0; i < N; i++) {
            state.list.contains(Integer.toString(i));
        }
    }

}
