package org.graalvm.compiler.microbenchmarks.graal.collections;

import java.io.BufferedReader;
import java.sql.Time;
import java.util.ArrayList;
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
    private static final int N = 100;

    @State(Scope.Benchmark)
    public static class ThreadState {
        final ArrayList<Integer> list = new ArrayList<>();
        final SpecifiedArrayList<Integer> sar = new SpecifiedArrayList<>();
        final Object[] randomObjects = {
                        new Integer(10),
                        new Double(1),
                        new HashMap<Object, String>(),
                        new Object(),
                        new String("foo"),
                        new BufferedReader(null),
                        new Boolean(true)
        };
        final Object[] intObjects = {
                        new Integer(100000),
                        new Integer(232100),
                        new Integer(123500),
                        new Integer(146123),
                        new Integer(112636),
                        new Integer(135412),
                        new Integer(123512)
        };
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void containsPolluted(ThreadState state) {
        for (Object o : state.randomObjects) {
            state.list.contains(o);
        }
    }

    @Benchmark
    @Warmup(iterations = 20)
    public void containsMonomorphic(ThreadState state) {
        for (Object o : state.intObjects) {
            state.list.contains(o);
        }
    }

// @State(Scope.Benchmark)
// public static class AddedClearedThreadState {
// final ArrayList<Integer> list = new ArrayList<>();
// final Integer[] integers = new Integer[N];
//
// // We don't want to measure the cost of list clearing
// @Setup(Level.Invocation)
// public void beforeInvocation() {
// list.clear();
// Integer curr;
// for (int i = 0; i < N; ++i) {
// curr = new Integer(i);
// list.add(i);
// integers[i] = curr;
// }
// }
// }
//
// @Benchmark
// @Warmup(iterations = 20)
// public void IndexOf(AddedClearedThreadState state) {
// for (int i = 0; i < N; ++i) {
// state.list.indexOf(state.integers[i]);
// }
// }

}
