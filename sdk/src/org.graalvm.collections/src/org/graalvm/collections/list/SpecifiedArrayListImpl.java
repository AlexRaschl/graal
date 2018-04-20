package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;

import sun.misc.SharedSecrets;

@Deprecated
public class SpecifiedArrayListImpl<E> extends SpecifiedArrayList<E> {

    private static final long serialVersionUID = 9130616599645229594L;

    private static final boolean USE_AL_STRATEGY = false;

    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = 10; // Capacity after first grow
    private final static int GROW_FACTOR = 2; // Growing factor
    private final static int CAPACITY_GROWING_THRESHOLD = 32; // Unused
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private int size;

    // RENAMED due to compatibility issues
    transient Object elementData[];

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
            size = collection.size();
            elementData = collection.toArray();
            if (size != 0) {
                // c.toArray might (incorrectly) not return Object[] (see 6260652)
                if (elementData.getClass() != Object[].class)
                    elementData = Arrays.copyOf(elementData, size, Object[].class);
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
    public E remove(int index) {
        if (USE_AL_STRATEGY) {
            checkBoundaries(index);

            modCount++;
            E oldValue = castUnchecked(elementData[index]);

            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index + 1, elementData, index,
                                numMoved);
            elementData[--size] = null; // clear to let GC do its work

            return oldValue;
        } else {
            checkBoundaries(index);
            modCount++;
            Object oldElem = elementData[index];
            System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
            elementData[--size] = null;
            return castUnchecked(oldElem);
        }

    }

    // TODO Could only set size to 0 for fast clear
    @Override
    public void clear() {
        modCount++;

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
    public boolean containsAll(Collection<?> c) {
        for (Object obj : c) {
            if (!contains(obj))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int cSize = c.size();

        // if(cSize == 0)return false;

        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + cSize);// Useful if c is large
        } else {
            newEnsureCapacity(size + cSize);// Useful if c is large
        }

        System.arraycopy(a, 0, elementData, size, cSize);
        size = size + cSize;
        return cSize != 0;
    }

    /*
     * Not optimized for insertions at end use addAll(Collection c) instead (non-Javadoc)
     *
     * @see org.graalvm.collections.list.SpecifiedArrayList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBoundsForAdd(index);

// if (c.size() == 0)
// return false;

        Object[] arr = c.toArray();
        int cSize = arr.length;

        if (USE_AL_STRATEGY) {
            ensureCapacityInternalAL(size + cSize);
            int numMoved = size - index;
            if (numMoved > 0)
                System.arraycopy(elementData, index, elementData, index + cSize,
                                numMoved);
        } else {
            newEnsureCapacity(size + cSize);
            System.arraycopy(elementData, index, elementData, index + cSize, size - index);

        }

        System.arraycopy(arr, 0, elementData, index, cSize);
        size += cSize;
        return cSize != 0;

    }

    /**
     * Copied 1:1 from ArrayList
     *
     * Removes from this list all of the elements whose index is between {@code fromIndex}, inclusive,
     * and {@code toIndex}, exclusive. Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements. (If
     * {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or {@code toIndex} is out of range
     *             ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                        numMoved);

        // clear to let GC do its work
        int newSize = size - (toIndex - fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeCollection(c, false);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeCollection(c, true);
    }

// @Override
// public Object clone() {
// try {
// SpecifiedArrayListImpl<?> v = (SpecifiedArrayListImpl<?>) super.clone();
// v.elementData = Arrays.copyOf(elementData, size);
// v.modCount = 0;
// return v;
// } catch (CloneNotSupportedException e) {
// // this shouldn't happen, since we are Cloneable
// throw new InternalError(e);
// }
// }

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
        protected int expectedModCount = getModCount();

        Itr() {
            cursor = 0;
            lastRet = -1;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = SpecifiedArrayListImpl.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        /** Moved this here because also supported by Iterator in ArrayList */
        @Override
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                SpecifiedArrayListImpl.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = getModCount();
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        // DONE insert forEachRemaining
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = SpecifiedArrayListImpl.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = SpecifiedArrayListImpl.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && getModCount() == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (getModCount() != expectedModCount)
                throw new ConcurrentModificationException();
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

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = SpecifiedArrayListImpl.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                SpecifiedArrayListImpl.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
// if (lastRet == -1)
// throw new IllegalStateException("Remove or add Operation has been already excecuted!");
// SpecifiedArrayListImpl.this.set(lastRet, e);

        }

        @Override
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                SpecifiedArrayListImpl.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = getModCount();
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
// SpecifiedArrayListImpl.this.add(cursor, e);
// // TODO check if LastRet = -1 needed
// lastRet = -1;
// cursor++;
        }

    }

// // TODO ATTENTION VERY UNOPTIMIZED AND ALSO TYPE POLLUTED
// @SuppressWarnings("unchecked")
// @Override
// public List<E> subList(int fromIndex, int toIndex) {
// ArrayList<E> list = new ArrayList<>();
// list.addAll((Collection<? extends E>) Arrays.asList(elemsData));
// return list.subList(fromIndex, toIndex);
// }

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
        modCount++;
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

    @Override
    public void newEnsureCapacity(int minCapacity) {
        modCount++;

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

    @Override
    public int calculateCapacity(int proposedCap, int minCap) {
        return Math.max(proposedCap, minCap); // TODO optimize (this is only a safe solution)
    }

    @Override
    public void trimToSize() {
        modCount++;
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

    @Override
    protected double getLoadFactor() {
        if (elementData == EMPTY_ELEMENTDATA || elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
            return 1.1;// Special Value for EMPTY_ELEMENT DATA
        return ((double) size / elementData.length);
    }

    @Override
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
        if (index > size || index < 0)
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
        modCount++;
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        elementData[--size] = null;
    }

    /**
     * Increases the arraySize by multiplying the array length by the current GROW_FACTOR
     */
    @Override
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
            modCount += size - w;
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
    @Override
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
    @Override
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
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            growAL(minCapacity);
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt> instance is emitted (int),
     *             followed by all of its elements (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
                    throws java.io.IOException {
        // Write out element count, and any hidden stuff
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // Write out size as capacity for behavioural compatibility with clone()
        s.writeInt(size);

        // Write out all elements in the proper order.
        for (int i = 0; i < size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is, deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
                    throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in capacity
        s.readInt(); // ignored

        if (size > 0) {
            // be like clone(), allocate array based upon size not capacity
            int capacity = calculateCapacityAL(elementData, size);
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
            ensureCapacityInternalAL(size);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i = 0; i < size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    @SuppressWarnings("cast")
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ? new RandomAccessSubList<>(this, fromIndex, toIndex) : new SubList<>(this, fromIndex, toIndex));
    }
}

// SUBLIST SECTION
class SubList<E> extends SpecifiedArrayListImpl<E> {
    private final SpecifiedArrayList<E> l;
    private final int offset;
    private int size;

    SubList(SpecifiedArrayList<E> list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                            ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
        this.modCount = l.getModCount();
    }

    @Override
    public E set(int index, E element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index + offset, element);
    }

    @Override
    public E get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index + offset);
    }

    @Override
    public int size() {
        checkForComodification();
        return size;
    }

    @Override
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        checkForComodification();
        l.add(index + offset, element);
        this.modCount = l.getModCount();
        size++;
    }

    @Override
    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = l.remove(index + offset);
        this.modCount = l.getModCount();
        size--;
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex + offset, toIndex + offset);
        this.modCount = l.getModCount();
        size -= (toIndex - fromIndex);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        int cSize = c.size();
        if (cSize == 0)
            return false;

        checkForComodification();
        l.addAll(offset + index, c);
        this.modCount = l.getModCount();
        size += cSize;
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        checkForComodification();
        rangeCheckForAdd(index);

        return new ListIterator<E>() {
            private final ListIterator<E> i = l.listIterator(index + offset);

            @Override
            public boolean hasNext() {
                return nextIndex() < size;
            }

            @Override
            public E next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            @Override
            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            @Override
            public E previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            @Override
            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            @Override
            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            @Override
            public void remove() {
                i.remove();
                SubList.this.modCount = l.getModCount();
                size--;
            }

            @Override
            public void set(E e) {
                i.set(e);
            }

            @Override
            public void add(E e) {
                i.add(e);
                SubList.this.modCount = l.getModCount();
                size++;
            }
        };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<E>(this, fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void checkForComodification() {
        if (this.modCount != l.getModCount())
            throw new ConcurrentModificationException();
    }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    RandomAccessSubList(SpecifiedArrayList<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}
