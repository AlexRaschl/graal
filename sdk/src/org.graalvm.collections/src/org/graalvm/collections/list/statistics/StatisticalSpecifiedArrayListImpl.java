package org.graalvm.collections.list.statistics;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.graalvm.collections.list.SpecifiedArrayListImpl;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.*;

public class StatisticalSpecifiedArrayListImpl<E> extends SpecifiedArrayListImpl<E> implements StatisticalCollection, StatisticalSpecifiedArrayList<E> {

    // TODO Check if Reflection works -> NO -> find solution -> kinda cheeky solution found and
    // implemented -> find better one
    //
    // private final StatisticTrackerImpl tracker = new StatisticTrackerImpl(((ParameterizedType)
    // getClass().getGenericSuperclass()).getActualTypeArguments()[0], this);
    private final StatisticTrackerImpl tracker = new StatisticTrackerImpl(this);

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl with a given initial capacity.
     *
     * @param initialCapacity Capacity the list will have from beginning
     */
    public StatisticalSpecifiedArrayListImpl(int initialCapacity) {
        super(initialCapacity);
        tracker.countOP(CSTR_CAP);
    }

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl.
     *
     */
    public StatisticalSpecifiedArrayListImpl() {
        super();
        tracker.countOP(CSTR_STD);
    }

    /**
     * Creates an instance of StatisticalSpecifiedArrayListImpl that holds the elements of the given
     * collection.
     *
     * @param collection
     */
    public StatisticalSpecifiedArrayListImpl(Collection<E> collection) {
        super(collection);
        if (collection.size() != 0)
            tracker.setType(collection.iterator().next().getClass());
        tracker.countOP(CSTR_COLL);
    }

    @Override
    public int size() {
        tracker.countOP(SIZE);
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        tracker.countOP(EMPTY);
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        tracker.countOP(CONTAINS);
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        tracker.countOP(TO_ARRAY);
        return super.toArray();
    }

    @Override
    public boolean add(E e) {
        tracker.countOP(ADD_OBJ);
        tracker.modified();
        tracker.setType(e.getClass());
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        tracker.countOP(REMOVE_OBJ);
        tracker.modified();
        return super.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        tracker.countOP(CONTAINS_ALL);
        return super.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        tracker.countOP(ADD_ALL);
        boolean res = super.addAll(c);
        if (c.size() != 0)
            tracker.setType(c.iterator().next().getClass());

        if (res)
            tracker.modified();
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        tracker.countOP(ADD_ALL_INDEXED);
        if (c.size() != 0)
            tracker.setType(c.iterator().next().getClass());

        boolean res = super.addAll(index, c);
        if (res)
            tracker.modified();
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        tracker.countOP(REMOVE_ALL);
        boolean res = super.removeAll(c);
        if (res)
            tracker.modified();
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        tracker.countOP(RETAIN_ALL);
        boolean res = super.retainAll(c);
        if (res)
            tracker.modified();
        return res;
    }

    @Override
    public void clear() {
        tracker.countOP(CLEAR);
        super.clear();
    }

    @Override
    public E get(int index) {
        tracker.countOP(GET_INDEXED);
        return super.get(index);
    }

    @Override
    public E set(int index, E element) {
        tracker.countOP(SET_INDEXED);
        tracker.modified();
        tracker.setType(element.getClass());
        return super.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        tracker.countOP(ADD_INDEXED);
        tracker.modified();
        tracker.setType(element.getClass());
        super.add(index, element);
    }

    @Override
    public E remove(int index) {
        tracker.countOP(REMOVE_INDEXED);
        tracker.modified();
        return super.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        tracker.countOP(INDEX_OF);
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        tracker.countOP(INDEX_OF_LAST);
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        tracker.countOP(CREATE_LIST_ITR);
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        tracker.countOP(CREATE_LIST_ITR_INDEXED);
        return super.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        tracker.countOP(SUBLIST);
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Iterator<E> iterator() {
        tracker.countOP(ITERATOR);
        return super.iterator();
    }

    @Override
    public void ensureCapacity(int capacity) {
        tracker.countOP(ENSURE_CAP);
        super.ensureCapacity(capacity);
    }

    @Override
    public void trimToSize() {
        tracker.countOP(TRIM_TO_SIZE);
        super.trimToSize();
    }

    public double getCurrentLoadFactor() {
        return super.getLoadFactor();
    }

    public int getCurrentCapacity() {
        return super.getCapacity();
    }

}
