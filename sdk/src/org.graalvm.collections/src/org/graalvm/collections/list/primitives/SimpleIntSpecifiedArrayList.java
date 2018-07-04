package org.graalvm.collections.list.primitives;

import java.util.Arrays;

public final class SimpleIntSpecifiedArrayList {

    // CONSTTANTS
    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = 8; // Capacity after first grow

    private final static int[] EMPTY_INTS = {};

    // Fields
    private transient int[] elementData;
    private int size = 0;

    public SimpleIntSpecifiedArrayList() {
        elementData = EMPTY_INTS;
    }

    public SimpleIntSpecifiedArrayList(final int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IndexOutOfBoundsException("Negative size: " + initialCapacity);
        } else if (initialCapacity == 0) {
            elementData = EMPTY_INTS;
        } else {
            elementData = new int[initialCapacity];
        }

    }

    // TODO Check if collection CTOR needed

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(int e) {
        return indexOf(e) >= 0;
    }

    public int[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    public boolean add(int e) {
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    public void add(int index, int element) {
        checkBoundsForAdd(index);
        ensureCapacity(size + 1);

        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;

    }

    public int removeIdx(int index) {
        checkBoundaries(index);
        final int oldElem = elementData[index];
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        return oldElem;
    }

    public boolean remove(int e) {
        for (int i = 0; i < size; i++) {
            if (e == elementData[i]) {
                fastRemove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the Object at given index without any checks.
     *
     * @param index index of object to be removed
     */
    private void fastRemove(final int index) {
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        size--;
    }

    public void clear() {
        size = 0;
    }

    public int get(int index) {
        checkBoundaries(index);
        return elementData[index];
    }

    public int set(int index, int element) {
        checkBoundaries(index);
        final int oldVal = elementData[index];
        elementData[index] = element;
        return oldVal;
    }

    public int indexOf(int e) {
        for (int i = 0; i < size; i++)
            if (e == elementData[i])
                return i;

        return -1;
    }

    public int lastIndexOf(int e) {
        for (int i = size - 1; i > -1; i--)
            if (e == elementData[i])
                return i;

        return -1;
    }

    public void ensureCapacity(final int capacity) {
        if (elementData.length < capacity) {
            grow(capacity);
        }

    }

    private void grow(int capacity) {
        final int curCapacity = elementData.length;
        if (elementData == EMPTY_INTS) {
            elementData = new int[Math.max(INITIAL_CAPACITY, capacity)];
        } else if (curCapacity <= NEXT_CAPACITY) {
            elementData = Arrays.copyOf(elementData, Math.max(NEXT_CAPACITY, capacity));
        } else {
            int nextCapacity = curCapacity + curCapacity;

            if (capacity < nextCapacity)
                nextCapacity = capacity;

            elementData = Arrays.copyOf(elementData, nextCapacity);
        }
    }

    private void checkBoundaries(final int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

    private void checkBoundsForAdd(final int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

}
