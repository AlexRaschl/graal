package org.graalvm.collections.list.statistics;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticTrackerImpl {
    // _____GLOBAL FIELDS______
    // ID
    private static int nextID = 0;
    // Size and Capacity
    private static int totalSizeAtEnd = 0;
    private static int capacityAtEnd = 0;

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

    private static final HashMap<Operation, AtomicInteger> globalOpMap = new HashMap<>(Operation.values().length);

    // Type Distribution
    private static final HashMap<Class<?>, AtomicInteger> globalTypeMap = new HashMap<>();

    // _____LOCAL FIELDS______
    private final int ID;
    private final HashMap<Operation, AtomicInteger> localOpMap;
    private final Class<?> typeClass;
    private int modifications;

    public StatisticTrackerImpl(Class<?> typeClass) {
        ID = nextID++;
        this.localOpMap = new HashMap<>(Operation.values().length);
        this.typeClass = typeClass;
        this.modifications = 0;
        addTypeTo(globalTypeMap, typeClass);
    }

    public void countOP(Operation op) {
        addOpTo(globalOpMap, op);
        addOpTo(localOpMap, op);
    }

    public void modified() {
        modifications++;
    }

    private static void addOpTo(HashMap<Operation, AtomicInteger> map, Operation op) {
        AtomicInteger curr = map.getOrDefault(op, null);
        if (curr == null) {
            map.put(op, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

    private static void addTypeTo(HashMap<Class<?>, AtomicInteger> map, Class<?> c) {
        AtomicInteger curr = map.getOrDefault(c, null);
        if (curr == null) {
            map.put(c, new AtomicInteger(1));
        } else {
            curr.getAndIncrement();
        }
    }

}
