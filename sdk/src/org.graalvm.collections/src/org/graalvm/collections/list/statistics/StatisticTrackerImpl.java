package org.graalvm.collections.list.statistics;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.omg.CORBA.DATA_CONVERSION;

public class StatisticTrackerImpl implements StatisticTracker {
    // _____GLOBAL FIELDS______
    // ID
    private static int nextID = 1;

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
        SIZE,
        GROW
    }

    // Operations that are tracked more precisely
    private static final EnumSet<Operation> SPECIAL_OPS = EnumSet.of(Operation.ADD_OBJ, Operation.REMOVE_OBJ, Operation.GET_INDEXED, Operation.SET_INDEXED);

    // _____LOCAL FIELDS______
    private final int ID;

    // Map of Operations performed on the list
    private final HashMap<Operation, AtomicInteger> localOpMap;

    // Maps for tracking Operation Distribution based on inserted Types and Subtypes. Only Operations
    // listed in SPECIAL_OPS are tracked
    private final HashMap<Operation, HashMap<Type, AtomicInteger>> localTypeOpMap;

    // Type
    private Type type;
    private boolean isAdded = false;

    // Number of times the content of the list has changed NOTE: Changes made by Iterators are not
    // tracked
    private int modifications;

    @SuppressWarnings("rawtypes") private final StatisticalSpecifiedArrayListImpl list; // No Use of get, add, ....

    @SuppressWarnings("rawtypes")
    public StatisticTrackerImpl(StatisticalSpecifiedArrayListImpl list) {
        ID = nextID++;
        this.localOpMap = new HashMap<>(Operation.values().length);
        this.localTypeOpMap = new HashMap<>();
        this.modifications = 0;
        this.list = list;

        Statistics.addTracker(this); // TODO Synchronize
    }

    public void countOP(Operation op) {
        synchronized (Statistics.globalOpMap) {
            addOpTo(Statistics.globalOpMap, op);
        }
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

    public String[] getOpDataLines(final char dataSeparator) {
        final String[] dataArr = new String[localOpMap.size()];
        final Iterator<Entry<Operation, AtomicInteger>> itr = localOpMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder(50);

        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, AtomicInteger> entry = itr.next();
            sb.append(this.ID);
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

    public String[] getTypeOpDataLines(char dataSeparator) {
        final String[][] dataArr = new String[localTypeOpMap.size()][];
        initStrings(dataArr);
        final Iterator<Entry<Operation, HashMap<Type, AtomicInteger>>> itr = localTypeOpMap.entrySet().iterator();
        int n = 0;
        while (itr.hasNext()) {
            Entry<Operation, HashMap<Type, AtomicInteger>> entry = itr.next();
            Operation op = entry.getKey();
            HashMap<Type, AtomicInteger> map = entry.getValue();
            dataArr[n] = new String[map.size()];
            putData(dataArr[n], op, map, this.ID, dataSeparator);
            n++;
        }
        return getFlatStringArray(dataArr);
    }

    private static void initStrings(String[][] dataArr) {
        for (int r = 0; r < dataArr.length; r++) {
            dataArr[r] = new String[0];
        }
    }

    private static void putData(String[] dataLines, Operation op, HashMap<Type, AtomicInteger> map, int ID, char dataSeparator) {

        final Iterator<Entry<Type, AtomicInteger>> itr = map.entrySet().iterator();

        StringBuilder sb = new StringBuilder(50);
        int n = 0;
        while (itr.hasNext()) {
            Entry<Type, AtomicInteger> entry = itr.next();
            sb.append(ID);
            sb.append(dataSeparator);
            sb.append(op.name());
            sb.append(dataSeparator);
            sb.append(entry.getKey().getTypeName());
            sb.append(dataSeparator);
            sb.append(entry.getValue().get());
            dataLines[n++] = sb.toString();
            sb = new StringBuilder(50);

        }

    }

    private static String[] getFlatStringArray(String[][] dataArr) {
        int dim1 = dataArr.length;
        if (dim1 == 0)
            return new String[0];
        if (dataArr[0] == null) {
            return null; // TODO handle case
        }

        int dim2 = dataArr[0].length;

        final String[] result = new String[dataArr.length * dataArr[0].length];
        int i = 0;
        for (int r = 0; r < dim1; r++) {
            for (int c = 0; c < dim2; c++) {
                result[i++] = dataArr[r][c];
            }
        }
        return result;
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

    static int getNextID() {
        return nextID;
    }

    void setType(Class<?> c) {
        if (!isAdded) {
            synchronized (Statistics.globalTypeMap) {
                this.type = c;
                addTypeTo(Statistics.globalTypeMap, type);
                isAdded = true;
            }
        }
    }

    void addTypeOpToMap(Operation op, Type t) {

        if (!SPECIAL_OPS.contains(op))
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

    public StackTraceElement getAllocationSite() {
        // TODO Auto-generated method stub
        return null;
    }

}
