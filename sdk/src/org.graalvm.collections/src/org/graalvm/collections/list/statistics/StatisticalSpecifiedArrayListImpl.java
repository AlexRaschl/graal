package org.graalvm.collections.list.statistics;

import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ADD_ALL;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ADD_ALL_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ADD_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ADD_OBJ;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CLEAR;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CONTAINS;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CONTAINS_ALL;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CREATE_LIST_ITR;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CREATE_LIST_ITR_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CSTR_CAP;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CSTR_COLL;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.CSTR_STD;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.EMPTY;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ENSURE_CAP;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.GET_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.GROW;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.INDEX_OF;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.INDEX_OF_LAST;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.ITERATOR;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.REMOVE_ALL;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.REMOVE_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.REMOVE_OBJ;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.RETAIN_ALL;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.SET_INDEXED;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.SIZE;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.SUBLIST;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.TO_ARRAY;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.TRIM_TO_SIZE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.SpecifiedArrayListImpl;
import org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation;

/**
 * This is an enhancement of the SpecifiedArrayList used to gather information during runtime. It
 * uses a StatisticTracker object to store the generated Information. This object can be used to
 * extract useful information about the SpecifiedArrayList like
 * <li>Size</li>
 * <li>Type Distribution</li>
 * <li>Load Factor</li>
 * <li>Distribution of Operators</li>
 *
 *
 * DONE Check if tracking also useful for Itr/ListItr -> Not necessary
 *
 * Done Check if the Interface is needed -> Nope
 *
 * @author Alex R.
 */

public class StatisticalSpecifiedArrayListImpl<E> extends SpecifiedArrayList<E> implements StatisticalCollection {

    private static final long serialVersionUID = 2325200269334451909L;

    private final static boolean TRACKS_ALL = false;
    private final static HashSet<String> trackedSites = new HashSet<>(10);

    /** Static block to set up Tracked Classes */
    static {
        trackedSites.add("org.graalvm.collections.test.list.statistics.StatisticsSimpleTest");
        trackedSites.add("org.graalvm.collections.test.list.statistics.ReplacementTest");
        //
        // trackedSites.add("org.graalvm.compiler.asm.Label");
        // trackedSites.add("org.graalvm.compiler.core.gen.NodeLIRBuilder");
        // trackedSites.add("org.graalvm.compiler.core.common.FieldsScanner");
        trackedSites.add("org.graalvm.compiler.nodes.IfNode");
        trackedSites.add("org.graalvm.compiler.nodes.InliningLog");
    }

    /**
     * Factory methods
     */
    public static <E> SpecifiedArrayList<E> createNew() {
        StatisticalSpecifiedArrayListImpl<E> list = new StatisticalSpecifiedArrayListImpl<>();
        return list;

    }

    public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
        StatisticalSpecifiedArrayListImpl<E> list = new StatisticalSpecifiedArrayListImpl<>(initalCapacity);
        return list;
    }

    public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
        return new StatisticalSpecifiedArrayListImpl<>(c);
    }

    private StatisticTrackerImpl tracker = null;

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl with a given initial capacity.
     *
     * @param initialCapacity Capacity the list will have from beginning
     */
    public StatisticalSpecifiedArrayListImpl(int initialCapacity) {
        super(initialCapacity);
        if (isTracked()) {
            tracker = new StatisticTrackerImpl(this);
            setAllocationSite();
            tracker.countOP(CSTR_CAP);
        }
    }

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl.
     *
     */
    public StatisticalSpecifiedArrayListImpl() {
        super();
        if (isTracked()) {
            tracker = new StatisticTrackerImpl(this);
            tracker.countOP(CSTR_STD);
            setAllocationSite();
        }
    }

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl that holds the elements of the given
     * collection.
     *
     * @param collection
     */
    public StatisticalSpecifiedArrayListImpl(Collection<E> collection) {
        super(collection);
        if (isTracked()) {
            tracker = new StatisticTrackerImpl(this);
            if (collection.size() != 0)
                tracker.setType(checkNull(collection.iterator().next()));
            tracker.countOP(CSTR_COLL);
            setAllocationSite();
        }
    }

// @Override
// protected void grow() {
// countIfTracked(GROW);
// super.grow();
// }

    @Override
    protected void growAL(int minCapacity) {
        countIfTracked(GROW);
        super.growAL(minCapacity);
    }

    @Override
    public int size() {
        countIfTracked(SIZE);
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        countIfTracked(EMPTY);
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        countIfTracked(CONTAINS);
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        countIfTracked(TO_ARRAY);
        return super.toArray();
    }

    @Override
    public boolean add(E e) {
        if (tracker != null) {
            Class<?> clazz = checkNull(e);
            tracker.countOP(ADD_OBJ);
            tracker.addTypeOpToMap(ADD_OBJ, clazz);
            tracker.modified();
            tracker.setType(clazz);
        }
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        if (tracker != null) {
            tracker.countOP(REMOVE_OBJ);
            tracker.modified();
            tracker.addTypeOpToMap(REMOVE_OBJ, o == null ? NoObject.class : o.getClass());
        }
        return super.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        countIfTracked(CONTAINS_ALL);
        return super.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean res = super.addAll(c);

        if (tracker != null) {
            tracker.countOP(ADD_ALL);

            if (c.size() != 0) {
                Class<?> clazz;
                clazz = checkNull(c.iterator().next());
                tracker.addTypeOpToMap(ADD_ALL, clazz);
            }

            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean res = super.addAll(index, c);

        if (tracker != null) {
            tracker.countOP(ADD_ALL_INDEXED);
            if (c.size() != 0) {
                Class<?> clazz;
                clazz = checkNull(c.iterator().next());
                tracker.addTypeOpToMap(ADD_ALL, clazz);
            }

            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = super.removeAll(c);

        if (tracker != null) {
            tracker.countOP(REMOVE_ALL);
            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean res = super.retainAll(c);

        if (tracker != null) {
            tracker.countOP(RETAIN_ALL);
            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public void clear() {
        countIfTracked(CLEAR);
        super.clear();
    }

    @Override
    public E get(int index) {
        E e = super.get(index);

        if (tracker != null) {
            tracker.countOP(GET_INDEXED);
            tracker.addTypeOpToMap(GET_INDEXED, checkNull(e));
        }
        return e;
    }

    @Override
    public E set(int index, E element) {
        if (tracker != null) {
            Class<?> clazz = checkNull(element);
            tracker.countOP(SET_INDEXED);
            tracker.modified();
            tracker.setType(clazz);
            tracker.addTypeOpToMap(SET_INDEXED, clazz);
        }

        return super.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        if (tracker != null) {
            tracker.countOP(ADD_INDEXED);
            tracker.modified();
            tracker.setType(checkNull(element));
        }
        super.add(index, element);
    }

    @Override
    public E remove(int index) {
        if (tracker != null) {
            tracker.countOP(REMOVE_INDEXED);
            tracker.modified();
        }
        return super.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        countIfTracked(INDEX_OF);
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        countIfTracked(INDEX_OF_LAST);
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        countIfTracked(CREATE_LIST_ITR);
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        countIfTracked(CREATE_LIST_ITR_INDEXED);
        return super.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        countIfTracked(SUBLIST);
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Iterator<E> iterator() {
        countIfTracked(ITERATOR);
        return super.iterator();
    }

    @Override
    public void ensureCapacity(int capacity) {
        countIfTracked(ENSURE_CAP);
        super.ensureCapacity(capacity);
    }

    @Override
    public void trimToSize() {
        countIfTracked(TRIM_TO_SIZE);
        super.trimToSize();
    }

    public double getCurrentLoadFactor() {
        return super.getLoadFactor();
    }

    public int getCurrentCapacity() {
        return super.getCapacity();
    }

    private Class<?> checkNull(E e) {
        if (e == null) {
            return NoObject.class;
        } else {
            return e.getClass();
        }
    }

    private void setAllocationSite() {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] elems = e.getStackTrace();

            if (elems.length >= 2 && !elems[1].getMethodName().equals("<init>")) {
                tracker.setAllocSiteElem(elems[1]);
            } else {
                if (!elems[2].getMethodName().equals("createNew")) {
                    tracker.setAllocSiteElem(elems[2]);
                } else {
                    tracker.setAllocSiteElem(elems[3]);
                }
            }

        }
    }

    private String getAllocationSiteName() {
        try {
            throw new Exception();
        } catch (Exception e) {
            // e.printStackTrace();
            StackTraceElement[] elems = e.getStackTrace();

            if (elems.length > 2 && !elems[2].getMethodName().equals("<init>")) {
                return elems[2].getClassName();
            } else {
                if (!elems[3].getMethodName().equals("createNew")) {
                    return elems[3].getClassName();
                } else {
                    return elems[4].getClassName();
                }

            }
        }
    }

    private boolean isTracked() {
        return TRACKS_ALL || trackedSites.contains(getAllocationSiteName());
    }

    private void countIfTracked(Operation op) {
        if (tracker != null)
            tracker.countOP(op);
    }
    /*
     * TODO check if setType also needed in ListIterator/Iterator funcitons
     */

}
