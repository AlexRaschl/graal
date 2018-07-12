package org.graalvm.collections.list.primitives;

import java.util.Arrays;

public class SimpleDoubleSpecifiedArrayList {
    // CONSTTANTS
    private final static int INITIAL_CAPACITY = 2; // Used on first insertion
    private final static int NEXT_CAPACITY = 8; // Capacity after first grow

    private final static double[] EMPTY_DOUBLES = {};

    // Fields
    private transient double[] elementData;
    private int size = 0;

    public SimpleDoubleSpecifiedArrayList() {
        elementData = EMPTY_DOUBLES;
    }

    public SimpleDoubleSpecifiedArrayList(final int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IndexOutOfBoundsException("Negative size: " + initialCapacity);
        } else if (initialCapacity == 0) {
            elementData = EMPTY_DOUBLES;
        } else {
            elementData = new double[initialCapacity];
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

    public double[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    public boolean add(double e) {
        ensureCapacity(size + 1);
        elementData[size++] = e;
        return true;
    }

    public void add(int index, double element) {
        checkBoundsForAdd(index);
        ensureCapacity(size + 1);

        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;

    }

    public double removeIdx(int index) {
        checkBoundaries(index);
        final double oldElem = elementData[index];
        System.arraycopy(elementData, index + 1, elementData, index, size - index - 1);
        return oldElem;
    }

    public boolean remove(double e) {
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
        elementData = EMPTY_DOUBLES;
    }

    public double get(int index) {
        checkBoundaries(index);
        return elementData[index];
    }

    public double set(int index, double element) {
        checkBoundaries(index);
        final double oldVal = elementData[index];
        elementData[index] = element;
        return oldVal;
    }

    public int indexOf(double e) {
        for (int i = 0; i < size; i++)
            if (e == elementData[i])
                return i;

        return -1;
    }

    public int lastIndexOf(double e) {
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

    private void grow(int minCapacity) {
        final int curCapacity = elementData.length;
        if (elementData == EMPTY_DOUBLES) {
            elementData = new double[Math.max(INITIAL_CAPACITY, minCapacity)];
        } else if (curCapacity <= NEXT_CAPACITY) {
            elementData = Arrays.copyOf(elementData, Math.max(NEXT_CAPACITY, minCapacity));
        } else {
            // final int nextCapacity = curCapacity + (curCapacity >> 1);
            // final int nextCapacity = curCapacity + NEXT_CAPACITY;
            final int newLength;
            if (curCapacity < 12) {
                newLength = curCapacity + 4;
            } else {
                newLength = curCapacity << 1; // Times 2
            }
            elementData = Arrays.copyOf(elementData, Math.max(newLength, minCapacity));
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
