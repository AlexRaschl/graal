package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class SpecifiedArrayListImpl<E> extends SpecifiedArrayList<E> {

    // DONE CHECK if NULL Insertion and NULL removal is needed. //Most likely Yes

    private final static int INITIAL_CAPACITY = 16;
    private final static int GROW_FACTOR = 2;

    private final static int CAPACITY_GROWING_THRESHOLD = 32;

    private int size;
    private Object elems[];

    /**
     * Factory methods
     */
    public static <E> SpecifiedArrayList<E> createNew() {
        return new SpecifiedArrayListImpl<>();
    }

    public static <E> SpecifiedArrayList<E> createNew(int initalCapacity) {
        return new SpecifiedArrayListImpl<>(initalCapacity);
    }

    public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
        return new SpecifiedArrayListImpl<>(c);
    }

    /**
     * Creates an instance of SpecifiedArrayListImpl with a given initial capacity.
     *
     * @param initialCapacity Capacity the list will have from beginning
     */
    public SpecifiedArrayListImpl(int initialCapacity) {
        if (initialCapacity >= 0) {
            this.size = 0;
            this.elems = new Object[initialCapacity];
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates an instance of SpecifiedArrayListImpl.
     *
     */
    public SpecifiedArrayListImpl() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Creates an instance of SpecifiedArrayListImpl that holds the elements of the given collection.
     *
     * @param collection
     */
    public SpecifiedArrayListImpl(Collection<E> collection) {
        this.size = collection.size();
        this.elems = Arrays.copyOf(collection.toArray(), collection.size());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elems, size);
    }

    @Override
    public boolean add(E e) {
        growIfNeeded();
        elems[size++] = e;
        return true;
    }

    @Override
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

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c) {
            if (!contains(obj))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        int cSize = c.size();
        if (cSize == 0)
            return false;

        ensureCapacity(size + cSize);// Useful if c is large

        System.arraycopy(c.toArray(), 0, elems, size, cSize);
        size = size + cSize;
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeCollection(c, false);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeCollection(c, true);
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elems[i] = null;
        }

        // elems = new Object[INITIAL_CAPACITY];
        size = 0;
        // System.gc();
    }

    @Override
    public E get(int index) {
        checkBoundaries(index);
        return castUnchecked(elems[index]);
    }

    @Override
    public E set(int index, E element) {
        checkBoundaries(index);
        Object oldElem = elems[index];
        elems[index] = element;
        return castUnchecked(oldElem);
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
        growIfNeeded();
        System.arraycopy(elems, index, elems, index + 1, size - index);
        elems[index] = element;
        size++;
    }

    @Override
    public E remove(int index) {
        checkBoundaries(index);
        Object oldElem = elems[index];
        System.arraycopy(elems, index + 1, elems, index, size - index - 1);
        size--;
        return castUnchecked(oldElem);
    }

    @Override
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

    @Override
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

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
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
     * DONE CHECK if ConcurrentModificationExceptions are needed for this specific task (Most Likely
     * this will be needed and I will need to make it Fail Fast) -> NOPE
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
            SpecifiedArrayListImpl.this.set(lastRet, e);

        }

        public void add(E e) {
            SpecifiedArrayListImpl.this.add(cursor, e);
            // TODO check if LastRet = -1 needed
            lastRet = -1;
            cursor++;
        }

    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // DONE CHECK IF NEEDED -> Most likely not
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

    @SuppressWarnings("unchecked")
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

    @Override
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

    @Override
    public void trimToSize() {
        if (elems.length >= size)
            elems = Arrays.copyOf(elems, size);
    }

    //
    //
    // -------------------POTECTED METHODS------------------------
    //
    //

    protected double getLoadFactor() {
        return ((double) size / elems.length);
    }

    protected int getCapacity() {
        return elems.length;
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

    private void trimIfUseful() {
        int threshold = (elems.length / GROW_FACTOR) + 1; // TODO Find more efficient strategy
        if (threshold > size && threshold < elems.length) {
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

    private boolean removeCollection(Collection<?> c, boolean isRetained) {
        int removed = 0;
        int w = 0, i = 0;

        for (; i < size; i++) {
            if (c.contains(elems[i]) == isRetained) {
                elems[w] = elems[i];
                w++;
            } else {
                removed++;
            }
        }
        if (i != size) {
            System.arraycopy(elems, i, elems, w, size - i);
            w += size - i;
        }
        if (w != size) {
            for (int j = w; j < size; j++)
                elems[j] = null;
            size = w;
        }
        trimIfUseful();
        return removed != 0;
    }

    @SuppressWarnings("unchecked")
    private E castUnchecked(Object obj) {
        return (E) obj;
    }

}