package org.graalvm.collections.list.statistics;

/**
 * Ensures that the current load factor and the current maximal capacity of a collection can be
 * determined.
 */
interface StatisticalCollection {

    /**
     * Returns the current load factor of the collection
     *
     * @return load factor of list
     */
    double getCurrentLoadFactor();

    /**
     * Returns the current capacity of the collection
     *
     * @return capacity of list
     */
    int getCurrentCapacity();
}
