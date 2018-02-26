package org.graalvm.collections.list.statistics;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.graalvm.collections.list.SpecifiedArrayListImpl;
import static org.graalvm.collections.list.statistics.StatisticTrackerImpl.Operation.*;

public class StatisticalSpecifiedArrayListImpl<E> extends SpecifiedArrayListImpl<E> implements StatisticalSpecifiedArrayList<E> {

    // TODO Reflection
    private final StatisticTrackerImpl tracker = new StatisticTrackerImpl((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);

    /**
     * TODO DOC
     *
     * @param initialCapacity
     */
    protected StatisticalSpecifiedArrayListImpl(int initialCapacity) {
        super(initialCapacity);
        tracker.countOP(CSTR_CAP);
    }

    /**
     * TODO DOC
     *
     */
    public StatisticalSpecifiedArrayListImpl() {
        super(INITIAL_CAPACITY);
        tracker.countOP(CSTR_STD);
    }

    /**
     * TODO DOC
     *
     * @param collection
     */
    public StatisticalSpecifiedArrayListImpl(Collection<E> collection) {
        super(collection);
        tracker.countOP(CSTR_COLL);
    }

    @Override
    public int size() {
        return super.size(); // TODO check if count needed
    }

    @Override
    public boolean isEmpty() {
        // TODO check if count needed
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
        boolean res = super.containsAll(c);
        if (res)
            tracker.modified();
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        tracker.countOP(ADD_ALL_INDEXED);

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
        // TODO check if count needed
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
        return super.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        tracker.countOP(ADD_INDEXED);
        tracker.modified();
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
        // TODO check if boolean Retval is needed for tracker.modified, would destroy API
        super.ensureCapacity(capacity);
    }

    @Override
    public void trimToSize() {
        tracker.countOP(TRIM_TO_SIZE);
        // TODO check if boolean Retval is needed for tracker.modified, would destroy API
        super.trimToSize();
    }

}
