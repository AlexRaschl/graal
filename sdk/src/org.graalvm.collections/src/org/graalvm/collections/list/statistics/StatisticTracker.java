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
     * Sets the capacity of the list. Need this method to not save the reference to the tracked list and
     * thus prevent it to get freed by the gc
     *
     * @param capacity The capacity of the list
     */
    public void setCurrentCapacity(int capacity);

    /**
     * Returns the current size of the tracked list
     *
     * @return size of list
     */
    int getCurrentSize();

    /**
     * Sets the size of the list. Need this method to not save the reference to the tracked list and
     * thus prevent it to get freed by the gc
     *
     * @param size The size of the list
     */
    void setCurrentSize(int size);

    /**
     * Returns the StackTraceElement that describes the allocation site of the List tracked by the
     * Tracker
     *
     * @return StackTraceElement of Allocation site
     */
    StackTraceElement getAllocationSite();

    /**
     * Returns the current load factor of the tracked list
     *
     * @return load factor of list
     */
    double getCurrentLoadFactor();

    /**
     * Sets the current LoadFactor
     * 
     * @param loadFactor
     */
    void setCurrentLoadFactor(double loadFactor);

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
