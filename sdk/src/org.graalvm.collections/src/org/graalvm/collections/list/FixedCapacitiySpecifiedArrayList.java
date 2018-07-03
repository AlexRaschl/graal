package org.graalvm.collections.list;

import java.util.Collection;

import sun.misc.SharedSecrets;

public class FixedCapacitiySpecifiedArrayList<E> extends SpecifiedArrayList<E> {

    private static final long serialVersionUID = 1L;

    final int capacity;

    FixedCapacitiySpecifiedArrayList(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Negative Capacity");
        this.capacity = capacity;
        this.size = 0;
        elementData = new Object[capacity];
    }

    @Override
    public void trimToSize() {
        return;
    }

    @Override
    void trimIfUseful() {
        return;
    }

    @Override
    public boolean add(E e) {
        elementData[size++] = e;
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }
        size = 0;
    }

    @Override
    public void add(int index, E element) {
        checkBoundsForAdd(index);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int cSize = c.size();
        System.arraycopy(a, 0, elementData, size, cSize);
        size = size + cSize;
        return cSize != 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkBoundsForAdd(index);
        Object[] arr = c.toArray();
        int cSize = arr.length;
        System.arraycopy(elementData, index, elementData, index + cSize, size - index);

        System.arraycopy(arr, 0, elementData, index, cSize);
        size += cSize;
        return cSize != 0;

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
            int capacity = size; // TODO check if replace correct
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);

            Object[] a = elementData;
            // Read in all elements in the proper order.
            for (int i = 0; i < size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    private void checkBoundsForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size " + size);
    }

}
