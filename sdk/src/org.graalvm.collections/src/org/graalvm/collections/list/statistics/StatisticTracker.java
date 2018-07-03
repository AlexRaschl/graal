package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The StatisticTracker is used to generate and store statistics about the
 * StatisticalSpecifiedArrayList.
 *
 */
interface StatisticTracker {

    /**
     * Indicates that specified instruction has been excecuted
     */
    void countOP(Statistics.Operation op);

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
    String getAllocationSite();

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
     * Get the main type that the tracked list stores. Main type is the type of the element that is
     * first added to the list
     *
     * @return the main type that the tracked list stores.
     */
    Type getType();

    // Set the allocation Site of the tracker
    // void setAllocSiteElem(StackTraceElement elem);

    // void adds the Operation to the TypeMap
    void addTypeOpToMap(Statistics.Operation op, Type t);

    // Sets the type of the tracker
    void setType(Class<?> c);

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

    public static String setAllocSiteElem(StackTraceElement elem) {
        if (StatisticConfigs.AGGREGATE_SAME_CLASSES) {
            return elem.getClassName();
        }
        return elem.getClassName() + "#" + elem.getMethodName() + ":" + elem.getLineNumber();
    }

    public static StatisticTracker initTracker(StackTraceElement allocSite) {
        synchronized (Statistics.allocToTracker) {
            StatisticTracker tracker = Statistics.fetchTracker(StatisticTracker.setAllocSiteElem(allocSite));
            if (tracker == null) {
                tracker = new AllocationSiteStatisticTrackerImpl(allocSite);
                Statistics.addTracker(tracker);
            }
            return tracker;
        }
    }

    static void addOpTo(HashMap<Statistics.Operation, AtomicInteger> map, Statistics.Operation op) {
        AtomicInteger curr = map.getOrDefault(op, null);
        if (curr == null) {
            map.put(op, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

    static void addTypeTo(HashMap<Type, AtomicInteger> map, Type t) {
        AtomicInteger curr = map.getOrDefault(t, null);
        if (curr == null) {
            map.put(t, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

    static void initStrings(String[][] dataArr) {
        for (int r = 0; r < dataArr.length; r++) {
            dataArr[r] = new String[0];
        }
    }

    static void putData(String[] dataLines, Statistics.Operation op, HashMap<Type, AtomicInteger> map, int ID, char dataSeparator) {

        final Iterator<Entry<Type, AtomicInteger>> itr = map.entrySet().iterator();

        StringBuilder sb = new StringBuilder(50);
        int n = 0;
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(ID);
            sb.append(dataSeparator);
            sb.append(op.name());
            sb.append(dataSeparator);
            sb.append(entry.getKey().getTypeName());
            sb.append(dataSeparator);
            sb.append(entry.getValue().get());
            dataLines[n++] = sb.toString();
            sb = new StringBuilder(50);

        }
    }

    static String[] getFlatStringArray(String[][] dataArr) {
        if (dataArr == null)
            return null;

        final int dim1 = dataArr.length;
        if (dim1 == 0)
            return new String[0];
        if (dataArr[0] == null) {
            return null;
        }

        final ArrayList<String> result = new ArrayList<>();

        int i = 0;
        for (int r = 0; r < dim1; r++) {
            for (int c = 0; c < dataArr[r].length; c++) {
                result.add(dataArr[r][c]);
            }
        }
        return result.toArray(new String[1]);
    }
}
