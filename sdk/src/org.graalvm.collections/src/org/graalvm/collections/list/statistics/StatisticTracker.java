package org.graalvm.collections.list.statistics;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

/**
 * The StatisticTracker is used to generate and store statistics about the
 * StatisticalSpecifiedArrayList.
 *
 */
interface StatisticTracker {

    /**
     * Indicates that specified instruction has been excecuted
     */
    void countOP(Operation op);

    /**
     * Tells the Tracker that the Data Structure has been modified
     */
    void modified();

    /**
     * Prints the general Information about this specific tracker
     */
    void printGeneralInformation();

    /**
     * Returns the current capacity of the tracked list
     *
     * @return capacity of list
     */
    int getCurrentCapacity();

    /**
     * Returns the current size of the tracked list
     *
     * @return size of list
     */
    int getCurrentSize();

    /**
     * Returns the current load factor of the tracked list
     *
     * @return load factor of list
     */
    double getCurrentLoadFactor();

}
