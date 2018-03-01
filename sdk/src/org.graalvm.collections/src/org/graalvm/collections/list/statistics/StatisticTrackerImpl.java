package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

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

    public StatisticTrackerImpl(StatisticalSpecifiedArrayListImpl list) {
        ID = nextID++;
        this.localOpMap = new HashMap<>(Operation.values().length);
        this.modifications = 0;
        this.list = list;

        Statistics.addTracker(this);
    }

    public void setType(Class<?> c) {
        if (!isAdded) {
            this.type = (Type) c;
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

    static int getNextID() {
        return nextID;
    }

    public void printGeneralInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("LOCAL INFORMATION: \n");
        sb.append("of StatisticTrackerImpl with ID: " + this.ID + "\n");
        sb.append("Tracks list of: " + this.type.getTypeName() + "\n");
        sb.append("Current used Size: " + list.size() + "\n");
        sb.append("Current Capacity: " + list.getCurrentCapacity() + "\n");
        sb.append("Current load factor: " + list.getCurrentLoadFactor() + "\n");
        sb.append("Modifications made so far: " + modifications + "\n");
        sb.append("Operation Usage: \n");
        sb.append(Statistics.getPrettyOpMapContentString(localOpMap));
        sb.append("END of Summary! \n\n");
        System.out.print(sb.toString());
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
