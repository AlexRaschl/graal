package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

// TODO Implements List<E>
public class ArrayListClone<E> extends SpecifiedArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -5786771564421642851L;

    // TODO REMOVE
    private static final boolean USE_AL_STRATEGY = true;

    // ARRAYLIST IMMITATION Stuff
    private static final Object[] EMPTY_ELEMENTDATA = SpecifiedArrayList.EMPTY_ELEMENTDATA; // TODO check if super.EMPTY_ELEMENTDATA needed
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    private final static int DEFAULT_CAPACITY = 10;

    public ArrayListClone(int initialCapacity) {
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
     * Creates an instance of SpecifiedArrayList.
     */
    public ArrayListClone() {
        if (USE_AL_STRATEGY) {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        } else {
            // this(INITIAL_CAPACITY);
            this.size = 0;
            this.elementData = EMPTY_ELEMENTDATA;
        }

    }

    /**
     * Creates an instance of SpecifieArrayList that holds the elements of the given collection.
     *
     * @param collection
     */
    public ArrayListClone(Collection<? extends E> collection) {
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

    /**
     * Trims the list to its size.
     */
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

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBoundsForAdd(index);

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

    private void checkBoundaries(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    private void checkBoundsForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    // ----------METHODS NEEDED FOR IMITATION OF ARRAYLIST------------------

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

}