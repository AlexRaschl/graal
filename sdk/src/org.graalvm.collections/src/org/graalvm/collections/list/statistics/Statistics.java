package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Statistics {

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    // List of all StatisticTrackers
    static final ArrayList<StatisticTracker> trackers = new ArrayList<>();

    static final HashMap<String, StatisticTracker> allocToTracker = new HashMap<>();

    // Map of all Operations performed
    static final HashMap<Operation, AtomicInteger> globalOpMap = new HashMap<>(Operation.values().length);

    // Type Distribution
    static final HashMap<Type, AtomicInteger> globalTypeMap = new HashMap<>();

    static enum Operation {
        CSTR_STD,
        CSTR_COLL,
        CSTR_CAP,
        CONTAINS,
        CONTAINS_ALL,
        TO_ARRAY,
        ADD_OBJ,
        ADD_INDEXED,
        ADD_ALL,
        ADD_ALL_INDEXED,
        REMOVE_OBJ,
        REMOVE_INDEXED,
        REMOVE_ALL,
        RETAIN_ALL,
        CLEAR,
        GET_INDEXED,
        SET_INDEXED,
        INDEX_OF,
        INDEX_OF_LAST,
        ITERATOR,
        CREATE_LIST_ITR,
        CREATE_LIST_ITR_INDEXED,
        SUBLIST,
        HASH_CODE,
        EQUALS,
        TO_STRING,
        ENSURE_CAP,
        TRIM_TO_SIZE,
        EMPTY,
        SIZE,
        GROW,
        SORT,
        CLONE,
        SPLITERATOR
    }

    /*
     * Instances of StatisticTrackerImpl call this function to enlist themselves in the trackers list.
     */
    public static void addTracker(StatisticTracker tracker) {
        LOCK.writeLock().lock();
        try {
            if (StatisticConfigs.USE_ALLOC_SITE_TRACKING) {
                allocToTracker.put(tracker.getAllocationSite(), tracker);
            }
            trackers.add(tracker);
        } finally {
            LOCK.writeLock().unlock();
        }

    }

    public static void getReadLock() {
        LOCK.readLock().lock();
    }

    public static void releaseReadLock() {
        LOCK.readLock().unlock();
    }

    public static StatisticTracker fetchTracker(String allocSite) {
        LOCK.readLock().lock();
        try {
            if (allocToTracker.containsKey(allocSite)) {
                return allocToTracker.get(allocSite);
            } else {
                return null;
            }
        } finally {
            LOCK.readLock().unlock();
        }
    }

    static StatisticTracker getTrackerByID(int id) {
        LOCK.readLock().lock();
        try {
            final Iterator<StatisticTracker> itr = trackers.iterator();
            while (itr.hasNext()) {
                StatisticTracker t = itr.next();
                if (t.getID() == id)
                    return t;
            }
            return null;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static void printOverallSummary() {
        LOCK.readLock().lock();
        try {
            printGlobalInformation();

            for (StatisticTracker t : trackers) {
                t.printGeneralInformation();
            }
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static void printGlobalInformation() {
        LOCK.readLock().lock();
        try {
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
            sb.append(trackers.size());
            sb.append('\n');
            sb.append("OPERATION USAGE: \n");
            sb.append('\n');
            sb.append(getPrettyOpMapContentString(globalOpMap));
            sb.append('\n');
            sb.append("TYPE DISTRIBUTION: \n");
            sb.append(getPrettyTypeMapContentString());
            sb.append("END of Summary! \n\n");
            System.out.print(sb.toString());
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /*
     * Data Lines for CSV Generator
     */
    public static String[] getOpDataLines(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static String[] getAllocSiteLines(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
            final String[] allocSites = new String[trackers.size()];
            StringBuilder sb = new StringBuilder(50);

            int i = 0;

            for (StatisticTracker t : trackers) {
                sb.append(t.getID());
                sb.append(dataSeparator);
                sb.append(t.getAllocationSite());
                allocSites[i++] = sb.toString();

                sb = new StringBuilder(50);
            }
            return allocSites;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /*
     * Returns an Overview of the used Types that the tracked lists are storing
     */
    public static String[] getTypeDataLines(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    /**
     * Returns a sequence of trackerIds and their Types.
     *
     * @param dataSeparator
     * @return
     */
    public static String[] getTypesForAllTrackers(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
            final String[] dataArr = new String[trackers.size()];

            StringBuilder sb = new StringBuilder(40);
            int n = 0;// TrackerId

            for (StatisticTracker tracker : trackers) {
                sb.append(tracker.getID());
                sb.append(dataSeparator);
                sb.append(tracker.getType());
                dataArr[n++] = sb.toString();
                sb = new StringBuilder();
            }
            return dataArr;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static String[] getCapacityAndSizes(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            final String[] dataArr = new String[trackers.size()];

            int n = 0;
            for (StatisticTracker tracker : trackers) {
                sb.append(tracker.getID());
                sb.append(dataSeparator);
                sb.append(tracker.getCurrentSize());
                sb.append(dataSeparator);
                sb.append(tracker.getCurrentCapacity());
                dataArr[n++] = sb.toString();
                sb = new StringBuilder();
            }
            return dataArr;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    private static final int INTERVAL_SIZE = 10;

    /*
     * Data Lines for CSV Generator
     */

    public static String[] getLoadFactorDataLinesOld(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static String[] getLoadFactorDataLines(final char dataSeparator) {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    static String getPrettyOpMapContentString(HashMap<Operation, AtomicInteger> map) {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public void printPrettyGlobalOpMapContentString() {
        System.out.println(getPrettyOpMapContentString(globalOpMap));
    }

    public static String getPrettyTypeMapContentString() {
        LOCK.readLock().lock();
        try {
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
        } finally {
            LOCK.readLock().unlock();
        }
    }

    private static int getCurrentTotalCapacity() {
        LOCK.readLock().lock();
        try {
            int capacity = 0;
            for (StatisticTracker t : trackers) {
                capacity += t.getCurrentCapacity();
            }
            return capacity;
        } finally {
            LOCK.readLock().unlock();
        }
    }

    private static int getCurrentTotalSize() {
        LOCK.readLock().lock();
        try {
            int size = 0;
            for (StatisticTracker t : trackers) {
                size += t.getCurrentSize();
            }
            return size;
        } finally {
            LOCK.readLock().unlock();
        }
    }

}
