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
    public static synchronized void addTracker(StatisticTrackerImpl tracker) {
        trackers.add(tracker);
    }

    static synchronized StatisticTracker getTrackerByID(int id) {
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

    public static synchronized void printGlobalInformation() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("GLOBAL INFORMATION: \n");
        sb.append("Current used Size: ");
        sb.append(getCurrentTotalSize());
        sb.append('\n');
        sb.append("Current available Capacity: ");
        sb.append(getCurrentTotalCapacity());
        sb.append('\n');
        sb.append("Current global load factor: ");
        sb.append(((double) getCurrentTotalSize()) / getCurrentTotalCapacity());
        sb.append('\n');
        sb.append("Trackers allocated: ");
        sb.append(StatisticTrackerImpl.getNextID() - 1);
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

    /*
     * Data Lines for CSV Generator
     */
    public static synchronized String[] getOpDataLines(final char dataSeparator) {
        String[] dataArr = new String[globalOpMap.size()];
        Iterator<Entry<Operation, AtomicInteger>> itr = globalOpMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder(50);

        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();
            sb.append(0);
            sb.append(dataSeparator);
            sb.append(entry.getKey().name());
            sb.append(dataSeparator);
            sb.append(' ');
            sb.append(entry.getValue().get());
            dataArr[n++] = sb.toString();
            sb = new StringBuilder(50);
        }
        return dataArr;
    }

    public static synchronized String[] getAllocSiteLines(final char dataSeparator) {
        final String[] allocSites = new String[trackers.size()];
        StringBuilder sb = new StringBuilder(50);

        int i = 0;
        for (StatisticTracker t : trackers) {
            sb.append(t.getID());
            sb.append(dataSeparator);
            sb.append(t.getAllocationSite().getClassName());
            allocSites[i++] = sb.toString();

            sb = new StringBuilder(50);
        }
        return allocSites;
    }

    /*
     * Data Lines for CSV Generator
     */
    public static synchronized String[] getTypeDataLines(final char dataSeparator) {
        String[] dataArr = new String[globalTypeMap.size()];
        Iterator<Entry<Type, AtomicInteger>> itr = globalTypeMap.entrySet().iterator();

        int n = 0;
        StringBuilder sb = new StringBuilder(30);
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(0);
            sb.append(dataSeparator);
            sb.append(entry.getKey().getTypeName());
            sb.append(dataSeparator);
            sb.append(' ');
            sb.append(entry.getValue().get());
            dataArr[n++] = sb.toString();
            sb = new StringBuilder();
        }
        return dataArr;
    }

    private static final int INTERVAL_SIZE = 10;

    /*
     * Data Lines for CSV Generator
     */

    // TODO ADD 0 LF recognition
    public static synchronized String[] getLoadFactorDataLines(final char dataSeparator) {
        int[] intervalOccurrences = new int[INTERVAL_SIZE + 1];
        String[] dataArr = new String[INTERVAL_SIZE + 1];
        double stepSize = 100 / INTERVAL_SIZE;
        for (StatisticTracker t : trackers) {
            double lf = t.getCurrentLoadFactor() * 100.0;
            int i = 1;
            while (i <= INTERVAL_SIZE + 1) {
                if (lf < i * stepSize) {
                    intervalOccurrences[i - 1]++;
                    break;
                }
                i++;
            }
        }

        StringBuilder sb = new StringBuilder(25);
        for (int i = 0; i < INTERVAL_SIZE; i++) {
            sb.append(0);
            sb.append(dataSeparator);
            sb.append("[");
            sb.append((i) * stepSize);
            sb.append("%, ");
            sb.append((i + 1) * stepSize);
            sb.append("%[");
            sb.append(dataSeparator);
            sb.append(' ');
            sb.append(intervalOccurrences[i]);
            dataArr[i] = sb.toString();
            sb = new StringBuilder(25);
        }
        sb.append(0);
        sb.append(dataSeparator);
        sb.append("[100%, 100%]");
        sb.append(dataSeparator);
        sb.append(' ');
        sb.append(intervalOccurrences[INTERVAL_SIZE]);
        dataArr[INTERVAL_SIZE] = sb.toString();
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
        }
        return sb.toString();
    }

    public void printPrettyGlobalOpMapContentString() {
        System.out.println(getPrettyOpMapContentString(globalOpMap));
    }

    public static String getPrettyTypeMapContentString() {
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
        }
        return sb.toString();
    }

    private static synchronized int getCurrentTotalCapacity() {
        int capacity = 0;
        for (StatisticTracker t : trackers) {
            capacity += t.getCurrentCapacity();
        }
        return capacity;
    }

    private static synchronized int getCurrentTotalSize() {
        int size = 0;
        for (StatisticTracker t : trackers) {
            size += t.getCurrentSize();
        }
        return size;
    }

}
