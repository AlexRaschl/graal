package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public final class SpecifiedArrayListImpl<E> implements SpecifiedArrayList<E> {

    // TODO CHECK if NULL Insertion and NULL removal is needed. //Most likely Yes

    private final static int INITIAL_CAPACITY = 16;
    private final static int GROW_FACTOR = 2;

    private final static int CAPACITY_GROWING_THRESHOLD = 32;

    // private final static Object[] EMPTY_ARRAY = {};

    private int size;
    private Object elems[];

    /**
     * TODO DOC
     *
     * @param initialCapacity
     */
    protected SpecifiedArrayListImpl(int initialCapacity) {
        if (size >= 0) {
            this.size = 0;
            this.elems = new Object[initialCapacity];
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * TODO DOC
     *
     */
    public SpecifiedArrayListImpl() {
        this(INITIAL_CAPACITY);
    }

    /**
     * TODO DOC
     *
     * @param collection
     */
    public SpecifiedArrayListImpl(Collection<E> collection) {
        this.size = collection.size();
        this.elems = Arrays.copyOf(collection.toArray(), collection.size());
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    public Object[] toArray() {
        return Arrays.copyOf(elems, size);
    }

    public boolean add(E e) {
        growIfNeeded();
        elems[size++] = e;
        return true;
    }

    public boolean remove(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elems[i] == null) {
                    fastRemove(i);
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elems[i])) {
                    fastRemove(i);
                    return true;
                }

            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object obj : c) {
            if (!contains(obj))
                return false;
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        int cSize = c.size();
        if (cSize == 0)
            return false;

        ensureCapacity(size + cSize);// Useful if c is large

        System.arraycopy(c.toArray(), 0, elems, size, cSize);
        size = size + cSize;
        return true;
    }

    // TODO CHECK IF NEEDED
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        int removed = 0;

        for (Object obj : c) {
            if (remove(obj))
                removed++;
        }

        trimIfUseful(removed);
        return removed != 0;
    }

    public boolean retainAll(Collection<?> c) {
        int removed = 0;
        for (int i = 0; i < size; i++) {
            if (!c.contains(elems[i])) {
                fastRemove(i);
                removed++;
            }
        }
        trimIfUseful(removed);
        return removed != 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elems[i] = null;
        }
        size = 0;
        trim(INITIAL_CAPACITY); // TODO CHECK IF SIZE shrinking is useful;
        System.gc();
    }

    public E get(int index) {
        checkBoundaries(index);
        return castUnchecked(elems[index]);
    }

    public E set(int index, E element) {
        checkBoundaries(index);
        Object oldElem = elems[index];
        elems[index] = element;
        return castUnchecked(oldElem);
    }

    public void add(int index, E element) {
        checkBoundaries(index);
        growIfNeeded();
        System.arraycopy(elems, index, elems, index + 1, size - index);
        elems[index] = element;
        size++;
    }

    public E remove(int index) {
        checkBoundaries(index);
        Object oldElem = elems[index];
        System.arraycopy(elems, index + 1, elems, index, size - index + 1);
        size--;
        return castUnchecked(oldElem);
    }

    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elems[i] == null)
                    return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elems[i]))
                    return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elems[i] == null)
                    return i;
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elems[i]))
                    return i;
            }
        }
        return -1;
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    public ListIterator<E> listIterator(int index) {
        return new ListItr(index);
    }

    private class Itr implements Iterator<E> {

        protected int cursor;
        protected int lastRet;

        Itr() {
            cursor = 0;
            lastRet = -1;
        }

        public boolean hasNext() {
            return cursor < size;
        }

        public E next() {

            if (cursor >= size)
                throw new NoSuchElementException();
            Object elem = elems[cursor];
            lastRet = cursor++;
            return castUnchecked(elem);
        }

        /** Moved this here because also supported by Iterator in ArrayList */
        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException("Set or add Operation has been already excecuted!");

            SpecifiedArrayListImpl.this.remove(lastRet);
            cursor = lastRet;
            lastRet = -1;
        }

    }

    /**
     * Not Fail Fast!!!!!
     *
     * TODO CHECK if ConcurrentModificationExceptions are needed for this specific task (Most Likely
     * this will be needed and I will need to make it Fail Fast)
     **/
    private class ListItr extends Itr implements ListIterator<E> {

        ListItr(int startIndex) {
            super();
            checkBoundaries(startIndex);
            cursor = startIndex;
        }

        public boolean hasPrevious() {
            return cursor > 0;
        }

        public E previous() {
            if (cursor < 0)
                throw new NoSuchElementException();
            Object[] elemsLocal = SpecifiedArrayListImpl.this.elems;
            lastRet = --cursor;
            return castUnchecked(elemsLocal[cursor]);
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void set(E e) {
            if (lastRet == -1)
                throw new IllegalStateException("Remove or add Operation has been already excecuted!");
            SpecifiedArrayListImpl.this.set(cursor, e);

        }

        public void add(E e) {
            SpecifiedArrayListImpl.this.add(cursor, e);
            cursor++;
        }

    }

    // TODO CHECK IF NEEDED
    public List<E> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elems);
        result = prime * result + size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpecifiedArrayListImpl<E> other = (SpecifiedArrayListImpl<E>) obj;
        if (!Arrays.equals(elems, other.elems))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SpecifiedArrayListImpl [size=" + size + ", elems=" + Arrays.toString(elems) + "]";
    }

    public void ensureCapacity(int capacity) {
        int curCapacity = elems.length;
        if (curCapacity < capacity) {
            // If we ensure that there is enough capacity it will be most likely that not much more elements
            // than this capacity will be added in the near future.
            // TODO CHECK IF USEFUL IN PROJECT
            int newLength;
            if (capacity <= CAPACITY_GROWING_THRESHOLD) {
                newLength = capacity;
            } else {
                newLength = capacity + INITIAL_CAPACITY;
            }
            elems = Arrays.copyOf(elems, newLength);
        }
    }

    public void trimToSize() {
        if (elems.length >= size)
            elems = Arrays.copyOf(elems, size);
    }

    //
    //
    // -------------------PRIVATE METHODS------------------------
    //
    //

    private void checkBoundaries(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

    }

    private void trimIfUseful(int removed) {
        int threshold = (elems.length / GROW_FACTOR) + 1; // TODO Find more efficient strategy
        if (removed >= threshold) {
            trim(threshold);
        }
    }

    /**
     * Removes the Object at given index without any checks.
     *
     * @param index index of object to be removed
     */
    private void fastRemove(int index) {
        System.arraycopy(elems, index + 1, elems, index, size - index - 1);
        elems[--size] = null;
    }

    /**
     * Increases the arraySize by multiplying the array length by the current GROW_FACTOR
     */
    private void grow() {
        int newLength = elems.length * GROW_FACTOR;
        elems = Arrays.copyOf(elems, newLength);
    }

    private void growIfNeeded() {
        if (size == elems.length)
            grow();
    }

    /** Performs a "hard" cut with potential data loss */
    private void trim(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        System.arraycopy(elems, 0, elems, 0, capacity);
    }

    @SuppressWarnings("unchecked")
    private E castUnchecked(Object obj) {
        return (E) obj;
    }

}
