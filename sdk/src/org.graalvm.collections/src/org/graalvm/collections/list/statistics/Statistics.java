package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

public class Statistics {

    // List of all StatisticTrackers
    static final LinkedList<StatisticTracker> trackers = new LinkedList<>();

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

    public static StatisticTracker getTrackerByID(int id) {
        Iterator<StatisticTracker> itr = trackers.iterator();
        while (itr.hasNext()) {
            StatisticTracker t = itr.next();
            if (t.getID() == id)
                return t;
        }
        return null;
    }

    public static void printOverallSummary() {
        printGlobalInformation();

        for (StatisticTracker t : trackers) {
            t.printGeneralInformation();
        }
    }

    static void printGlobalInformation() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("GLOBAL INFORMATION: \n");
        sb.append("Current used Size: ");
        sb.append(getCurrentTotalSize());
        sb.append('\n');
        sb.append("Current available Capacity: ");
        sb.append(getCurrentTotalCapacity());
        sb.append('\n');
        sb.append("Current load factor: ");
        sb.append(((double) getCurrentTotalSize()) / getCurrentTotalCapacity());
        sb.append('\n');
        sb.append("Trackers allocated: ");
        sb.append(StatisticTrackerImpl.getNextID());
        sb.append('\n');
        sb.append("OPERATION USAGE: \n");
        sb.append('\n');
        sb.append(getPrettyOpMapContentString(globalOpMap));
        sb.append('\n');
        sb.append("TYPE DISTRIBUTION: \n");
        sb.append(getPrettyTypeMapContentString());
        sb.append("END of Summary! \n\n");
        System.out.print(sb.toString());
    }

    private static int getCurrentTotalCapacity() {
        int capacity = 0;
        for (StatisticTracker t : trackers) {
            capacity += t.getCurrentCapacity();
        }
        return capacity;
    }

    private static int getCurrentTotalSize() {
        int size = 0;
        for (StatisticTracker t : trackers) {
            size += t.getCurrentSize();
        }
        return size;
    }

    // TODO USE StringBuilder
    public static String[] getOpDataLines(final char separator) {
        String[] dataArr = new String[globalOpMap.size() + 1];
        Iterator<Entry<Operation, AtomicInteger>> itr = globalOpMap.entrySet().iterator();

        StringBuilder sb = new StringBuilder(30);
        sb.append("Operation Occurrences");
        sb.append(separator);
        dataArr[0] = sb.toString();
        sb = new StringBuilder(50);

        int n = 1;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();

            sb.append(entry.getKey().name());
            sb.append(separator);
            sb.append(' ');
            sb.append(entry.getValue().get());
            sb.append(separator);
            sb.append(' ');
            dataArr[n++] = sb.toString();
            sb = new StringBuilder(50);
            // dataArr[n++] = entry.getKey().name() + separator + " " + entry.getValue().get() + separator + "
            // ";
        }
        return dataArr;
    }

    // DONE USE StringBuilder
    public static String[] getTypeDataLines(final char separator) {
        String[] dataArr = new String[globalTypeMap.size() + 1];
        Iterator<Entry<Type, AtomicInteger>> itr = globalTypeMap.entrySet().iterator();

        dataArr[0] = "Type Occurrences" + separator;
        int n = 1;
        StringBuilder sb = new StringBuilder();
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(entry.getKey().getTypeName());
            sb.append(separator);
            sb.append(' ');
            sb.append(entry.getValue().get());
            sb.append(separator);
            sb.append(' ');
            dataArr[n++] = sb.toString();
            sb = new StringBuilder();
            // dataArr[n++] = entry.getKey().getTypeName() + separator + " " + entry.getValue().get() +
            // separator + " ";
        }
        return dataArr;
    }

    private static final int INTERVAL_SIZE = 10;

    // TODO USE StringBuilder
    public static String[] getLoadFactorDataLines(final char separator) {
        int[] intervalOccurrences = new int[INTERVAL_SIZE];
        String[] dataArr = new String[INTERVAL_SIZE + 1];
        double stepSize = 100 / INTERVAL_SIZE;
        for (StatisticTracker t : trackers) {
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

        dataArr[0] = "Load Factor Intervals" + separator;
        StringBuilder sb = new StringBuilder(25);
        for (int i = 1; i <= INTERVAL_SIZE; i++) {
            sb.append('[');
            sb.append((i - 1) * stepSize);
            sb.append("%, ");
            sb.append(i * stepSize);
            sb.append("%[");
            sb.append(separator);
            sb.append(' ');
            sb.append(intervalOccurrences[i - 1]);
            sb.append(separator);
            sb.append(' ');
            dataArr[i] = sb.toString();
            sb = new StringBuilder(25);
            // dataArr[i] = "[" + (i - 1) * stepSize + "%, " + i * stepSize + "%[" + separator + " " +
            // intervalOccurrences[i - 1] + separator + " ";
        }
        return dataArr;
    }

    static String getPrettyOpMapContentString(HashMap<Operation, AtomicInteger> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<Operation, AtomicInteger>> itr = map.entrySet().iterator();

        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();
            sb.append(++n);
            sb.append(": Operation: ");
            sb.append(entry.getKey().name());
            sb.append(", Usages: ");
            sb.append(entry.getValue().get());
            sb.append('\n');
            // sb.append(++n + ": Operation: " + entry.getKey().name() + ", Usages: " + entry.getValue().get() +
            // "\n");
        }
        return sb.toString();
    }

    static String getPrettyTypeMapContentString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<Type, AtomicInteger>> itr = Statistics.globalTypeMap.entrySet().iterator();

        int n = 0;
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(++n);
            sb.append(": Type: ");
            sb.append(entry.getKey().getTypeName());
            sb.append(", Usages: ");
            sb.append(entry.getValue().get());
            sb.append('\n');
            // sb.append(++n + ": Type: " + entry.getKey().getTypeName() + ", Usages: " + entry.getValue().get()
            // + "\n");
        }
        return sb.toString();
    }

}
