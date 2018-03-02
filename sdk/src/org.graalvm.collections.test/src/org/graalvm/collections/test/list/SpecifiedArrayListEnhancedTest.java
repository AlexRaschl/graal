package org.graalvm.collections.test.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.SpecifiedArrayListImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SpecifiedArrayListEnhancedTest {
    private static String[] testData;
    private final static int TEST_SIZE = 2031;
    private ArrayList<String> referenceList;
    private SpecifiedArrayList<String> testList;
    private final Random r = new Random();

    @BeforeClass
    public static void setup() {
        testData = new String[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            testData[i] = "String: " + i + ";";
        }
    }

    @Before
    public void setupSAR() {
        // SpecifiedArrayList
        testList = new SpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(testData[i]); // Assuming Add works like intended
        }

        // ArrayList
        referenceList = new ArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            referenceList.add(testData[i]);
        }

    }

    @After
    public void teardownSAR() {
        testList = null;
        referenceList = null;
        System.gc();
    }

    @Test
    public void testAddNull() {
        for (int i = 0; i < TEST_SIZE / 10; i++) {
            testList.add(null);
            referenceList.add(null);
        }
        Assert.assertTrue(compareLists(testList, referenceList));

        for (int i = 0; i < TEST_SIZE / 10; i++) {
            testList.add(i, null);
            referenceList.add(i, null);
        }
        Assert.assertTrue(compareLists(testList, referenceList));
    }

    @Test
    public void testRemoveNull() {
        for (int i = 0; i < TEST_SIZE / 10; i++) {
            testList.add(null);
            referenceList.add(null);
        }
        Assert.assertTrue(compareLists(testList, referenceList));

        for (int i = 0; i < TEST_SIZE / 10; i++) {
            testList.add(i, null);
            referenceList.add(i, null);
        }
        Assert.assertTrue(compareLists(testList, referenceList));

        for (int i = 0; i < TEST_SIZE / 10; i++) {
            Assert.assertTrue(testList.remove(null));
            Assert.assertTrue(referenceList.remove(null));
        }
        Assert.assertTrue(compareLists(testList, referenceList));
        for (int i = 0; i < TEST_SIZE / 10; i++) {
            Assert.assertTrue(testList.remove(null));
            Assert.assertTrue(referenceList.remove(null));
        }
        Assert.assertTrue(testList.size() == TEST_SIZE);
        Assert.assertTrue(referenceList.size() == TEST_SIZE);
    }

    @Test
    public void testRemoveNonExistent() {
        Assert.assertFalse(testList.remove("Not Exististing String"));
    }

    @Test
    public void testSetOfOperations() {
        testList = new SpecifiedArrayListImpl<>(20);
        referenceList = new ArrayList<>(20);

        testList.add("ABC");
        testList.add(0, "def");
        String[] arr = {"a", "b", "c", "d"};
        testList.addAll(Arrays.asList(arr));
        Assert.assertFalse(testList.contains(null));
        Assert.assertTrue(testList.contains("a"));
        Assert.assertTrue(testList.contains("ABC"));
        Assert.assertTrue(testList.indexOf("a") == 2);
        Assert.assertTrue(!testList.isEmpty());
        Assert.assertTrue(testList.containsAll(Arrays.asList(arr)));
        Assert.assertTrue(testList.retainAll(Arrays.asList(arr)));
        Assert.assertTrue(testList.size() == 4);
        Assert.assertTrue(testList.removeAll(Arrays.asList(arr)));
        Assert.assertTrue(testList.isEmpty() && testList.size() == 0);
        //
        testList = new SpecifiedArrayListImpl<>(Arrays.asList(arr));
        Assert.assertTrue(testList.size() == 4);
        testList.ensureCapacity(1000);
        testList.trimToSize();
        testList.clear();
        Assert.assertTrue(testList.isEmpty());
        testList.clear();
        Assert.assertFalse(testList.contains(null));
        Assert.assertFalse(testList.contains("a"));
        //
        testList = new SpecifiedArrayListImpl<>();
        testList.add("ABC");
        testList.add(0, "def");
        testList.addAll(Arrays.asList(arr));
        Iterator<String> itr = testList.iterator();
        Assert.assertEquals("def", itr.next());
        itr.remove();
        Assert.assertTrue(testList.size() == 5);
        try {
            itr.remove();
            Assert.fail("IllegalStateException should have been thrown instead!");
        } catch (IllegalStateException e) {

        }
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals("ABC", itr.next());

        itr.remove();
        Assert.assertTrue(testList.size() == 4);
        itr.next();
        itr.next();
        itr.next();
        itr.next();

        try {
            itr.next();
            Assert.fail("NoSuchElementException should have been thrown instead!");
        } catch (NoSuchElementException e) {

        }

        ListIterator<String> listItr = testList.listIterator();
        Assert.assertFalse(listItr.hasPrevious());
        Assert.assertTrue(listItr.hasNext());
        Assert.assertEquals(listItr.next(), "a");

        Assert.assertTrue(listItr.hasPrevious());
        listItr.set("Z");
        Assert.assertTrue(listItr.hasNext());
        Assert.assertEquals(listItr.next(), "b");

        Assert.assertEquals(listItr.previous(), "b");
        Assert.assertEquals(listItr.previous(), "Z");

        listItr.add("added");
        listItr.add("added2");

        Assert.assertTrue(listItr.hasPrevious() && listItr.nextIndex() == 2);

        try {
            listItr.set("notPossible");
            Assert.fail("NoSuchElementException should have been thrown instead!");
        } catch (IllegalStateException e) {

        }

        Assert.assertEquals(listItr.next(), "Z");
        Assert.assertTrue(listItr.previousIndex() == 2);

        testList.clear();

    }

    /** Assuming they have the same length */
    private static boolean compareStringArrays(Object[] arr1, Object[] arr2) {
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == null && arr2[i] == null) {
                continue;
            } else if (arr1[i] == null || arr2[i] == null) {
                return false;
            } else if (!arr1[i].equals(arr2[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareLists(SpecifiedArrayList<String> sar, ArrayList<String> ar) {
        return compareStringArrays(sar.toArray(), ar.toArray());
    }
}
