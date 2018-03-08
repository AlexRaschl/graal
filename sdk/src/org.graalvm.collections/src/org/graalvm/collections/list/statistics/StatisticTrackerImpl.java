package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticTrackerImpl implements StatisticTracker {
    // _____GLOBAL FIELDS______
    // ID
    private static int nextID = 0;

    // Operations
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
        SIZE
    }

    // _____LOCAL FIELDS______
    private final int ID;

    // Map of Operations performed on the list
    private final HashMap<Operation, AtomicInteger> localOpMap;

    // Type
    private Type type;
    private boolean isAdded = false;

    // Number of times the content of the list has changed
    private int modifications;

    @SuppressWarnings("rawtypes") private final StatisticalSpecifiedArrayListImpl list; // No Use of get, add, ....

    @SuppressWarnings("rawtypes")
    public StatisticTrackerImpl(StatisticalSpecifiedArrayListImpl list) {
        ID = nextID++;
        this.localOpMap = new HashMap<>(Operation.values().length);
        this.modifications = 0;
        this.list = list;

        Statistics.addTracker(this);
    }

    void setType(Class<?> c) {
        if (!isAdded) {
            this.type = c;
            addTypeTo(Statistics.globalTypeMap, type);
            isAdded = true;
        }
    }

    public void countOP(Operation op) {
        addOpTo(Statistics.globalOpMap, op);
        addOpTo(localOpMap, op);
    }

    public void modified() {
        modifications++;
    }

    public int getCurrentCapacity() {
        return list.getCurrentCapacity();
    }

    public int getCurrentSize() {
        return list.size();
    }

    public double getCurrentLoadFactor() {
        return list.getCurrentLoadFactor();
    }

    public int getID() {
        return ID;
    }

    static int getNextID() {
        return nextID;
    }

    public void printGeneralInformation() {
        StringBuilder sb = new StringBuilder(200);
        // sb.append("LOCAL INFORMATION: \n");
        sb.append("StatisticTrackerImpl with ID: ");
        sb.append(this.ID);
        sb.append('\n');
        sb.append("Tracks list of: ");
        sb.append(this.type.getTypeName());
        sb.append('\n');
        sb.append("Current used Size: ");
        sb.append(list.size());
        sb.append('\n');
        sb.append("Current Capacity: ");
        sb.append(list.getCurrentCapacity());
        sb.append('\n');
        sb.append("Current load factor: ");
        sb.append(list.getCurrentLoadFactor());
        sb.append('\n');
        sb.append("Modifications made so far: ");
        sb.append(modifications);
        sb.append('\n');
        sb.append("Operation Usage: \n");
        sb.append(Statistics.getPrettyOpMapContentString(localOpMap));
        sb.append("END of Summary! \n\n");
        System.out.print(sb.toString());
    }

    public String[] getOpDataLines(final char dataSeparator) {
        String[] dataArr = new String[localOpMap.size()];
        Iterator<Entry<Operation, AtomicInteger>> itr = localOpMap.entrySet().iterator();

// StringBuilder sb = new StringBuilder(30);
// sb.append("Operation Occurrences");
// sb.append(dataSeparator);
// sb.append("Num");
// dataArr[0] = sb.toString();
        StringBuilder sb = new StringBuilder(50);

        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();

            sb.append(entry.getKey().name());
            sb.append(dataSeparator);
            sb.append(' ');
            sb.append(entry.getValue().get());
            dataArr[n++] = sb.toString();
            sb = new StringBuilder(50);
            // dataArr[n++] = entry.getKey().name() + separator + " " + entry.getValue().get();
        }
        return dataArr;
    }

    private static void addOpTo(HashMap<Operation, AtomicInteger> map, Operation op) {
        AtomicInteger curr = map.getOrDefault(op, null);
        if (curr == null) {
            map.put(op, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

    private static void addTypeTo(HashMap<Type, AtomicInteger> map, Type t) {
        AtomicInteger curr = map.getOrDefault(t, null);
        if (curr == null) {
            map.put(t, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

}
