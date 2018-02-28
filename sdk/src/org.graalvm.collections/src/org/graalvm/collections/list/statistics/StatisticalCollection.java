package org.graalvm.collections.list.statistics;

/**
 * Ensures that the current Load Factor and the current maximal Capacity of a collection can be
 * determined.
 */
interface StatisticalCollection {

    double getCurrentLoadFactor();

    int getCurrentCapacity();
}
