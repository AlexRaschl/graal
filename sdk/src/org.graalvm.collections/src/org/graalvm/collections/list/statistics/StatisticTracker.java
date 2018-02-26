package org.graalvm.collections.list.statistics;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

/**
 * The StatisticTracker is used to generate and store statistics about Abstract Data Structures.
 * Therefore it is recommended to generate a Wrapper class for the ADS, which keeps the
 * functionality of the original ADS but also holds an Instance of a StatisticTracker to gather
 * information during runtime.
 *
 */
public interface StatisticTracker {

    /** Indicates that specified instruction has been excecuted */
    public void countOP(Operation op);

    /** Tells the Tracker that the Data Structure has been modified */
    public void modified();

}
