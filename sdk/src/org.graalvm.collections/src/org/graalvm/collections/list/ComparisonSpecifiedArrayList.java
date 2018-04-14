package org.graalvm.collections.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class ComparisonSpecifiedArrayList<E> extends SpecifiedArrayListImpl<E> {

    private ArrayList<E> referenceList;
    private int itrsUsed = 0;
    private int subLists = 0;
    private int addAlls = 0;
    private int removeAlls = 0;
    private int retainAlls = 0;

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
        assert compareItrs(super.iterator(), referenceList.iterator());
        return super.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        itrsUsed++;
        assert compareLists();
        assert compareListItrs(super.listIterator(), referenceList.listIterator());
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        itrsUsed++;
        assert compareLists();
        assert compareListItrs(super.listIterator(index), referenceList.listIterator(index));
        return super.listIterator(index);
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

    private boolean compareLists() {
        if (super.size() != referenceList.size()) {
            System.err.println("Sizes dont match: " + super.size() + " : " + referenceList.size() + "Itrs Used: " + itrsUsed + " Sublists used: " + subLists + " AddAlls used: " + addAlls +
                            " Remove Alls: " + removeAlls + " Retain Alls: " + retainAlls);
            return false;
        }

        Object[] superElems = super.elementData;
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
