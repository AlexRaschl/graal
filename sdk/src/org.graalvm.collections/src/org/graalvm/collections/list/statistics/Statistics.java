package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

public class Statistics {

    public static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

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
        final Iterator<StatisticTracker> itr = trackers.iterator();
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
        final String[] dataArr = new String[globalOpMap.size()];
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
        // sb.append("Trackers allocated: " + (StatisticTrackerImpl.getNextID() - 1));
        // sb.append("trackerList size: " + trackers.size());
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
     * Returns an Overview of the used Types that the tracked lists are storing
     */
    public static synchronized String[] getTypeDataLines(final char dataSeparator) {
        final String[] dataArr = new String[globalTypeMap.size()];
        final Iterator<Entry<Type, AtomicInteger>> itr = globalTypeMap.entrySet().iterator();

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

    /**
     * Returns a sequence of trackerIds and their Types.
     *
     * @param dataSeparator
     * @return
     */
    public static synchronized String[] getTypesForAllTrackers(final char dataSeparator) {
        final String[] dataArr = new String[trackers.size()];

        StringBuilder sb = new StringBuilder(40);
        int n = 0;// TrackerId

        for (StatisticTracker tracker : trackers) {
            // n = tracker.getID();
            sb.append(tracker.getID());
            sb.append(dataSeparator);
            sb.append(tracker.getType());
            dataArr[n++] = sb.toString();
            sb = new StringBuilder();
        }
        return dataArr;
    }

    public static String[] getCapacityAndSizes(final char dataSeparator) {
        StringBuilder sb = new StringBuilder();
        final String[] dataArr = new String[trackers.size()];

        int n = 0;
        for (StatisticTracker tracker : trackers) {
            // n = tracker.getID();
            sb.append(tracker.getID());
            sb.append(dataSeparator);
            sb.append(tracker.getCurrentSize());
            sb.append(dataSeparator);
            sb.append(tracker.getCurrentCapacity());
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
    public static synchronized String[] getLoadFactorDataLinesOld(final char dataSeparator) {
        int[] intervalOccurrences = new int[INTERVAL_SIZE + 2];
        String[] dataArr = new String[INTERVAL_SIZE + 2];
        double stepSize = 100 / INTERVAL_SIZE;
        for (StatisticTracker t : trackers) {
            double lf = t.getCurrentLoadFactor() * 100.0;
            if (lf == 0) {
                intervalOccurrences[INTERVAL_SIZE + 1]++;
            } else {
                int i = 1;
                while (i <= INTERVAL_SIZE + 1) {
                    if (lf < i * stepSize) {

                        intervalOccurrences[i - 1]++;
                        break;
                    }
                    i++;
                }
            }
        }

        StringBuilder sb = new StringBuilder(25);
        sb.append(0);
        sb.append(dataSeparator);
        sb.append("[0%, 0%]");
        sb.append(dataSeparator);
        sb.append(' ');
        sb.append(intervalOccurrences[INTERVAL_SIZE + 1]);
        dataArr[0] = sb.toString();
        sb = new StringBuilder(25);
        for (int i = 0; i < INTERVAL_SIZE; i++) {
            sb.append(0);
            sb.append(dataSeparator);
            if (i == 0) {
                sb.append(']');
            } else {
                sb.append('[');
            }
            sb.append((i) * stepSize);
            sb.append("%, ");
            sb.append((i + 1) * stepSize);
            sb.append("%[");
            sb.append(dataSeparator);
            sb.append(' ');
            sb.append(intervalOccurrences[i]);
            dataArr[i + 1] = sb.toString();
            sb = new StringBuilder(25);
        }
        sb.append(0);
        sb.append(dataSeparator);
        sb.append("[100%, 100%]");
        sb.append(dataSeparator);
        sb.append(' ');
        sb.append(intervalOccurrences[INTERVAL_SIZE]);
        dataArr[INTERVAL_SIZE + 1] = sb.toString();
        return dataArr;
    }

    public static synchronized String[] getLoadFactorDataLines(final char dataSeparator) {
        final int[] intervalOccurrences = new int[INTERVAL_SIZE + 3];
        final String[] dataArr = new String[INTERVAL_SIZE + 3];
        final double stepSize = 100 / INTERVAL_SIZE;

        for (StatisticTracker t : trackers) {
            final double lf = t.getCurrentLoadFactor() * 100.0;
            if (lf == 0) {
                intervalOccurrences[0]++;
            } else if (lf >= 100) {
                if (lf == 100) {
                    intervalOccurrences[INTERVAL_SIZE + 1]++;
                } else {
                    intervalOccurrences[INTERVAL_SIZE + 2]++;
                }

            } else {
                int i = 1;
                while (i <= INTERVAL_SIZE) {
                    if (lf < i * stepSize) {
                        intervalOccurrences[i]++;
                        break;
                    }
                    i++;
                }
            }
        }

        dataArr[0] = "[0%, 0%]" + dataSeparator + " " + intervalOccurrences[0];
        for (int i = 1; i <= INTERVAL_SIZE; i++) {
            dataArr[i] = ((i == 0) ? "]" : "[") + (i - 1) * stepSize + "%, " + i * stepSize + "%[" + dataSeparator + " " + intervalOccurrences[i];
        }
        dataArr[INTERVAL_SIZE + 1] = "[100%, 100%]" + dataSeparator + " " + intervalOccurrences[INTERVAL_SIZE + 1];
        dataArr[INTERVAL_SIZE + 2] = "[ZERO, SIZE]" + dataSeparator + " " + intervalOccurrences[INTERVAL_SIZE + 2];
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
