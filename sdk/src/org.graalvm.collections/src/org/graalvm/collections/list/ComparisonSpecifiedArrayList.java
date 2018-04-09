package org.graalvm.collections.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ComparisonSpecifiedArrayList<E> extends SpecifiedArrayListImpl<E> {

    private ArrayList<E> referenceList;

    public ComparisonSpecifiedArrayList(int initialCapacity) {
        super(initialCapacity);
        referenceList = new ArrayList<>(initialCapacity);
    }

    public ComparisonSpecifiedArrayList() {
        super();
        referenceList = new ArrayList<>();
    }

    public ComparisonSpecifiedArrayList(Collection<? extends E> collection) {
        super(collection);
        referenceList = new ArrayList<>(collection);
    }

    @Override
    public int size() {
        assert referenceList.size() == super.size();
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        assert referenceList.isEmpty() == super.isEmpty();
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        boolean res = super.contains(o);
        assert (res == referenceList.contains(o));
        return res;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] superRes = super.toArray(a);
        T[] refRes = referenceList.toArray(a);
        assert compareArrays(superRes, refRes, super.size());
        return superRes;
    }

    @Override
    public boolean add(E e) {
        boolean res = super.add(e);
        assert res == referenceList.add(e);
        assert compareLists() == true;
        return res;
    }

    @Override
    public boolean remove(Object o) {
        boolean res = super.remove(o);
        assert res == referenceList.remove(o);
        compareLists();
        return res;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean res = super.containsAll(c);
        assert res == referenceList.containsAll(c);
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean res = super.addAll(c);
        assert res == referenceList.containsAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean res = super.addAll(index, c);
        assert res == referenceList.addAll(index, c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = super.removeAll(c);
        assert res == referenceList.removeAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean res = super.retainAll(c);
        assert res == referenceList.retainAll(c);
        assert compareLists();
        return res;
    }

    @Override
    public void clear() {
        super.clear();
        referenceList.clear();
        assert compareLists();
    }

    @Override
    public E get(int index) {
        E res = super.get(index);
        assert res == referenceList.get(index);
        return res;
    }

    @Override
    public E set(int index, E element) {
        E res = super.set(index, element);
        assert res == referenceList.set(index, element);
        assert compareLists();
        return res;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        referenceList.add(index, element);
        assert compareLists();
    }

    @Override
    public E remove(int index) {
        E res = super.remove(index);
        assert res == referenceList.remove(index);
        compareLists();
        return res;
    }

    @Override
    public int indexOf(Object o) {
        int i = super.indexOf(o);
        assert i == referenceList.indexOf(o);
        return i;
    }

    @Override
    public int lastIndexOf(Object o) {
        int i = super.lastIndexOf(o);
        assert i == referenceList.lastIndexOf(o);
        return i;
    }

    @Override
    public Iterator<E> iterator() {
        assert compareItrs(super.iterator(), referenceList.iterator());
        return super.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        assert compareListItrs(super.listIterator(), referenceList.listIterator());
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        assert compareListItrs(super.listIterator(index), referenceList.listIterator(index));
        return super.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        List<E> res = super.subList(fromIndex, toIndex);
        assert res.equals(referenceList.subList(fromIndex, toIndex));
        return res;
    }

    @Override
    public String toString() {
        String res = super.toString();
        assert res.equals(referenceList.toString());
        return res;
    }

    private boolean compareLists() {
        if (super.size() != referenceList.size())
            return false;

        Object[] superElems = super.elementData;
        Object[] refElems = referenceList.toArray();

        for (int i = 0; i < super.size(); i++) {
            if (superElems[i] != refElems[i])
                return false;
        }
        return true;
    }

    private static boolean compareArrays(Object[] a, Object[] b, int length) {
        for (int i = 0; i < length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

    private boolean compareItrs(Iterator<E> itr1, Iterator<E> itr2) {
        while (itr1.hasNext()) {
            if (!itr2.hasNext())
                return true;
            if (itr1.next() != itr2.next())
                return false;
        }
        return !itr2.hasNext();
    }

    private boolean compareListItrs(ListIterator<E> itr1, ListIterator<E> itr2) {
        while (itr1.hasNext()) {
            if (!itr2.hasNext())
                return true;
            if (itr1.next() != itr2.next())
                return false;
        }
        return !itr2.hasNext();
    }

}
