package org.graalvm.collections.list.statistics;

public interface StatisticalSpecifiedArrayList<E> extends StatisticalCollection {
    // TODO DELETE

    // I Use this Interface to make small notes

    /**
     * I figured out that some percentage of the Lists generated in
     * "org.graalvm.compiler.core.gen.NodeLIRBuilder" never contain objects during their lifetime. This
     * could be simply replaced.
     *
     * Concerning that many Lists only grow to Sizes of 1 or 2 i will implement following strategy to be
     * more memory efficient than the ArrayList
     *
     * Copy STATIC References of Empty Array from ArrayList strategy.
     *
     * Start at first add with INITIAL_CAPACITY == 2; As soon as this Capacity is reached grow at one
     * Step to >=10 Then use Size*1.5 as growing Strategy
     *
     * Also try to use stuff like trimToSize as often as beneficial.
     *
     * Also use the approach of shrinking the capacity of the list when the list gets cleared to prevent
     * inefficient memory usage after clearing large lists
     *
     * Now concerning performance
     *
     * Do not use HashSet in Background since Index of is not very often atm.
     *
     * Do not optimize on insertions at end. add(Object o , int index)
     *
     *
     *
     * TODO package org.graalvm.compiler.lir.alloc.trace.lsra.TraceLinearScanWalker inefficient empty
     * list strategy.
     *
     * TODO org.graalvm.compiler.nodes.FrameState uses sublist!!
     *
     * TODO Sublist was most likely to cause trouble. Now implemented Sublist in SARImpl but not 100
     * correctly To implement this 100% correctly i need to make SAR the main class and delete the IMPL
     * class
     *
     */

}
