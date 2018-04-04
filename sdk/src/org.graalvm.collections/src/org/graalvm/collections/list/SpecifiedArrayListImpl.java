package org.graalvm.collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class SpecifiedArrayListImpl<E> extends SpecifiedArrayList<E> {

    private static final boolean USE_AL_STRATEGY = false;

    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = 10; // Capacity after first grow
    private final static int GROW_FACTOR = 2; // Growing factor
    private final static int CAPACITY_GROWING_THRESHOLD = 32; // Unused
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int size;

    // ARRAYLIST IMMITATION Stuff
    private static final Object[] EMPTY_ELEMENTDATA = {};
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    // AL Capacity
    private final static int DEFAULT_CAPACITY = 10;

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
        if (USE_AL_STRATEGY) {

            if (initialCapacity > 0) {
                // this.size = 0;
                this.elementData = new Object[initialCapacity];
            } else if (initialCapacity == 0) {
                this.elementData = EMPTY_ELEMENTDATA;
            } else {
                throw new IllegalArgumentException("Negative Capacity: " + initialCapacity);
            }

        } else {
            if (initialCapacity > 0) {
                this.elementData = new Object[initialCapacity];
            } else if (initialCapacity == 0) {
                this.elementData = EMPTY_ELEMENTDATA;
            } else {
                throw new IllegalArgumentException("Negative Capacity: " + initialCapacity);
            }
        }

    }

    /**
     * Creates an instance of SpecifiedArrayListImpl.
     *
     */
    public SpecifiedArrayListImpl() {
        if (USE_AL_STRATEGY) {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        } else {
            // this(INITIAL_CAPACITY);
            this.size = 0;
            this.elementData = EMPTY_ELEMENTDATA;
        }

    }

    /**
     * Creates an instance of SpecifiedArrayListImpl that holds the elements of the given collection.
     *
     * @param collection
     */
    public SpecifiedArrayListImpl(Collection<? extends E> collection) {
        if (USE_AL_STRATEGY) {
            elementData = collection.toArray();
            if ((size = elementData.length) != 0) {
                // c.toArray might (incorrectly) not return Object[] (see 6260652)
                if (elementData.getClass() != Object[].class)
                    elementData = Arrays.copyOf(elementData, size, Object[].class);
            } else {
                // replace with empty array.
                this.elementData = EMPTY_ELEMENTDATA;
            }
        } else {
            this.size = collection.size();
            if (size != 0) {
                this.elementData = Arrays.copyOf(collection.toArray(), collection.size());
            } else {
                this.elementData = EMPTY_ELEMENTDATA;
            }

        }
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
        return Arrays.copyOf(elementData, size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;

    }

    @Override
    public boolean add(E e) {
        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + 1);
        } else {
            newEnsureCapacity(size + 1);
            // growIfNeeded();
        }
        elementData[size++] = e;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    fastRemove(i);
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) {
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

        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + cSize);// Useful if c is large
        } else {
            newEnsureCapacity(size + cSize);// Useful if c is large
        }

        System.arraycopy(c.toArray(), 0, elementData, size, cSize);
        size = size + cSize;
        return true;
    }

    /*
     * Not optimized for insertions at end use addAll(Collection c) instead (non-Javadoc)
     *
     * @see org.graalvm.collections.list.SpecifiedArrayList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBoundsForAdd(index);

        if (c.size() == 0)
            return false;

        final Object[] arr = c.toArray();
        int nElems = arr.length;

        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + nElems);
        } else {
            newEnsureCapacity(size + nElems);
        }

        System.arraycopy(elementData, index, elementData, index + nElems, size - index);
        System.arraycopy(arr, 0, elementData, index, nElems);

        size += nElems;

        return true;

    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeCollection(c, false);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeCollection(c, true);
    }

    // TODO Could only set size to 0 for fast clear
    @Override
    public void clear() {

        if (USE_AL_STRATEGY) {
            for (int i = 0; i < size; i++) {
                elementData[i] = null;
            }
            size = 0;
        } else {
            elementData = EMPTY_ELEMENTDATA;
            size = 0;
        }

        // elems = new Object[INITIAL_CAPACITY];
        // System.gc();
    }

    @Override
    public E get(int index) {
        checkBoundaries(index);
        return castUnchecked(elementData[index]);
    }

    @Override
    public E set(int index, E element) {
        checkBoundaries(index);
        Object oldElem = elementData[index];
        elementData[index] = element;
        return castUnchecked(oldElem);
    }

    @Override
    public void add(int index, E element) {
        checkBoundsForAdd(index);
        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + 1);
        } else {
            // growIfNeeded();
            newEnsureCapacity(size + 1);
        }

        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    @Override
    public E remove(int index) {
        if (USE_AL_STRATEGY) {
            checkBoundaries(index);

            E oldValue = castUnchecked(elementData[index]);

            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index + 1, elementData, index,
                                numMoved);
            elementData[--size] = null; // clear to let GC do its work

            return oldValue;
        } else {
            checkBoundaries(index);
            Object oldElem = elementData[index];
            System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
            elementData[--size] = null;
            return castUnchecked(oldElem);
        }

    }

    @Override
    public int indexOf(Object o) {

        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null)
                    return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i]))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null)
                    return i;
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elementData[i]))
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
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
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
            Object elem = elementData[cursor];
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
            // checkBoundaries(startIndex);
            cursor = startIndex;
        }

        public boolean hasPrevious() {
            return cursor > 0;
        }

        public E previous() {
            if (cursor < 0)
                throw new NoSuchElementException();
            Object[] elemsLocal = SpecifiedArrayListImpl.this.elementData;
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

    // TODO ATTENTION VERY UNOPTIMIZED AND ALSO TYPE POLLUTED
    @SuppressWarnings("unchecked")
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        ArrayList<E> list = new ArrayList<>();
        list.addAll((Collection<? extends E>) Arrays.asList(elementData));
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(elementData);
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
        if (!Arrays.equals(elementData, other.elementData))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public String toString() {
        Iterator<E> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
        // return "SpecifiedArrayListImpl [size=" + size + ", elems=" + Arrays.toString(elems) + "]";
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        int curCapacity = elementData.length;
        if (curCapacity < minCapacity) {
            // If we ensure that there is enough capacity it will be most likely that not much more elements
            // than this capacity will be added in the near future.
            // TODO CHECK IF USEFUL IN PROJECT
            int newLength;
            if (minCapacity <= CAPACITY_GROWING_THRESHOLD) {
                newLength = minCapacity;
            } else {
                // newLength = capacity + INITIAL_CAPACITY;
                newLength = curCapacity + (curCapacity >> 1);
            }
            elementData = Arrays.copyOf(elementData, newLength);
        }
    }

    public void newEnsureCapacity(int minCapacity) {
        final int curCapacity = elementData.length;

        if (curCapacity < minCapacity) {
            if (elementData == EMPTY_ELEMENTDATA) {
                elementData = new Object[calculateCapacity(INITIAL_CAPACITY, minCapacity)];

            } else if (curCapacity == INITIAL_CAPACITY) {
                elementData = Arrays.copyOf(elementData, calculateCapacity(NEXT_CAPACITY, minCapacity));
            } else {
                // grow();
                int newLength = curCapacity + (curCapacity >> 1);
                elementData = Arrays.copyOf(elementData, calculateCapacity(newLength, minCapacity));
            }
        }
    }

    public int calculateCapacity(int proposedCap, int minCap) {
        return Math.max(proposedCap, minCap);
    }

    @Override
    public void trimToSize() {
        if (USE_AL_STRATEGY) {
            if (size < elementData.length) {
                elementData = (size == 0)
                                ? EMPTY_ELEMENTDATA
                                : Arrays.copyOf(elementData, size);
            }
        } else {
            if (elementData.length >= size)
                elementData = Arrays.copyOf(elementData, size);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        Arrays.sort((E[]) elementData, 0, size, c);
    }

    //
    //
    // -------------------POTECTED METHODS------------------------
    //
    //

    protected double getLoadFactor() {
        if (elementData == EMPTY_ELEMENTDATA || elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            return 1.1;// Special Value for EMPTY_ELEMENT DATA
        return ((double) size / elementData.length);
    }

    protected int getCapacity() {
        return elementData.length;
    }

    //
    //
    // -------------------PRIVATE METHODS------------------------
    //
    //

    private void checkBoundaries(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    private void checkBoundsForAdd(int index) {
        if (index < 0 || index > this.size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    private void trimIfUseful() {
        int threshold = (elementData.length / GROW_FACTOR) + 1; // TODO Find more efficient strategy
        if (threshold > size && threshold < elementData.length) {
            trim(threshold);
        }
    }

    /**
     * Removes the Object at given index without any checks.
     *
     * @param index index of object to be removed
     */
    private void fastRemove(int index) {
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        elementData[--size] = null;
    }

    /**
     * Increases the arraySize by multiplying the array length by the current GROW_FACTOR
     */
    protected void grow() {
        int newLength = elementData.length * GROW_FACTOR; // TODO remove Protected
        elementData = Arrays.copyOf(elementData, newLength);
    }

    private void growIfNeeded() {
        if (size == elementData.length)
            grow();
    }

    /** Performs a "hard" cut with potential data loss */
    private void trim(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        System.arraycopy(elementData, 0, elementData, 0, capacity);
    }

    private boolean removeCollection(Collection<?> c, boolean isRetained) {
        int removed = 0;
        int w = 0, i = 0;

        for (; i < size; i++) {
            if (c.contains(elementData[i]) == isRetained) {
                elementData[w] = elementData[i];
                w++;
            } else {
                removed++;
            }
        }
        if (i != size) {
            System.arraycopy(elementData, i, elementData, w, size - i);
            w += size - i;
        }
        if (w != size) {
            for (int j = w; j < size; j++)
                elementData[j] = null;
            size = w;
        }
        if (!USE_AL_STRATEGY)
            trimIfUseful();
        return removed != 0;
    }

    @SuppressWarnings("unchecked")
    private E castUnchecked(Object obj) {
        return (E) obj;
    }

    // METHODS NEEDED FOR IMITATION OF ARRAYLIST

    /**
     * Increases the capacity to ensure that it can hold at least the number of elements specified by
     * the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    protected void growAL(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacityAL(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacityAL(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary, to ensure that it can
     * hold at least the number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacityAL(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                        // any size if not default element table
                        ? 0
                        // larger than default for default empty table. It's already
                        // supposed to be at default size.
                        : DEFAULT_CAPACITY;

        if (minCapacity > minExpand) {
            ensureExplicitCapacityAL(minCapacity);
        }
    }

    private static int calculateCapacityAL(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    private void ensureCapacityInternalAL(int minCapacity) {
        ensureExplicitCapacityAL(calculateCapacityAL(elementData, minCapacity));
    }

    private void ensureExplicitCapacityAL(int minCapacity) {

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            growAL(minCapacity);
    }

}