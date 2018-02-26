package org.graalvm.collections.list.statistics;

/**
 * The StatisticTracker is used to generate and store statistics about Abstract Data Structures.
 * Therefore it is recommended to generate a Wrapper class for the ADS, which keeps the
 * functionality of the original ADS but also holds an Instance of a StatisticTracker to gather
 * information during runtime.
 *
 */
public interface StatisticTracker {

}
