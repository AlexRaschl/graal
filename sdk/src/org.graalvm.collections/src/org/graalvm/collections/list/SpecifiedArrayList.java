package org.graalvm.collections.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.graalvm.collections.list.statistics.StatisticalSpecifiedArrayListImpl;

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
 * @author Alex R.
 *
 */

// TODO Implements List<E>
public abstract class SpecifiedArrayList<E> implements List<E> {

    Object elementData[]; // RENAMED due to compatibility issues
    // TODO restore old name and location replacing known TruffleTypes

    /**
     * Factory methods
     */

    // TODO If only one occurrence is replaced with SSAR only these instances will be tracked
    public static <E> SpecifiedArrayList<E> createNew() {
        return new StatisticalSpecifiedArrayListImpl<>();
    }

    public static <E> SpecifiedArrayList<E> createNew(final int initalCapacity) {
        return new StatisticalSpecifiedArrayListImpl<>(initalCapacity);
    }

    public static <E> SpecifiedArrayList<E> createNew(Collection<E> c) {
        return new StatisticalSpecifiedArrayListImpl<>(c);
    }

    /**
     * Returns the current size of the list
     *
     * @return the number of elements in this list
     *
     */
    public abstract int size();

    /**
     * Returns true if this list contains no elements.
     *
     * @return true if size is equal to 0
     */
    public abstract boolean isEmpty();

    /**
     * Returns false if o is null or true if at least one of the elements in this list are equal to the
     * Object o
     *
     * @param o
     * @return true if o is not null and o is in list
     */
    public abstract boolean contains(Object o);

    /**
     * An Array of the Objects that are stored in this List. The Array is <tt>size<tt> elements long.
     *
     * @return Array of Objects stored in List
     */
    public abstract Object[] toArray();

    /**
     * Adds the the Element e to the List. If the Array has reached its max capacity it will grow to
     * hold more elements. Returns <tt>true<tt> if insertion was successful.
     *
     * @param e
     * @return <tt>true<tt> if the element has been inserted successfully else <tt>false<tt>
     */
    public abstract boolean add(E e);

    /**
     * Removes the first occurrence of an Object form the list if it exists otherwise returns false
     *
     * @return <tt>true<tt> if element has been found and removed else <tt>false<tt>
     */
    public abstract boolean remove(Object o);

    /**
     * Checks if the List contains all of the elements stored in the given collection. Basically uses
     * the contains function on each element in the collection.
     *
     *
     * @param c collection of elements to be checked
     * @return <tt>true<tt> iff all elements of c are located in the List else <tt>false<tt>
     */
    public abstract boolean containsAll(Collection<?> c);

    /**
     * Adds the whole collection c to the ArrayList.
     *
     * @param c Collection of elements to be added to the list
     * @return <tt>true<tt> if all elements have been added successfully else <tt>false<tt>
     */
    public abstract boolean addAll(Collection<? extends E> c); // TODO Check if EnsureCapacity is useful here

    /**
     * DONE CHECK IF NEEDED -> Most Likely Not
     *
     * @param index
     * @param c
     * @return
     */
    public abstract boolean addAll(int index, Collection<? extends E> c);

    /**
     * Removes all the elements of c from the ArrayList.
     *
     * @param c Collection of elements to be removed from the list
     * @return <tt>true<tt> iff the list is changed else <tt>false<tt>
     */
    public abstract boolean removeAll(Collection<?> c);

    /**
     * Retains only the elements that are stored in the Collection c.
     *
     * @param c Collection of elements to be retained
     * @return <tt>true<tt> iff the list is changed else <tt>false<tt>
     */
    public abstract boolean retainAll(Collection<?> c);

    /**
     * Deletes all elements from this list.
     */
    public abstract void clear();

    /**
     * Get the Element at given Index
     *
     * @param index of element to be retrieved
     * @return Element at given index
     *
     * @throws ArrayIndexOutOfBoundsException if Index is out of range
     */
    public abstract E get(int index);

    /**
     * Replaces the Element at position index with the given element e.
     *
     * @param index of element to be retrieved
     * @param element to be stored in list at given index
     * @return Element previously located at given index
     *
     * @throws ArrayIndexOutOfBoundsException if Index is out of range
     */
    public abstract E set(int index, E element);

    /**
     * TODO CHECK IF NEEDED due to Performance
     *
     * @param index
     * @param element
     */
    public abstract void add(int index, E element);

    /**
     * Removes the Object at given Index from the List.
     *
     * @param index of Object to be removed
     * @return removed Object
     *
     * @throws ArrayIndexOutOfBoundsException if Index is out of range
     */
    public abstract E remove(int index);

    /**
     * Returns the Index of the first occurrence of an Object.
     *
     * @param o Object to search for
     * @return Index of the first occurrence in List or -1 if it is not contained
     */
    public abstract int indexOf(Object o);

    /**
     * Returns the Index of the last occurrence of an Object.
     *
     * @param o Object to search for
     * @return Index of the last occurrence in List or -1 if it is not contained
     */
    public abstract int lastIndexOf(Object o);

    /**
     * Generates a ListIterator over the elements in this List.
     *
     * @return ListIterator over the elements in this List
     */
    public abstract ListIterator<E> listIterator();

    /**
     * Generates a ListIterator over the elements in this List starting at given index.
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index is out of Range
     */
    public abstract ListIterator<E> listIterator(int index);

    /**
     *
     * @param fromIndex Index of the first Element in the new Sublist (inclusive)
     * @param toIndex Marks the EndPoint of the Sublist (exclusive)
     * @return A Sublist containing the Elements at fromIndex until toIndex
     *
     * @throws IllegalArgumentException if fromIndex > toIndex
     * @throws IndexOutOfBoundsException if one of the given Indexes is out of Range
     */
    public abstract List<E> subList(int fromIndex, int toIndex); // DONE CHECK IF NEEDED -> Most likely Not

    /**
     * Returns an Iterator over the elements in the list.
     *
     * @return Iterator over elements in the list
     */
    public abstract Iterator<E> iterator();

    /**
     * Ensures that the internal Array is at least of size capacity. Used for adding large amounts of
     * data effectively.
     *
     * @param capacity that the List should be able to hold at minimum
     */
    public abstract void ensureCapacity(int capacity);

    /**
     * Trims the capacity of the SAR to so be it's current size;
     */
    public abstract void trimToSize();

}
