package org.graalvm.collections.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class TwoCapacitySpecifiedArrayList<E> implements List<E> {

    private final static int SWITCH_AMT = 3;

    private transient Object elem0;
    private transient Object elem1;

    private static final Object[] EMPTY_ELEMS = new Object[]{};
    private transient Object[] elementData;

    private int size;
    private boolean isArray;

    public TwoCapacitySpecifiedArrayList() {
        this.size = 0;
        isArray = false;
        elementData = EMPTY_ELEMS;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public Object[] toArray() {
        if (!isArray) {
            switch (size) {
                case 0:
                    return new Object[]{};
                case 1:
                    return new Object[]{elem0};
                case 2:
                    return new Object[]{elem0, elem1};
                default:
                    throw new IllegalStateException("Size greater than 2 in elem branch!");
            }
        } else {
            return Arrays.copyOf(elementData, size);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final Object[] elems = toArray();
        if (a.length < size) {
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        }
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    public boolean add(E e) {
        ensureCapacity(size + 1); // Modifies isArray boolean

        if (!isArray) {
            if (size == 0) {
                elem0 = e;
            } else {
                elem1 = e;
            }
        } else {
            elementData[size] = e;
        }
        size++;
        return true;
    }

    // Wont be called much so its not so tragic that it is inefficient
    public boolean remove(Object o) {
        if (!isArray) {
            if (o == null) {
                if (size == 1) {
                    if (elem0 == null) {
                        size = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else if (size == 2) {
                    if (elem0 == null) {
                        elem0 = elem1;
                        elem1 = null;
                        size = 1;
                        return true;
                    } else if (elem1 == null) {
                        size = 1;
                        return true;
                    }
                }
            } else { // o != null
                if (size == 1) {
                    if (o.equals(elem0)) {
                        size = 0;
                        return true;
                    } else {
                        return false;
                    }
                } else if (size == 2) {
                    if (o.equals(elem0)) {
                        elem0 = elem1;
                        elem1 = null;
                        size = 1;
                        return true;
                    } else if (o.equals(elem1)) {
                        size = 1;
                        return true;
                    }
                }
            }
        } else {
            if (o == null) {
                for (int index = 0; index < size; index++)
                    if (elementData[index] == null) {
                        fastRemove(index);
                        return true;
                    }
            } else {
                for (int index = 0; index < size; index++)
                    if (o.equals(elementData[index])) {
                        fastRemove(index);
                        return true;
                    }
            }
        }
        return false;
    }

    private void fastRemove(int index) {
        assert isArray;
        if (size == SWITCH_AMT) {
            isArray = false;
            // TODO check if correct
            switch (index) {
                case 0:
                    elem0 = elementData[1];
                    elem1 = elementData[2];
                    break;
                case 1:
                    elem0 = elementData[0];
                    elem1 = elementData[2];
                    break;
                case 2:
                    elem0 = elementData[0];
                    elem1 = elementData[1];
            }
            size = 2;
        } else {
            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index + 1, elementData, index,
                                numMoved);
            elementData[--size] = null; // clear to let GC do its work
        }
    }

    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e))
                return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends E> c) {
        final int cSize = c.size();

        if (cSize == 0)
            return false;
        ensureCapacity(size + cSize); // changes isArray
        final Object[] toAdd = c.toArray();
        if (!isArray) {
            for (Object o : toAdd) {
                add((E) o);
            }
        } else {
            System.arraycopy(toAdd, 0, elementData, size, cSize);
            size = size + cSize;
        }
        return true;

    }

    @SuppressWarnings("unchecked")
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        final int cSize = c.size();
        if (cSize == 0)
            return false;

        final Object[] toAdd = c.toArray();

        ensureCapacity(size + cSize);// Modifies isArray
        if (!isArray) {
            if (cSize == 1) {
                add(index, (E) toAdd[0]);
            } else {
                assert index == 0;
                add((E) toAdd[0]);
                add((E) toAdd[1]);

            }
            return true;
        } else {
            System.arraycopy(elementData, index, elementData, index + cSize, size - index);

            System.arraycopy(toAdd, 0, elementData, index, cSize);
            size += cSize;
            return true;
        }
    }

    public boolean removeAll(Collection<?> c) {
        final int cSize = c.size();
        if (cSize == 0)
            return false;

        final Object[] arr = c.toArray();

        if (!isArray) {
            boolean removed = false;
            for (Object o : arr) {
                removed = removed | remove(o);
            }
            return removed;
        } else {
            boolean retVal = removeCollection(c, false);
            checkIsArrayChanged();
            return retVal;
        }
    }

    public boolean retainAll(Collection<?> c) {
        final int cSize = c.size();
        if (cSize == 0) {
            boolean retVal = size != 0;
            clear();
            return retVal;
        }

        final Object[] arr = c.toArray();

        if (!isArray) {
            if (size == 1) {
                if (!c.contains(elem0)) {
                    elem0 = null;
                    size = 0;
                    return true;
                }
            } else if (size == 2) {
                int idx = 1;
                if (!c.contains(elem0)) {
                    fastRemove(0);
                    idx--;
                }
                if (!c.contains(elem1)) {
                    fastRemove(idx);
                    return true;
                }
                return idx == 0;
            }
            return false;
        } else {
            boolean retVal = removeCollection(c, true);
            checkIsArrayChanged();
            return retVal;
        }
    }

    private void checkIsArrayChanged() {
        if (isArray) {
            if (size < SWITCH_AMT) {
                switch (size) {
                    case 1:
                        elem0 = elementData[0];
                        break;
                    case 2:
                        elem0 = elementData[0];
                        elem1 = elementData[1];
                        break;
                    default: // Size == 0
                        break;
                }
                isArray = false;
            }
        }
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
        return removed != 0;
    }

    public void clear() {
        if (!isArray) {
            elem0 = null;
            elem1 = null;
        } else {
            isArray = false;
            for (int i = 0; i < size; i++)
                elementData[i] = null;
        }
        size = 0;

    }

    public E get(int index) {
        rangeCheck(index);
        if (!isArray) {
            if (index == 0)
                return castUnchecked(elem0);
            if (index == 1)
                return castUnchecked(elem1);
        }
        return castUnchecked(elementData[index]);
    }

    public E set(int index, E element) {
        rangeCheck(index);
        final E oldVal;
        if (!isArray) {
            if (index == 0) {
                oldVal = castUnchecked(elem0);
                elem0 = element;
            } else {
                oldVal = castUnchecked(elem1);
                elem1 = element;
            }
        } else {
            oldVal = castUnchecked(elementData[index]);
            elementData[index] = element;
        }
        return oldVal;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);
        ensureCapacity(size + 1);

        if (!isArray) {
            if (size == 0) {
                elem0 = element;
                size = 1;
            } else { // size == 1 since otherwise isArray would be true
                if (index == 0) {
                    elem1 = elem0;
                    elem0 = element;
                } else {
                    elem1 = element;
                }
                size = 2;
            }
        } else {
            System.arraycopy(elementData, index, elementData, index + 1,
                            size - index);
            elementData[index] = element;
            size++;
        }

    }

    public E remove(int index) {
        rangeCheck(index);

        final E removedVal;
        if (!isArray) {
            if (index == 0) {
                removedVal = castUnchecked(elem0);
                elem0 = elem1;
            } else {// Index == 1
                removedVal = castUnchecked(elem1);
            }
            elem1 = null;
            size--;
        } else {
            removedVal = castUnchecked(elementData[index]);
            fastRemove(index);// changes isArray if size == 3;
        }
        return removedVal;
    }

    public int indexOf(Object o) {
        if (!isArray) {
            if (o == null) {
                switch (size) {
                    case 0:
                        return -1;
                    case 1:
                        return elem0 == null ? 0 : -1;
                    case 2:
                        if (elem0 == null) {
                            return 0;
                        } else if (elem1 == null) {
                            return 1;
                        }
                }
            } else {
                switch (size) {
                    case 0:
                        return -1;
                    case 1:
                        return o.equals(elem0) ? 0 : -1;
                    case 2:
                        if (o.equals(elem0)) {
                            return 0;
                        } else if (o.equals(elem1)) {
                            return 1;
                        }
                }
            }
        } else {
            if (o == null) {
                for (int i = 0; i < size; i++)
                    if (elementData[i] == null)
                        return i;
            } else {
                for (int i = 0; i < size; i++)
                    if (o.equals(elementData[i]))
                        return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        if (!isArray) {
            if (o == null) {
                switch (size) {
                    case 0:
                        return -1;
                    case 1:
                        return elem0 == null ? 0 : -1;
                    case 2:
                        if (elem1 == null) {
                            return 1;
                        } else if (elem0 == null) {
                            return 0;
                        }
                }
            } else {
                switch (size) {
                    case 0:
                        return -1;
                    case 1:
                        return o.equals(elem0) ? 0 : -1;
                    case 2:
                        if (o.equals(elem1)) {
                            return 1;
                        } else if (o.equals(elem0)) {
                            return 0;
                        }
                }
            }
        } else {
            if (o == null) {
                for (int i = size - 1; i >= 0; i--)
                    if (elementData[i] == null)
                        return i;
            } else {
                for (int i = size - 1; i >= 0; i--)
                    if (o.equals(elementData[i]))
                        return i;
            }
        }
        return -1;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity < SWITCH_AMT) {
            assert size < SWITCH_AMT;
        } else {
            if (minCapacity > elementData.length) {
                if (!isArray) {
                    isArray = true;
                    elementData = new Object[minCapacity];
                    elementData[0] = elem0;
                    elementData[1] = elem1;
                } else {
                    grow(minCapacity);
                }
            }
        }

    }

    private void grow(int minCapacity) {
        final int curCapacity = elementData.length;
        final int newLength = curCapacity << 1;

        elementData = Arrays.copyOf(elementData, Math.max(newLength, minCapacity));
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
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

    @SuppressWarnings("unchecked")
    private E castUnchecked(Object obj) {
        return (E) obj;
    }

    private final class Itr implements Iterator<E> {

        protected int cursor;
        protected int lastRet;

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
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            if (!isArray) {
                // TODO
                return null;
            } else {
                Object[] elementData = TwoCapacitySpecifiedArrayList.this.elementData;
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return (E) elementData[lastRet = i];
            }
        }

        /** Moved this here because also supported by Iterator in ArrayList */
        @Override
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();

            try {
                TwoCapacitySpecifiedArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
