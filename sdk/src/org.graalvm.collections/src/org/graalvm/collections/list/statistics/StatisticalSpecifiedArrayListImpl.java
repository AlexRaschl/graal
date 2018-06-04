package org.graalvm.collections.list.statistics;

import static org.graalvm.collections.list.statistics.Statistics.Operation.ADD_ALL;
import static org.graalvm.collections.list.statistics.Statistics.Operation.ADD_ALL_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.ADD_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.ADD_OBJ;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CLEAR;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CLONE;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CONTAINS;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CONTAINS_ALL;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CREATE_LIST_ITR;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CREATE_LIST_ITR_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CSTR_CAP;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CSTR_COLL;
import static org.graalvm.collections.list.statistics.Statistics.Operation.CSTR_STD;
import static org.graalvm.collections.list.statistics.Statistics.Operation.EMPTY;
import static org.graalvm.collections.list.statistics.Statistics.Operation.ENSURE_CAP;
import static org.graalvm.collections.list.statistics.Statistics.Operation.GET_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.GROW;
import static org.graalvm.collections.list.statistics.Statistics.Operation.INDEX_OF;
import static org.graalvm.collections.list.statistics.Statistics.Operation.INDEX_OF_LAST;
import static org.graalvm.collections.list.statistics.Statistics.Operation.ITERATOR;
import static org.graalvm.collections.list.statistics.Statistics.Operation.REMOVE_ALL;
import static org.graalvm.collections.list.statistics.Statistics.Operation.REMOVE_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.REMOVE_OBJ;
import static org.graalvm.collections.list.statistics.Statistics.Operation.RETAIN_ALL;
import static org.graalvm.collections.list.statistics.Statistics.Operation.SET_INDEXED;
import static org.graalvm.collections.list.statistics.Statistics.Operation.SIZE;
import static org.graalvm.collections.list.statistics.Statistics.Operation.SORT;
import static org.graalvm.collections.list.statistics.Statistics.Operation.SPLITERATOR;
import static org.graalvm.collections.list.statistics.Statistics.Operation.SUBLIST;
import static org.graalvm.collections.list.statistics.Statistics.Operation.TO_ARRAY;
import static org.graalvm.collections.list.statistics.Statistics.Operation.TRIM_TO_SIZE;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

import org.graalvm.collections.list.SpecifiedArrayList;

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
    private final boolean isTracked = isTracked();

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

    private StatisticTracker tracker = null;

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl with a given initial capacity.
     *
     * @param initialCapacity Capacity the list will have from beginning
     */
    public StatisticalSpecifiedArrayListImpl(int initialCapacity) {
        super(initialCapacity);

        // isTracked = isTracked();

        if (isTracked) {
            final StackTraceElement allocSite = getAllocationSite();
            if (StatisticConfigs.USE_ALLOC_SITE_TRACKING) {
                tracker = StatisticTracker.initTracker(allocSite);
            } else {
                tracker = new StatisticTrackerImpl(allocSite);
            }
            tracker.countOP(CSTR_CAP);
        }
    }

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl.
     *
     */
    public StatisticalSpecifiedArrayListImpl() {
        super();
        // isTracked = isTracked();
        if (isTracked) {
            final StackTraceElement allocSite = getAllocationSite();
            if (StatisticConfigs.USE_ALLOC_SITE_TRACKING) {
                tracker = StatisticTracker.initTracker(allocSite);
            } else {
                tracker = new StatisticTrackerImpl(allocSite);
            }
            tracker.countOP(CSTR_STD);
            // setAllocationSite();
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
        // isTracked = isTracked();
        if (isTracked) {
            final StackTraceElement allocSite = getAllocationSite();
            if (StatisticConfigs.USE_ALLOC_SITE_TRACKING) {
                tracker = StatisticTracker.initTracker(allocSite);
            } else {
                tracker = new StatisticTrackerImpl(allocSite);
            }
            if (collection.size() != 0)
                tracker.setType(checkNull(collection.iterator().next()));
            tracker.countOP(CSTR_COLL);
            // setAllocationSite();
        }
    }

    @Override
    protected void grow(int minCapacity) {
        countIfTracked(GROW);
        super.grow(minCapacity);
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
        if (isTracked) {
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
        if (isTracked) {
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

        if (isTracked) {
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

        if (isTracked) {
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

        if (isTracked) {
            tracker.countOP(REMOVE_ALL);
            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean res = super.retainAll(c);

        if (isTracked) {
            tracker.countOP(RETAIN_ALL);
            if (res)
                tracker.modified();
        }
        return res;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        if (isTracked) {
            tracker.countOP(SORT);
            tracker.modified();
        }
    }

    @Override
    public Object clone() {
        countIfTracked(CLONE);
        return super.clone();
    }

    @Override
    public Spliterator<E> spliterator() {
        countIfTracked(SPLITERATOR);
        return super.spliterator();
    }

    @Override
    public void clear() {
        countIfTracked(CLEAR);
        super.clear();
    }

    @Override
    public E get(int index) {
        E e = super.get(index);

        if (isTracked) {
            tracker.countOP(GET_INDEXED);
            tracker.addTypeOpToMap(GET_INDEXED, checkNull(e));
        }
        return e;
    }

    @Override
    public E set(int index, E element) {
        if (isTracked) {
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
        if (isTracked) {
            tracker.countOP(ADD_INDEXED);
            tracker.modified();
            tracker.setType(checkNull(element));
        }
        super.add(index, element);
    }

    @Override
    public E remove(int index) {
        if (isTracked) {
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
        countIfTracked(ENSURE_CAP); // Faked by internal calls. Shows more than actual called via public api
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

    public int getCurrentSize() {
        return super.size();
    }

    private Class<?> checkNull(E e) {
        if (e == null) {
            return NoObject.class;
        } else {
            return e.getClass();
        }
    }

// private void setAllocationSite() {
// Exception e = new Exception();
//
// StackTraceElement[] elems = e.getStackTrace();
//
// if (elems.length >= 2 && !elems[1].getMethodName().equals("<init>")) {
// tracker.setAllocSiteElem(elems[1]);
// } else {
// if (!elems[2].getMethodName().equals("createNew")) {
// tracker.setAllocSiteElem(elems[2]);
// } else {
// tracker.setAllocSiteElem(elems[3]);
// }
// }
//
// }

    private static StackTraceElement getAllocationSite() {
        Exception e = new Exception();

        StackTraceElement[] elems = e.getStackTrace();

        if (elems.length >= 2 && !elems[1].getMethodName().equals("<init>")) {
            return elems[1];
        } else {
            if (!elems[2].getMethodName().equals("createNew")) {
                return elems[2];
            } else {
                return elems[3];
            }
        }

    }

    private static String getAllocationSiteName() {

        Exception e = new Exception();

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

    // TODO replace with variable that is set in constructor to remove the getAllocSiteNameNeed and also
    // for performance
    private static boolean isTracked() {
        return StatisticConfigs.TRACKS_ALL || StatisticConfigs.TRACKED_SITES.contains(getAllocationSiteName());
    }

    private void countIfTracked(Statistics.Operation op) {
        if (isTracked)
            tracker.countOP(op);
    }

    // TODO implement better solution than finalize
    @Override
    protected void finalize() throws Throwable {
        if (isTracked) {
            synchronized (tracker) {
                tracker.setCurrentCapacity(getCapacity());
                tracker.setCurrentSize(size());
                tracker.setCurrentLoadFactor(getLoadFactor());
            }
        }
        super.finalize();
    }

    /*
     * TODO check if setType also needed in ListIterator/Iterator funcitons
     */

}
