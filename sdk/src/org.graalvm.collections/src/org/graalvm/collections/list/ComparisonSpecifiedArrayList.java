package org.graalvm.collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class ComparisonSpecifiedArrayList<E> extends SpecifiedArrayListImpl<E> {

    private final ArrayList<E> referenceList;
    private int itrsUsed = 0;
    private int subLists = 0;
    private int addAlls = 0;
    private int removeAlls = 0;
    private int retainAlls = 0;
    private int ensureCap = 0;

    public ComparisonSpecifiedArrayList(int initialCapacity) {
        super(initialCapacity);
        referenceList = new ArrayList<>(initialCapacity);
        assert compareLists();
    }

    public ComparisonSpecifiedArrayList() {
        super();
        referenceList = new ArrayList<>();
        assert compareLists();
    }

    public ComparisonSpecifiedArrayList(Collection<? extends E> collection) {
        super(collection);
        referenceList = new ArrayList<>(collection);
        assert compareLists();
    }

    @Override
    public int size() {
        assert compareLists();
        assert referenceList.size() == super.size();
        assert compareLists();
        return super.size();

    }

    @Override
    public boolean isEmpty() {
        assert compareLists();
        assert referenceList.isEmpty() == super.isEmpty();
        assert compareLists();
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        assert compareLists();
        boolean res = super.contains(o);
        assert (res == referenceList.contains(o));
        assert compareLists();
        return res;
    }

    @Override
    public Object[] toArray() {
        assert compareLists();
        Object[] arr = super.toArray();
        assert compareArrays(arr, referenceList.toArray(), super.size());
        assert compareLists();
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] a2 = a.clone();
        assert compareLists();
        T[] superRes = super.toArray(a);
        T[] refRes = referenceList.toArray(a2);
        assert compareLists();

        assert compareArrays(superRes, refRes, super.size());
        return super.toArray(a);
    }

    @Override
    public boolean add(E e) {
        assert compareLists();
        boolean res = super.add(e);
        assert res == referenceList.add(e);
        assert compareLists();
        return res;
    }

    @Override
    public boolean remove(Object o) {
        assert compareLists();
        boolean res = super.remove(o);
        assert res == referenceList.remove(o);
        assert compareLists();
        return res;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        assert compareLists();
        boolean res = super.containsAll(c);
        assert res == referenceList.containsAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        assert compareLists();
        addAlls++;
        boolean res = super.addAll(c);
        assert res == referenceList.addAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        assert compareLists();
        addAlls++;
        boolean res = super.addAll(index, c);
        assert res == referenceList.addAll(index, c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        assert compareLists();
        removeAlls++;
        boolean res = super.removeAll(c);
        assert res == referenceList.removeAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        assert compareLists();
        retainAlls++;
        boolean res = super.retainAll(c);
        assert res == referenceList.retainAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public void clear() {
        assert compareLists();
        super.clear();
        referenceList.clear();
        assert compareLists();
    }

    @Override
    public E get(int index) {
        assert compareLists();
        E res = super.get(index);
        assert res == referenceList.get(index);
        assert compareLists();
        return res;
    }

    @Override
    public E set(int index, E element) {
        assert compareLists();
        E res = super.set(index, element);
        assert res == referenceList.set(index, element);
        assert compareLists();
        return res;
    }

    @Override
    public void add(int index, E element) {
        assert compareLists();
        super.add(index, element);
        referenceList.add(index, element);
        assert compareLists();
    }

    @Override
    public E remove(int index) {
        assert compareLists();
        E res = super.remove(index);
        assert res == referenceList.remove(index);
        assert compareLists();
        return res;
    }

    @Override
    public int indexOf(Object o) {
        assert compareLists();
        int i = super.indexOf(o);
        assert i == referenceList.indexOf(o);
        assert compareLists();
        return i;
    }

    @Override
    public int lastIndexOf(Object o) {
        assert compareLists();
        int i = super.lastIndexOf(o);
        assert i == referenceList.lastIndexOf(o);
        assert compareLists();
        return i;
    }

    @Override
    public Iterator<E> iterator() {
        itrsUsed++;
        assert compareLists();
        assert compareItrs(new Itr(), referenceList.iterator());
        return super.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        itrsUsed++;
        assert compareLists();
        assert compareListItrs(new ListItr(0), referenceList.listIterator());
        return new ListItr(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        itrsUsed++;
        assert compareLists();
        assert compareListItrs(new ListItr(index), referenceList.listIterator(index));
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

        public boolean hasNext() {
            return cursor != ComparisonSpecifiedArrayList.this.size();
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= ComparisonSpecifiedArrayList.this.size())
                throw new NoSuchElementException();
            Object[] elementData = ComparisonSpecifiedArrayList.this.toArray();
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        /** Moved this here because also supported by Iterator in ArrayList */
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ComparisonSpecifiedArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        // DONE insert forEachRemaining
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ComparisonSpecifiedArrayList.this.size();
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ComparisonSpecifiedArrayList.this.toArray();
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
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
            return cursor != 0;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ComparisonSpecifiedArrayList.this.toArray();
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ComparisonSpecifiedArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
// if (lastRet == -1)
// throw new IllegalStateException("Remove or add Operation has been already excecuted!");
// SpecifiedArrayListImpl.this.set(lastRet, e);

        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ComparisonSpecifiedArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
// SpecifiedArrayListImpl.this.add(cursor, e);
// // TODO check if LastRet = -1 needed
// lastRet = -1;
// cursor++;
        }

    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        assert compareLists();
        subLists++;

        List<E> res = super.subList(fromIndex, toIndex);
        assert res.equals(referenceList.subList(fromIndex, toIndex));
        assert compareLists();
        return res;
    }

    @Override
    public String toString() {
        assert compareLists();
        String res = super.toString();
        assert res.equals(referenceList.toString());
        assert compareLists();
        return res;
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        ensureCap++;
        assert compareLists();
        super.ensureCapacity(minCapacity);
        referenceList.ensureCapacity(minCapacity);
        assert compareLists();
    }

    @Override
    public void trimToSize() {
        assert compareLists();
        super.trimToSize();
        referenceList.trimToSize();
        assert compareLists();
    }

    @Override
    public void sort(Comparator<? super E> c) {
        assert compareLists();
        super.sort(c);
        referenceList.sort(c);
        assert compareLists();
    }

    private boolean compareLists() {
        if (super.size() != referenceList.size()) {
            System.err.println("Sizes dont match: " + super.size() + " : " + referenceList.size() + "Itrs Used: " + itrsUsed + " Sublists used: " + subLists + " AddAlls used: " + addAlls +
                            " Remove Alls: " + removeAlls + " Retain Alls: " + retainAlls + " Ensure Capacities: " + ensureCap);
            return false;
        }

        Object[] superElems = super.toArray();
        Object[] refElems = referenceList.toArray();

        for (int i = 0; i < super.size(); i++) {
            if (superElems[i] != refElems[i]) {
                System.err.println(
                                "CompareLists: " + superElems[i].toString() + " is not equal to " + refElems[i].toString() + "Index: " + i + "Itrs Used: " + itrsUsed + " Sublists used: " + subLists +
                                                " AddAlls used: " + addAlls + " Remove Alls: " + removeAlls + " Retain Alls: " + retainAlls);
                return false;
            }

        }
        return true;
    }

    private boolean compareArrays(Object[] a, Object[] b, int length) {
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i]) {
                System.err.println(
                                "CompareArrays: " + a[i].toString() + " is not equal to " + b[i].toString() + "Itrs Used: " + itrsUsed + " Sublists used: " + subLists + " AddAlls used: " + addAlls +
                                                " Remove Alls: " + removeAlls + " Retain Alls: " + retainAlls);
                return false;
            }

        }
        return true;
    }

    private boolean compareItrs(Iterator<E> itr1, Iterator<E> itr2) {
        while (itr1.hasNext()) {
            if (!itr2.hasNext())
                return false;
            if (itr1.next() != itr2.next())
                return false;
        }
        return !itr2.hasNext();
    }

    private boolean compareListItrs(ListIterator<E> itr1, ListIterator<E> itr2) {
        while (itr1.hasNext()) {
            if (!itr2.hasNext())
                return false;
            if (itr1.next() != itr2.next())
                return false;
        }
        return !itr2.hasNext();
    }

}
