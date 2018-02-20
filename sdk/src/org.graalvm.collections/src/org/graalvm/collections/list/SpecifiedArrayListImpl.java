package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class SpecifiedArrayListImpl<E> implements SpecifiedArrayList<E> {

    final static int INITIAL_CAPACITY = 16;
    final static int GROW_FACTOR = 2;

    private int size;
    private Object elems[];

    /**
     * TODO JAVADOC
     *
     * @param initialCapacity
     */
    protected SpecifiedArrayListImpl(int initialCapacity) {
        this.size = 0;
        this.elems = new Object[initialCapacity];
    }

    /**
     * TODO JAVADOC
     *
     */
    public SpecifiedArrayListImpl() {
        this(INITIAL_CAPACITY);
    }

    /**
     * TODO JAVADOC
     *
     * @param collection
     */
    public SpecifiedArrayListImpl(Collection<E> collection) {
        this.size = collection.size();
        this.elems = Arrays.copyOf(collection.toArray(), collection.size());
    }

    /**
     * Returns the current size of the list
     *
     * @return the number of elements in this list
     *
     */
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns false if o == null or true if any of the elements in this list are equal to the Object o
     *
     * @param o
     * @return true if o is not Null and o is in list
     */
    public boolean contains(Object o) {
        if (o == null)
            return false;

        for (Object e : elems) { // TODO REMOVE possible code duplicate with indexOf
            if (e.equals(o))
                return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public Object[] toArray() {
        return Arrays.copyOfRange(elems, 0, size);

    }

    public boolean add(E e) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Increases the arraySize by multiplying the array length by the current GROW_FACTOR
     */
    private void grow() {

    }

    /**
     * Removes an Object form the list if it exists otherwise returns false
     *
     * @return True if element has been found and removed else false
     */
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public E get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public E set(int index, E element) {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(int index, E element) {
        // TODO Auto-generated method stub

    }

    public E remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public int indexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public ListIterator<E> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public ListIterator<E> listIterator(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<E> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<E> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

}
