package org.graalvm.compiler.microbenchmarks.graal.collections;

import java.util.concurrent.TimeUnit;

import org.graalvm.collections.list.ArrayListClone;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@SuppressWarnings("rawtypes")
public class TypeProfileWidthBenchmark {

    @State(Scope.Thread)
    public static class Context {

        ArrayListClone list;

        @Setup(Level.Invocation)
        public void setup() {
            list = new ArrayListClone<>();
        }

        @TearDown(Level.Invocation)
        public void teardown() {
            list = null;
        }

    }

    static class Z1 {

    }

    static class Z2 {

    }

    static class A {
        static long id;

        final long ID = id++;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof A && ((A) obj).ID == this.ID;
        }
    }

    static class B {
        static long id;

        final long ID = id++;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof B && ((B) obj).ID == this.ID;
        }
    }

    @Benchmark
    @Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    // use community for basic inliner
    @Fork(value = 1, jvmArgsAppend = {"-XX:TypeProfileWidth=2", "-Dgraal.CompilerConfiguration=community"})
    public int width2(Context c) {
        ArrayListClone a = c.list;
        int cc = 0;
        // register 3 different types in the profile
        Z1 zz1 = new Z1();
        Z2 zz2 = new Z2();
        A aa = new A();
        B bb = new B();
        a.add(zz1);
        a.add(zz2);
        a.add(aa);
        a.add(bb);
        cc += a.contains(zz1) ? 1 : 0;
        cc += a.contains(zz2) ? 1 : 0;
        cc += a.contains(aa) ? 1 : 0;
        cc += a.contains(bb) ? 1 : 0;
        for (int i = 0; i < 100; i++) {
            cc += bench(a, zz2);
            cc += bench(a, zz1);
            cc += bench(a, aa);
            cc += bench(a, bb);
        }
        return cc;
    }

    static int bench(ArrayListClone a, Object o) {
        int contained = 0;
        for (int i = 0; i < 1000_000; i++) {
            // here we can inline contains -> index of -> equals(only if we profile 4 types,
            // else this will stay a call as we only profiled 2 of our 4 types)
            if (a.contains(o)) {
                contained++;
            }
        }
        return contained;
    }

    @Benchmark
    @Warmup(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
    // use community for basic inliner
    @Fork(value = 1, jvmArgsAppend = {"-XX:TypeProfileWidth=4", "-Dgraal.CompilerConfiguration=community"})
    public int width4(Context c) {
        ArrayListClone a = c.list;
        int cc = 0;
        // register 3 different types in the profile
        Z1 zz1 = new Z1();
        Z2 zz2 = new Z2();
        A aa = new A();
        B bb = new B();
        a.add(zz1);
        a.add(zz2);
        a.add(aa);
        a.add(bb);
        cc += a.contains(zz1) ? 1 : 0;
        cc += a.contains(zz2) ? 1 : 0;
        cc += a.contains(aa) ? 1 : 0;
        cc += a.contains(bb) ? 1 : 0;
        for (int i = 0; i < 100; i++) {
            cc += bench(a, zz2);
            cc += bench(a, zz1);
            cc += bench(a, aa);
            cc += bench(a, bb);
        }
        return cc;
    }

}
