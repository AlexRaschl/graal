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

    /**
     * Returns the ID of the tracker
     *
     * @return ID
     */
    int getID();

    /**
     * Creates a String Array that containing the gathered information about the operation distribution
     * of this StatisticTracker. One ArrayFiled corresponds to one line of data. The data entries in one
     * line are separated by the given separator. The separator gets passed for CSV File creation.
     *
     * @param separator Char to be used to separate Data entries
     * @return
     */
    String[] getOpDataLines(final char dataSeparator);

    /**
     * Creates a String Array that containing the gathered information about the distribution of
     * operations performed on specific sub Types and Types stored in the list of this StatisticTracker.
     * One ArrayFiled corresponds to one line of data. The data entries in one line are separated by the
     * given separator. The separator gets passed for CSV File creation.
     *
     * @param dataSeparator
     * @return
     */
    String[] getTypeOpDataLines(final char dataSeparator);

}
