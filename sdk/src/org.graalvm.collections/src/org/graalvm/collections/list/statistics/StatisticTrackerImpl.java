package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static org.graalvm.collections.list.statistics.Statistics.Operation.*;

public class StatisticTrackerImpl implements StatisticTracker {
    // _____GLOBAL FIELDS______
    // ID
    private static int nextID = 1;

    // _____LOCAL FIELDS______
    private final int ID;

    // Map of Operations performed on the list
    private final HashMap<Statistics.Operation, AtomicInteger> localOpMap;

    // Maps for tracking Operation Distribution based on inserted Types and Subtypes. Only Operations
    // listed in SPECIAL_OPS are tracked
    private final HashMap<Statistics.Operation, HashMap<Type, AtomicInteger>> localTypeOpMap;

    // Type
    private Type type;
    // private StackTraceElement allocSiteElem;
    private String allocSite;
    private boolean isAdded = false;

    // Number of times the content of the list has changed NOTE: Changes made by Iterators are not
    // tracked
    private int modifications;

    private int size = 0;
    private int capacity = 0;
    private double loadFactor;

    public StatisticTrackerImpl(StackTraceElement allocSite) {
        ID = nextID++;
        this.localOpMap = new HashMap<>(Statistics.Operation.values().length);
        this.localOpMap.put(GROW, new AtomicInteger(0));
        this.localTypeOpMap = new HashMap<>();
        this.modifications = 0;
        Statistics.addTracker(this);
        this.allocSite = StatisticTracker.setAllocSiteElem(allocSite);
    }

    public void countOP(Statistics.Operation op) {
        synchronized (Statistics.globalOpMap) {
            StatisticTracker.addOpTo(Statistics.globalOpMap, op);
        }
        StatisticTracker.addOpTo(localOpMap, op);
    }

    public void modified() {
        modifications++;
    }

    public int getCurrentCapacity() {
        return capacity;
    }

    public void setCurrentCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentSize() {
        return size;
    }

    public void setCurrentSize(int size) {
        this.size = size;
    }

    public double getCurrentLoadFactor() {
        return loadFactor;
    }

    public Type getType() {
        return this.type;
    }

    public void setCurrentLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public int getID() {
        return ID;
    }

// public String[] getOpDataLines(final char dataSeparator) {
// final String[] dataArr = new String[localOpMap.size()];
// final Iterator<Entry<Operation, AtomicInteger>> itr = localOpMap.entrySet().iterator();
// StringBuilder sb = new StringBuilder(50);
//
// int n = 0;
// while (itr.hasNext()) {
// Entry<Operation, AtomicInteger> entry = itr.next();
// sb.append(this.ID);
// sb.append(dataSeparator);
// sb.append(entry.getKey().name());
// sb.append(dataSeparator);
// sb.append(entry.getValue().get());
//
// dataArr[n++] = sb.toString();
// sb = new StringBuilder(50);
// }
// return dataArr;
// }

    public String[] getOpDataLines(final char dataSeparator) {
        final String[] dataArr = new String[localOpMap.size()];
        final Iterator<Entry<Statistics.Operation, AtomicInteger>> itr = localOpMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder(50);

        int n = 0;
        while (itr.hasNext()) {
            Entry<Statistics.Operation, AtomicInteger> entry = itr.next();
            sb.append(this.ID);
            sb.append(dataSeparator);
            sb.append(entry.getKey().name());
            sb.append(dataSeparator);
            sb.append(entry.getValue().get());
            dataArr[n++] = sb.toString();
            sb = new StringBuilder(50);
        }
        return dataArr;
    }

    public String[] getTypeOpDataLines(char dataSeparator) {
        final String[][] dataArr = new String[localTypeOpMap.size()][];
        StatisticTracker.initStrings(dataArr);
        final Iterator<Entry<Statistics.Operation, HashMap<Type, AtomicInteger>>> itr = localTypeOpMap.entrySet().iterator();
        int n = 0;
        while (itr.hasNext()) {
            Entry<Statistics.Operation, HashMap<Type, AtomicInteger>> entry = itr.next();
            Statistics.Operation op = entry.getKey();
            HashMap<Type, AtomicInteger> map = entry.getValue();
            dataArr[n] = new String[map.size()];
            StatisticTracker.putData(dataArr[n], op, map, this.ID, dataSeparator);
            n++;
        }
        return StatisticTracker.getFlatStringArray(dataArr);
    }

    public void printGeneralInformation() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("StatisticTrackerImpl with ID: ");
        sb.append(this.ID);
        sb.append('\n');
        sb.append("Tracks list of: ");
        sb.append(this.type.getTypeName());
        sb.append('\n');
        sb.append("Current used Size: ");
        // sb.append(list.getCurrentSize());
        sb.append(size);
        sb.append('\n');
        sb.append("Current Capacity: ");
        // sb.append(list.getCurrentCapacity());
        sb.append(capacity);
        sb.append('\n');
        sb.append("Current load factor: ");
        // sb.append(list.getCurrentLoadFactor());
        sb.append(loadFactor);
        sb.append('\n');
        sb.append("Allocation Site: ");
        // sb.append(allocSiteElem.getClassName());
        sb.append(allocSite);
        sb.append('\n');
        sb.append("Modifications made so far: ");
        sb.append(modifications);
        sb.append('\n');
        sb.append("Operation Usage: \n");
        sb.append(Statistics.getPrettyOpMapContentString(localOpMap));
        sb.append("END of Summary! \n\n");
        System.out.print(sb.toString());
    }

    static int getNextID() {
        return nextID;
    }

    // TODO check if i need to change the type if it is a superclass of the currently added one
    public synchronized void setType(Class<?> c) {
        if (!isAdded) {
            synchronized (Statistics.globalTypeMap) {
                this.type = c;
                StatisticTracker.addTypeTo(Statistics.globalTypeMap, type);
                isAdded = true;
            }
        }
    }

    public void addTypeOpToMap(Statistics.Operation op, Type t) {

        if (!StatisticConfigs.SPECIAL_OPS.contains(op))
            return;
        HashMap<Type, AtomicInteger> map = localTypeOpMap.getOrDefault(op, null);
        if (map == null) {
            map = new HashMap<>();
            AtomicInteger i = new AtomicInteger(1);
            map.put(t, i);
            localTypeOpMap.put(op, map);
        } else {
            AtomicInteger curr = map.getOrDefault(t, null);
            if (curr == null) {
                map.put(t, new AtomicInteger(1));
            } else {
                curr.getAndIncrement();
            }
        }
    }

    public String getAllocationSite() {
        // return allocSiteElem;
        return this.allocSite;
    }

// public String getAllocSiteName() {
// return this.allocSite;
// }

}
