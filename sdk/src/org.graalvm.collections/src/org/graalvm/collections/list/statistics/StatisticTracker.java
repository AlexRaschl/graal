package org.graalvm.collections.list.statistics;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

/**
 * The StatisticTracker is used to generate and store statistics about the
 * StatisticalSpecifiedArrayList.
 *
 */
interface StatisticTracker { // TODO check if Interface is needed

    /** Indicates that specified instruction has been excecuted */
    void countOP(Operation op);

    /** Tells the Tracker that the Data Structure has been modified */
    void modified();

    void printGeneralInformation();

    int getCurrentCapacity();

    int getCurrentSize();

    double getCurrentLoadFactor();

}
