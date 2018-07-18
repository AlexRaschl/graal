package org.graalvm.collections.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.graalvm.collections.list.primitives.SimpleDoubleSpecifiedArrayList;
import org.graalvm.collections.list.primitives.SimpleIntSpecifiedArrayList;
import org.graalvm.collections.list.statistics.StatisticalSpecifiedArrayListImpl;

import sun.misc.SharedSecrets;

/**
 * The SpecifiedArrayList is a specified version of the Java.Util.ArrayList that has been made for
 * the graal Compiler to enhance its performance and to prevent from pollution of the users type
 * profile.
 * <p>
 * It provides the most important functionalities of the original ArrayList. The API is basically a
 * copy of the java.util.List API although some functionalities are left out since they are not
 * needed in the graal compiler.
 *
 *
 * @author Alex R. NOTE: Some things are copied directly from the ArrayList.class code since they
 *         did already a very good job. So props to the developers of java.util.ArrayList
 *
 */

public class SpecifiedArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    /**
     * Factory methods
     */

// If only one occurrence is replaced with SSAR only these instances will be tracked
//
// public static <E> SpecifiedArrayList<E> createNew() {
// return new ArrayListClone<>();
// }
//
// public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
// return new ArrayListClone<>(initalCapacity);
// }
//
// public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
// return new ArrayListClone<>(c);
// }
//
// public static <E> SpecifiedArrayList<E> createNewFixed(final int initalCapacity) {
// return new ArrayListClone<>(initalCapacity);
// }

// public static <E> SpecifiedArrayList<E> createNew() {
// return new StatisticalArrayListClone<>();
// }
//
// public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
// return new StatisticalArrayListClone<>(initalCapacity);
// }
//
// public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
// return new StatisticalArrayListClone<>(c);
// }
//
// public static <E> SpecifiedArrayList<E> createNewFixed(final int initalCapacity) {
// return new StatisticalArrayListClone<>(initalCapacity);
// }

// public static <E> SpecifiedArrayList<E> createNew() {
// return new StatisticalSpecifiedArrayListImpl<>();
// }
//
// public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
// return new StatisticalSpecifiedArrayListImpl<>(initalCapacity);
// }
//
// public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
// return new StatisticalSpecifiedArrayListImpl<>(c);
// }
//
// public static <E> SpecifiedArrayList<E> createNewFixed(final int initialCapacity) {
// return new StatisticalSpecifiedArrayListImpl<>(initialCapacity);
// }

    public static <E> SpecifiedArrayList<E> createNew() {
        return new SpecifiedArrayList<>();
    }

    public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
        return new SpecifiedArrayList<>(initalCapacity);
    }

    public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
        return new SpecifiedArrayList<>(c);
    }

    public static <E> SpecifiedArrayList<E> createNewFixed(final int initialCapacity) {
        return new FixedCapacitiySpecifiedArrayList<>(initialCapacity);
    }

    public static SimpleIntSpecifiedArrayList createNewIntList(final int initialCapacity) {
        return new SimpleIntSpecifiedArrayList(initialCapacity);
    }

    public static SimpleIntSpecifiedArrayList createNewIntList(final int[] c) {
        return new SimpleIntSpecifiedArrayList(c);
    }

    public static SimpleDoubleSpecifiedArrayList createNewDoubleList(final int initialCapacity) {
        return new SimpleDoubleSpecifiedArrayList(initialCapacity);
    }

    public static final int GROW_OFFSET;
    static {
        final String prop = System.getProperty("GROW_OFFSET");
        int def = 32;
        if (prop != null) {
            def = Integer.parseInt(prop);
        }
        GROW_OFFSET = def;
    }

    // -------------------------FIELDS-------------------------------------------------

    private static final long serialVersionUID = 9130616599645229594L;

    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = GROW_OFFSET; // Capacity after first grow
    private final static int TRIM_FACTOR = 2; // Trim factor
    //
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    // Non Private for ArrayListClone
    int size;
    transient Object elementData[];

    // ARRAYLIST IMMITATION Stuff
    static final Object[] EMPTY_ELEMENTDATA = {};
    static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    // ---------------------CONSTRUCTORS --------------------------------
    /**
     * Creates an instance of SpecifiedArrayList with a given initial capacity.
     *
     * @param initialCapacity Capacity the list will have from beginning
     */
    public SpecifiedArrayList(int initialCapacity) {

        if (initialCapacity > 0) {
            elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Negative Capacity: " + initialCapacity);
        }

    }

    /**
     * Creates an instance of SpecifiedArrayList.
     */
    public SpecifiedArrayList() {
        size = 0;
        elementData = EMPTY_ELEMENTDATA;

    }

    /**
     * Creates an instance of SpecifieArrayList that holds the elements of the given collection.
     *
     * @param collection
     */
    public SpecifiedArrayList(Collection<? extends E> collection) {
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

    /**
     * Trims the list to its size.
     */
    public void trimToSize() {
        modCount++;

        if (elementData.length >= size) {
            if (size == 0) {
                elementData = EMPTY_ELEMENTDATA;
            } else {
                elementData = Arrays.copyOf(elementData, size);
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
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    @Override
    public void add(int index, E element) {
        checkBoundsForAdd(index);
        ensureCapacity(size + 1);

        if (index != size)
            System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    @Override
    public boolean remove(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) {
                    fastRemove(i);
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    fastRemove(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public E remove(int index) {
        checkBoundaries(index);
        modCount++;

        final Object oldElem = elementData[index];

        if (index != size)
            System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        elementData[--size] = null;
        return castUnchecked(oldElem);

    }

    @Override
    public void clear() {
        modCount++;

        // if (elementData.length > 12) {
        // elementData = EMPTY_ELEMENTDATA;
        // System.out.println("Reset to 0 cap");
// } else {
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }
        size = 0;

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

        final Object oldElem = elementData[index];
        elementData[index] = element;
        return castUnchecked(oldElem);
    }

    @Override
    public int indexOf(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i]))
                    return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null)
                    return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o != null) {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elementData[i]))
                    return i;
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null)
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

        final int cSize = c.size();

        if (cSize == 0)
            return false;

        ensureCapacity(size + cSize);// Useful if c is large

        final Object[] a = c.toArray();
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

        if (c.size() == 0)
            return false;

        final Object[] arr = c.toArray();
        final int cSize = arr.length;

        ensureCapacity(size + cSize);
        System.arraycopy(elementData, index, elementData, index + cSize, size - index);

        System.arraycopy(arr, 0, elementData, index, cSize);
        size += cSize;
        return cSize != 0;

    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeCollection(c, false);
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
        final int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                        numMoved);

        // clear to let GC do its work
        final int newSize = size - (toIndex - fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeCollection(c, true);
    }

    // TODO Optimize the following functions

    @Override
    @SuppressWarnings("hiding")
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i = 0, j = 0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k = newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    @Override
    @SuppressWarnings({"unchecked", "hiding"})
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    @Override
    public Object clone() {
        try {
            final SpecifiedArrayList<?> v = (SpecifiedArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
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
        protected int expectedModCount = modCount;

        Itr() {
            cursor = 0;
            lastRet = -1;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        @SuppressWarnings({"unchecked", "hiding"})
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = SpecifiedArrayList.this.elementData;
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
                SpecifiedArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings({"unchecked", "hiding"})
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = SpecifiedArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = SpecifiedArrayList.this.elementData;
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
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

    }

    private class ListItr extends Itr implements ListIterator<E> {

        ListItr(int startIndex) {
            super();
            cursor = startIndex;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        @SuppressWarnings({"unchecked", "hiding"})
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = SpecifiedArrayList.this.elementData;
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
                SpecifiedArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                SpecifiedArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }// END LIST_ITERATOR

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
        final SpecifiedArrayList<E> other = (SpecifiedArrayList<E>) obj;
        if (!Arrays.equals(elementData, other.elementData))
            return false;
        if (size != other.size)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final Iterator<E> it = iterator();
        if (!it.hasNext())
            return "[]";

        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

// // ---------------------------SERIALIZATION METHODS------------------------------
//
// /**
// * Save the state of the <tt>ArrayList</tt> instance to a stream (that is, serialize it).
// *
// * @serialData The length of the array backing the <tt>ArrayList</tt> instance is emitted (int),
// * followed by all of its elements (each an <tt>Object</tt>) in the proper order.
// */
// private void writeObject(java.io.ObjectOutputStream s)
// throws java.io.IOException {
// // Write out element count, and any hidden stuff
// final int expectedModCount = modCount;
// s.defaultWriteObject();
//
// // Write out size as capacity for behavioural compatibility with clone()
// s.writeInt(size);
//
// // Write out all elements in the proper order.
// for (int i = 0; i < size; i++) {
// s.writeObject(elementData[i]);
// }
//
// if (modCount != expectedModCount) {
// throw new ConcurrentModificationException();
// }
// }
//
// /**
// * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is, deserialize it).
// */
// private void readObject(java.io.ObjectInputStream s)
// throws java.io.IOException, ClassNotFoundException {
// elementData = EMPTY_ELEMENTDATA;
//
// // Read in size, and any hidden stuff
// s.defaultReadObject();
//
// // Read in capacity
// s.readInt(); // ignored
//
// if (size > 0) {
// // be like clone(), allocate array based upon size not capacity
// final int capacity = size; // TODO check if replace correct
// SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
// ensureCapacity(size);
//
// final Object[] a = elementData;
// // Read in all elements in the proper order.
// for (int i = 0; i < size; i++) {
// a[i] = s.readObject();
// }
// }
// }

    // ------------------GROWING METHODS------------------------

// Old ensureCap Method
// public void ensureCapacity(int minCapacity) {
// modCount++;
// int curCapacity = elementData.length;
// if (curCapacity < minCapacity) {
// // If we ensure that there is enough capacity it will be most likely that not much more elements
// // than this capacity will be added in the near future.
// // TODO CHECK IF USEFUL IN PROJECT
// int newLength;
// if (minCapacity <= CAPACITY_GROWING_THRESHOLD) {
// newLength = minCapacity;
// } else {
// // newLength = capacity + INITIAL_CAPACITY;
// newLength = curCapacity + (curCapacity >> 1);
// }
// elementData = Arrays.copyOf(elementData, newLength);
// }
// }

    public void ensureCapacity(int minCapacity) {
        modCount++;

        if (elementData.length < minCapacity) {
            grow(minCapacity);
        }
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

    // Sublist Support functions
    protected void incModCount() {
        modCount++;
    }

    protected int getModCount() {
        return modCount;
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

    void trimIfUseful(final int numRemoved) {
        final int threshold = (elementData.length / TRIM_FACTOR) + 1;
        if (numRemoved > NEXT_CAPACITY && numRemoved > elementData.length / 4 && threshold > size && threshold < elementData.length) {
            trim(threshold);
        }
    }

    /** Performs a "hard" cut with potential data loss */
    private void trim(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        System.arraycopy(elementData, 0, elementData, 0, capacity);
    }

    /**
     * Removes the Object at given index without any checks.
     *
     * @param index index of object to be removed
     */
    private void fastRemove(final int index) {
        modCount++;
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        elementData[--size] = null;
    }

    /**
     * Increases the capacity of the underlying array
     */
    protected void grow(final int minCapacity) {
        final int curCapacity = elementData.length;

// TODO I actually have no Idea why this commenting this stuff raises the loadFactor by 10 Percent

        if (elementData == EMPTY_ELEMENTDATA) {
            elementData = new Object[Math.max(INITIAL_CAPACITY, minCapacity)];

        } else if (curCapacity <= INITIAL_CAPACITY) { // TODO check if <= leads to better results
            elementData = Arrays.copyOf(elementData, Math.max(NEXT_CAPACITY, minCapacity));
        } else {
            // final int newLength = curCapacity + (curCapacity >> 1); // *1.5
            // final int newLength = curCapacity + (curCapacity >> 1) + (curCapacity >> 2); // *1.75
            // final int newLength = curCapacity << 2;

// if (curCapacity < 12) {
// newLength = curCapacity + 4;
// } else {
            final int newLength = curCapacity << 1; // Times 2
            // }
            elementData = Arrays.copyOf(elementData, Math.max(newLength, minCapacity));
        }
    }

// private static int calculateCapacity(final int proposedCap, final int minCap) {
// int capacity = proposedCap;
// if (proposedCap < minCap)
// capacity = minCap;
//
// return capacity;
// }

// // TODO Use like in ArrayList
// private static int hugeCapacityAL(int minCapacity) {
// if (minCapacity < 0) // overflow
// throw new OutOfMemoryError();
// return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
// }

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

        trimIfUseful(removed);// TODO Check if useful
        return removed != 0;
    }

    @SuppressWarnings("unchecked")
    private E castUnchecked(Object obj) {
        return (E) obj;
    }

// private static int calculateCapacity(Object[] elementData, int minCapacity) {
// if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
// return Math.max(DEFAULT_CAPACITY, minCapacity);
// }
// return minCapacity;
// }

    //
    //
    // --------------------SUBLIST SECITON----------------
    //
    //

    /**
     * Returns a view of theensureCapacityInternalAl portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive. (If {@code fromIndex} and
     * {@code toIndex} are equal, the returned list is empty.) The returned list is backed by this list,
     * so non-structural changes in the returned list are reflected in this list, and vice-versa. The
     * returned list supports all of the optional list operations.
     *
     * <p>
     * This method eliminates the need for explicit range operations (of the sort that commonly exist
     * for arrays). Any operation that expects a list can be used as a range operation by passing a
     * subList view instead of a whole list. For example, the following idiom removes a range of
     * elements from a list:
     *
     * <pre>
     * list.subList(from, to).clear();
     * </pre>
     *
     * Similar idioms may be constructed for {@link #indexOf(Object)} and {@link #lastIndexOf(Object)},
     * and all of the algorithms in the {@link Collections} class can be applied to a subList.
     *
     * <p>
     * The semantics of the list returned by this method become undefined if the backing list (i.e.,
     * this list) is <i>structurally modified</i> in any way other than via the returned list.
     * (Structural modifications are those that change the size of this list, or otherwise perturb it in
     * such a fashion that iterations in progress may yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                            ") > toIndex(" + toIndex + ")");
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                        int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = SpecifiedArrayList.this.modCount;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = (E) SpecifiedArrayList.this.elementData[offset + index];
            SpecifiedArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return (E) SpecifiedArrayList.this.elementData[offset + index];
        }

        @Override
        public int size() {
            checkForComodification();
            return this.size;
        }

        @Override
        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = ((SpecifiedArrayList<E>) parent).modCount;
            this.size++;
        }

        @Override
        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = ((SpecifiedArrayList<E>) parent).modCount;
            this.size--;
            return result;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            ((SpecifiedArrayList<E>) parent).removeRange(parentOffset + fromIndex,
                            parentOffset + toIndex);
            this.modCount = ((SpecifiedArrayList<E>) parent).modCount;
            this.size -= toIndex - fromIndex;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize == 0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = ((SpecifiedArrayList<E>) parent).modCount;
            this.size += cSize;
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
            @SuppressWarnings("hiding")
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = SpecifiedArrayList.this.modCount;

                @Override
                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @Override
                @SuppressWarnings({"unchecked", "hiding"})
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = SpecifiedArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @Override
                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @Override
                @SuppressWarnings({"unchecked", "hiding"})
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = SpecifiedArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @Override
                @SuppressWarnings({"unchecked", "hiding"})
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = SpecifiedArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // update once at end of iteration to reduce heap write traffic
                    lastRet = cursor = i;
                    checkForComodification();
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
                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = SpecifiedArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                @Override
                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SpecifiedArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                @Override
                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = SpecifiedArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != SpecifiedArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + this.size;
        }

        private void checkForComodification() {
            if (SpecifiedArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

// @Override
// public Spliterator<E> spliterator() {
// checkForComodification();
// return new SpecifiedArrayListSpliterator<>(SpecifiedArrayList.this, offset,
// offset + this.size, this.modCount);
// }
    }

    // --------------SPLITERATOR --------------------------------

// /**
// * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em> and <em>fail-fast</em>
// * {@link Spliterator} over the elements in this list.
// *
// * <p>
// * The {@code Spliterator} reports {@link Spliterator#SIZED}, {@link Spliterator#SUBSIZED}, and
// * {@link Spliterator#ORDERED}. Overriding implementations should document the reporting of
// * additional characteristic values.
// *
// * @return a {@code Spliterator} over the elements in this list
// * @since 1.8
// */
// @Override
// public Spliterator<E> spliterator() {
// return new SpecifiedArrayListSpliterator<>(this, 0, -1, 0);
// }

// /** Index-based split-by-two, lazily initialized Spliterator */
// static final class SpecifiedArrayListSpliterator<E> implements Spliterator<E> {
//
// /*
// * If ArrayLists were immutable, or structurally immutable (no adds, removes, etc), we could
// * implement their spliterators with Arrays.spliterator. Instead we detect as much interference
// * during traversal as practical without sacrificing much performance. We rely primarily on
// * modCounts. These are not guaranteed to detect concurrency violations, and are sometimes overly
// * conservative about within-thread interference, but detect enough problems to be worthwhile in
// * practice. To carry this out, we (1) lazily initialize fence and expectedModCount until the
// latest
// * point that we need to commit to the state we are checking against; thus improving precision.
// * (This doesn't apply to SubLists, that create spliterators with current non-lazy values). (2) We
// * perform only a single ConcurrentModificationException check at the end of forEach (the most
// * performance-sensitive method). When using forEach (as opposed to iterators), we can normally
// only
// * detect interference after actions, not before. Further CME-triggering checks apply to all other
// * possible violations of assumptions for example null or too-small elementData array given its
// * size(), that could only have occurred due to interference. This allows the inner loop of
// forEach
// * to run without any further checks, and simplifies lambda-resolution. While this does entail a
// * number of checks, note that in the common case of list.stream().forEach(a), no checks or other
// * computation occur anywhere other than inside forEach itself. The other less-often-used methods
// * cannot take advantage of most of these streamlinings.
// */
//
// private final SpecifiedArrayList<E> list;
// private int index; // current index, modified on advance/split
// private int fence; // -1 until used; then one past last index
// private int expectedModCount; // initialized when fence set
//
// /** Create new spliterator covering the given range */
// SpecifiedArrayListSpliterator(SpecifiedArrayList<E> list, int origin, int fence,
// int expectedModCount) {
// this.list = list; // OK if null unless traversed
// this.index = origin;
// this.fence = fence;
// this.expectedModCount = expectedModCount;
// }
//
// private int getFence() { // initialize fence to size on first use
// int hi; // (a specialized variant appears in method forEach)
// SpecifiedArrayList<E> lst;
// if ((hi = fence) < 0) {
// if ((lst = list) == null)
// hi = fence = 0;
// else {
// expectedModCount = lst.modCount;
// hi = fence = lst.size;
// }
// }
// return hi;
// }
//
// public SpecifiedArrayListSpliterator<E> trySplit() {
// int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
// return (lo >= mid) ? null : // divide range in half unless too small
// new SpecifiedArrayListSpliterator<>(list, lo, index = mid,
// expectedModCount);
// }
//
// public boolean tryAdvance(Consumer<? super E> action) {
// if (action == null)
// throw new NullPointerException();
// int hi = getFence(), i = index;
// if (i < hi) {
// index = i + 1;
// @SuppressWarnings("unchecked")
// E e = (E) list.elementData[i];
// action.accept(e);
// if (list.modCount != expectedModCount)
// throw new ConcurrentModificationException();
// return true;
// }
// return false;
// }
//
// public void forEachRemaining(Consumer<? super E> action) {
// int i, hi, mc; // hoist accesses and checks from loop
// SpecifiedArrayList<E> lst;
// Object[] a;
// if (action == null)
// throw new NullPointerException();
// if ((lst = list) != null && (a = lst.elementData) != null) {
// if ((hi = fence) < 0) {
// mc = lst.modCount;
// hi = lst.size;
// } else
// mc = expectedModCount;
// if ((i = index) >= 0 && (index = hi) <= a.length) {
// for (; i < hi; ++i) {
// @SuppressWarnings("unchecked")
// E e = (E) a[i];
// action.accept(e);
// }
// if (lst.modCount == mc)
// return;
// }
// }
// throw new ConcurrentModificationException();
// }
//
// public long estimateSize() {
// return (long) (getFence() - index);
// }
//
// public int characteristics() {
// return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
// }
// }

}
//
// /**
// * Returns the current size of the list
// *
// * @return the number of elements in this list
// *
// */
// @Override
// public abstract int size();
//
// /**
// * Returns true if this list contains no elements.
// *
// * @return true if size is equal to 0
// */
// @Override
// public abstract boolean isEmpty();
//
// /**
// * Returns false if o is null or true if at least one of the elements in this list are equal to
// the
// * Object o
// *
// * @param o
// * @return true if o is not null and o is in list
// */
// @Override
// public abstract boolean contains(Object o);
//
// /**
// * An Array of the Objects that are stored in this List. The Array is <tt>size<tt> elements long.
// *
// * @return Array of Objects stored in List
// */
// @Override
// public abstract Object[] toArray();
//
// /**
// * Adds the the Element e to the List. If the Array has reached its max capacity it will grow to
// * hold more elements. Returns <tt>true<tt> if insertion was successful.
// *
// * @param e
// * @return <tt>true<tt> if the element has been inserted successfully else <tt>false<tt>
// */
// @Override
// public abstract boolean add(E e);
//
// /**
// * Removes the first occurrence of an Object form the list if it exists otherwise returns false
// *
// * @return <tt>true<tt> if element has been found and removed else <tt>false<tt>
// */
// @Override
// public abstract boolean remove(Object o);
//
// /**
// * Checks if the List contains all of the elements stored in the given collection. Basically uses
// * the contains function on each element in the collection.
// *
// *
// * @param c collection of elements to be checked
// * @return <tt>true<tt> iff all elements of c are located in the List else <tt>false<tt>
// */
// @Override
// public abstract boolean containsAll(Collection<?> c);
//
// /**
// * Adds the whole collection c to the ArrayList.
// *
// * @param c Collection of elements to be added to the list
// * @return <tt>true<tt> if all elements have been added successfully else <tt>false<tt>
// */
// @Override
// public abstract boolean addAll(Collection<? extends E> c); // TOD Check if EnsureCapacity is
// useful here
//
// /**
// * DONE CHECK IF NEEDED -> Most Likely Not
// *
// * @param index
// * @param c
// * @return
// */
// @Override
// public abstract boolean addAll(int index, Collection<? extends E> c);
//
// /**
// * Removes all the elements of c from the ArrayList.
// *
// * @param c Collection of elements to be removed from the list
// * @return <tt>true<tt> iff the list is changed else <tt>false<tt>
// */
// @Override
// public abstract boolean removeAll(Collection<?> c);
//
// /**
// * Retains only the elements that are stored in the Collection c.
// *
// * @param c Collection of elements to be retained
// * @return <tt>true<tt> iff the list is changed else <tt>false<tt>
// */
// @Override
// public abstract boolean retainAll(Collection<?> c);
//
// /**
// * Deletes all elements from this list.
// */
// @Override
// public abstract void clear();
//
// /**
// * Get the Element at given Index
// *
// * @param index of element to be retrieved
// * @return Element at given index
// *
// * @throws ArrayIndexOutOfBoundsException if Index is out of range
// */
// @Override
// public abstract E get(int index);
//
// /**
// * Replaces the Element at position index with the given element e.
// *
// * @param index of element to be retrieved
// * @param element to be stored in list at given index
// * @return Element previously located at given index
// *
// * @throws ArrayIndexOutOfBoundsException if Index is out of range
// */
// @Override
// public abstract E set(int index, E element);
//
// /**
// *
// *
// * @param index
// * @param element
// */
// @Override
// public abstract void add(int index, E element);
//
// /**
// * Removes the Object at given Index from the List.
// *
// * @param index of Object to be removed
// * @return removed Object
// *
// * @throws ArrayIndexOutOfBoundsException if Index is out of range
// */
// @Override
// public abstract E remove(int index);
//
// /**
// * Returns the Index of the first occurrence of an Object.
// *
// * @param o Object to search for
// * @return Index of the first occurrence in List or -1 if it is not contained
// */
// @Override
// public abstract int indexOf(Object o);
//
// /**
// * Returns the Index of the last occurrence of an Object.
// *
// * @param o Object to search for
// * @return Index of the last occurrence in List or -1 if it is not contained
// */
// @Override
// public abstract int lastIndexOf(Object o);
//
// /**
// * Generates a ListIterator over the elements in this List.
// *
// * @return ListIterator over the elements in this List
// */
// @Override
// public abstract ListIterator<E> listIterator();
//
// /**
// * Generates a ListIterator over the elements in this List starting at given index.
// *
// * @param index
// * @return
// * @throws IndexOutOfBoundsException if index is out of Range
// */
// @Override
// public abstract ListIterator<E> listIterator(int index);
//
// /**
// *
// * @param fromIndex Index of the first Element in the new Sublist (inclusive)
// * @param toIndex Marks the EndPoint of the Sublist (exclusive)
// * @return A Sublist containing the Elements at fromIndex until toIndex
// *
// * @throws IllegalArgumentException if fromIndex > toIndex
// * @throws IndexOutOfBoundsException if one of the given Indexes is out of Range
// */
// @Override
// public abstract List<E> subList(int fromIndex, int toIndex); // DONE CHECK IF NEEDED -> Most
// likely Not
//
// /**
// * Returns an Iterator over the elements in the list.
// *
// * @return Iterator over elements in the list
// */
// @Override
// public abstract Iterator<E> iterator();
//
// /**
// * Ensures that the internal Array is at least of size capacity. Used for adding large amounts of
// * data effectively.
// *
// * @param capacity that the List should be able to hold at minimum
// */
// public abstract void ensureCapacity(int capacity);
//
// /**
// * Trims the capacity of the SAR to so be it's current size;
// */
// public abstract void trimToSize();
