package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.graalvm.collections.list.statistics.Statistics.Operation;

public class AllocationSiteStatisticTrackerImpl implements StatisticTracker {

    private static int nextID = 1;

    private final HashMap<Statistics.Operation, AtomicInteger> localOpMap;

    private final String allocSite;
    private final int ID;

    // Can be changed by multiple threads
    private volatile int modifications = 0;
    private volatile int size = 0;
    private volatile int capacity = 0;

    private final HashMap<Statistics.Operation, HashMap<Type, AtomicInteger>> localTypeOpMap;

    private boolean isAdded = false;
    private Type mainType; // This is mostly useless here. Use the typeOPDistribution to get all types

    public AllocationSiteStatisticTrackerImpl(StackTraceElement allocSite) {
        this.ID = nextID++;
        this.allocSite = StatisticTracker.setAllocSiteElem(allocSite);
        this.localOpMap = new HashMap<>(Statistics.Operation.values().length);
        if (StatisticConfigs.INIT_ZERO) {
            for (Operation op : StatisticConfigs.INIT_ZERO_SET)
                localOpMap.put(op, new AtomicInteger(0));
        }
        this.localTypeOpMap = new HashMap<>();
    }

    public void countOP(Statistics.Operation op) {
        synchronized (Statistics.globalOpMap) {
            StatisticTracker.addOpTo(Statistics.globalOpMap, op);
        }
        synchronized (localOpMap) {
            StatisticTracker.addOpTo(localOpMap, op);
        }
    }

    public synchronized void modified() {
        modifications++;
    }

    public int getCurrentCapacity() {
        return capacity;
    }

    public synchronized void setCurrentCapacity(int capacity) {
        this.capacity += capacity;
    }

    public int getCurrentSize() {
        return size;
    }

    public synchronized void setCurrentSize(int size) {
        this.size += size;
    }

    public String getAllocationSite() {
        return allocSite;
    }

    public synchronized double getCurrentLoadFactor() {
        if (capacity == 0) {
            return 1.1;
        } else {
            return ((double) size) / capacity;
        }
    }

    // Unsupported
    public void setCurrentLoadFactor(double loadFactor) {
        return;
        // this.loadFactor = loadFactor;
    }

    public int getID() {
        return ID;
    }

    public Type getType() {
        return this.mainType;
    }

    public void printGeneralInformation() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("StatisticTrackerImpl with ID: ");
        sb.append(this.ID);
        sb.append('\n');
        sb.append("Tracks list of: ");
        sb.append(this.mainType.getTypeName());
        sb.append('\n');
        sb.append("Current used Size: ");
        sb.append(size);
        sb.append('\n');
        sb.append("Current Capacity: ");
        sb.append(capacity);
        sb.append('\n');
        sb.append("Current load factor: ");
        sb.append(getCurrentLoadFactor());
        sb.append('\n');
        sb.append("Allocation Site: ");
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

    public String[] getOpDataLines(char dataSeparator) {
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

    public void addTypeOpToMap(Operation op, Type t) {

        if (!StatisticConfigs.SPECIAL_OPS.contains(op))
            return;
        synchronized (localTypeOpMap) {
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
    }

    public synchronized void setType(Class<?> c) {
        if (!isAdded) {
            synchronized (Statistics.globalTypeMap) {
                this.mainType = c;
                StatisticTracker.addTypeTo(Statistics.globalTypeMap, mainType);
                isAdded = true;
            }
        }
    }

}
