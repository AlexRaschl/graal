package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

public class Statistics {

    // For CSV file creation
    private final static char DATA_SEPARATOR = ';';

    // List of all StatisticTrackers
    static final LinkedList<StatisticTrackerImpl> trackers = new LinkedList<>();

    // Map of all Operations performed
    static final HashMap<Operation, AtomicInteger> globalOpMap = new HashMap<>(Operation.values().length);

    // Type Distribution
    static final HashMap<Type, AtomicInteger> globalTypeMap = new HashMap<>();

    /*
     * Instances of StatisticTrackerImpl call this function to enlist themselves in the trackers list.
     */
    public static void addTracker(StatisticTrackerImpl tracker) {
        trackers.add(tracker);
    }

    public static void printOverallSummary() {
        printGlobalInformation();

        for (StatisticTrackerImpl t : trackers) {
            t.printGeneralInformation();
        }
    }

    static void printGlobalInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("GLOBAL INFORMATION: \n");
        sb.append("Current used Size: " + getCurrentTotalSize() + "\n");
        sb.append("Current available Capacity: " + getCurrentTotalCapacity() + "\n");
        sb.append("Current load factor: " + ((double) getCurrentTotalSize()) / getCurrentTotalCapacity() + "\n");
        sb.append("Trackers allocated: " + StatisticTrackerImpl.getNextID() + "\n\n");
        sb.append("OPERATION USAGE: \n");
        sb.append(getPrettyOpMapContentString(globalOpMap) + "\n");
        sb.append("TYPE DISTRIBUTION: \n");
        sb.append(getPrettyTypeMapContentString());
        sb.append("END of Summary! \n\n");
        System.out.print(sb.toString());
    }

    private static int getCurrentTotalCapacity() {
        int capacity = 0;
        for (StatisticTrackerImpl t : trackers) {
            capacity += t.getCurrentCapacity();
        }
        return capacity;
    }

    private static int getCurrentTotalSize() {
        int size = 0;
        for (StatisticTrackerImpl t : trackers) {
            size += t.getCurrentSize();
        }
        return size;
    }

    // TODO USE StringBuilder
    public static String[] getOpHistogramData() {
        String[] dataArr = new String[globalOpMap.size() + 1];
        Iterator<Entry<Operation, AtomicInteger>> itr = globalOpMap.entrySet().iterator();

        dataArr[0] = "Operation" + DATA_SEPARATOR + " Occurrences" + DATA_SEPARATOR;
        int n = 1;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();
            dataArr[n++] = entry.getKey().name() + DATA_SEPARATOR + " " + entry.getValue().get() + DATA_SEPARATOR + " ";
        }
        return dataArr;
    }

    // TODO USE StringBuilder
    public static String[] getTypeHistogramData() {
        String[] dataArr = new String[globalTypeMap.size() + 1];
        Iterator<Entry<Type, AtomicInteger>> itr = globalTypeMap.entrySet().iterator();

        dataArr[0] = "Type" + DATA_SEPARATOR + " Occurrences" + DATA_SEPARATOR;
        int n = 1;
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            dataArr[n++] = entry.getKey().getTypeName() + DATA_SEPARATOR + " " + entry.getValue().get() + DATA_SEPARATOR + " ";
        }
        return dataArr;
    }

    private static final int INTERVAL_SIZE = 10;

    // TODO USE StringBuilder
    public static String[] getLoadFactorHistogramData() {
        int[] intervalOccurrences = new int[INTERVAL_SIZE];
        String[] dataArr = new String[INTERVAL_SIZE + 1];
        double stepSize = 100 / INTERVAL_SIZE;
        for (StatisticTrackerImpl t : trackers) {
            double lf = t.getCurrentLoadFactor() * 100.0;
            int i = 1;
            while (i <= INTERVAL_SIZE) {
                if (lf < i * stepSize) {
                    intervalOccurrences[i - 1]++;
                    break;
                }
                i++;
            }
        }

        dataArr[0] = "Upper Bound" + DATA_SEPARATOR + " Load Factor Percentage" + DATA_SEPARATOR;
        for (int i = 1; i <= INTERVAL_SIZE; i++) {
            dataArr[i] = "[" + (i - 1) * 10 + "%, " + i * 10 + "%[" + DATA_SEPARATOR + " " + intervalOccurrences[i - 1] + DATA_SEPARATOR + " ";
        }
        return dataArr;
    }

    static String getPrettyOpMapContentString(HashMap<Operation, AtomicInteger> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<Operation, AtomicInteger>> itr = map.entrySet().iterator();

        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();
            sb.append(++n + ": Operation: " + entry.getKey().name() + ", Usages: " + entry.getValue().get() + "\n");
        }
        return sb.toString();
    }

    static String getPrettyTypeMapContentString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<Type, AtomicInteger>> itr = Statistics.globalTypeMap.entrySet().iterator();

        int n = 0;
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(++n + ": Type: " + entry.getKey().getTypeName() + ", Usages: " + entry.getValue().get() + "\n");
        }
        return sb.toString();
    }

}
