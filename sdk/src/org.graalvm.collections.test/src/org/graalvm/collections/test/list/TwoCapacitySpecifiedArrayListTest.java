package org.graalvm.collections.test.list;

import java.util.ArrayList;
import java.util.Random;

import org.graalvm.collections.list.TwoCapacitySpecifiedArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TwoCapacitySpecifiedArrayListTest {

    /**
     * TODO REPLACE get comparison with TestUtils
     */
    private static Integer[] testData;
    private final static int TEST_SIZE = 2031;
    private ArrayList<Integer> referenceList;
    private TwoCapacitySpecifiedArrayList<Integer> testList;
    private final Random r = new Random();

    @BeforeClass
    public static void setup() {
        testData = new Integer[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            testData[i] = new Integer(i);
        }
    }

    @Before
    public void setupSAR() {
        // SpecifiedArrayList
        testList = new TwoCapacitySpecifiedArrayList<>();
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
    public void testAdd() {
        testList = new TwoCapacitySpecifiedArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(i);
        }

// for (int i = 0; i < TEST_SIZE; i++) {
// Assert.assertEquals("Result: ", testList.get(i), referenceList.get(i));
// }
        TestUtilities.compareLists(testList, referenceList);
    }

    @Test
    public void testSize() {
        Assert.assertEquals("Size check: ", testList.size(), referenceList.size());
    }

    @Test
    public void testSimpleRemove() {
        for (int i = TEST_SIZE - 1; i >= 0; i--) {
            Assert.assertEquals("Result: ", testList.remove(new Integer(i)), referenceList.remove(new Integer(i)));
            Assert.assertEquals("Size check:", testList.size(), referenceList.size());
        }
        Assert.assertTrue(testList.isEmpty());
        Assert.assertTrue(referenceList.isEmpty());
    }

    @Test
    public void testRemoveAtIndex() {
        for (int i = 0; i < TEST_SIZE; i++) {
            int index = r.nextInt(testList.size());
            Assert.assertEquals("Result: ", testList.remove(index), referenceList.remove(index));
            Assert.assertEquals("Size check:", testList.size(), referenceList.size());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidRemoveAt() {
        referenceList.remove(TEST_SIZE);
    }

    @Test
    public void testRemoveObject() {
        for (int i = 0; i < TEST_SIZE; i++) {
            int toRemove = r.nextInt(testList.size());
            Assert.assertEquals("Result: ", testList.remove(new Integer(toRemove)), referenceList.remove(new Integer(toRemove)));
            Assert.assertEquals("Size check:", testList.size(), referenceList.size());
        }
    }

    @Test
    public void testAddRemove() {
        for (int i = 0; i < TEST_SIZE; i++) {
            if (i % 2 == 0) {
                int index = r.nextInt(testList.size());
                Assert.assertEquals("Result: ", testList.remove(index), referenceList.remove(index));
                Assert.assertEquals("Size check:", testList.size(), referenceList.size());
            } else {
                int toAdd = r.nextInt();
                Assert.assertEquals("Size check:", testList.add(toAdd), referenceList.add(toAdd));
            }

        }
    }

    @Test
    public void testContains() {
        for (int i = 0; i < TEST_SIZE; i++) {
            int n = r.nextInt(TEST_SIZE * 3);
            Assert.assertEquals("Result: ", testList.contains(n), referenceList.contains(n));
        }
    }

    @Test
    public void testToArray() {
        Assert.assertTrue(TestUtilities.compareLists(testList, referenceList));
    }

// @Test
// public void testXAll() {
// // NOTE addAll(int index... not Included)
// Assert.assertTrue(testList.containsAll(referenceList));
// Assert.assertTrue(testList.removeAll(referenceList));
// Assert.assertTrue(testList.isEmpty());
// Assert.assertTrue(testList.addAll(referenceList));
// Assert.assertFalse(testList.retainAll(referenceList));
// Assert.assertTrue(testList.containsAll(referenceList));
// testList.clear();
// Assert.assertTrue(testList.isEmpty());
// }

    @Test
    public void testClear() {
        Assert.assertEquals(testList.size(), referenceList.size());
        testList.clear();
        referenceList.clear();
        Assert.assertEquals(testList.size(), referenceList.size());
        Assert.assertTrue(testList.isEmpty());
    }

    @Test
    public void testGet() {
        for (int i = 0; i < TEST_SIZE; i++) {
            Assert.assertEquals(testList.get(i), referenceList.get(i));
        }
    }

    @Test
    public void testSet() {
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.set(i, i + 1);
            referenceList.set(i, i + 1);
            Assert.assertEquals(testList.size(), referenceList.size());
            TestUtilities.compareLists(testList, referenceList);
// for (int j = 0; j < TEST_SIZE; j++) {
// Assert.assertEquals(testList.get(j), referenceList.get(j));
// }
        }

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidSet() {
        referenceList.set(TEST_SIZE, new Integer(TEST_SIZE));
    }

    @Test
    public void addAt() {
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(i, i + 1);
            referenceList.add(i, i + 1);
            Assert.assertEquals(testList.size(), referenceList.size());
            TestUtilities.compareLists(testList, referenceList);
// for (int j = 0; j < TEST_SIZE; j++) {
// Assert.assertEquals(testList.get(j), referenceList.get(j));
// }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidAddAt() {
        referenceList.add(Integer.MAX_VALUE - 10, new Integer(TEST_SIZE));
    }

    @Test
    public void testIndexOf() {
        for (int i = 0; i < TEST_SIZE; i++) {
            Assert.assertEquals(testList.indexOf(testData[i]), referenceList.indexOf(testData[i]));
        }
    }

    @Test
    public void testLastIndexOf() {
        for (int i = 0; i < TEST_SIZE; i++) {
            Assert.assertEquals(testList.lastIndexOf(testData[i]), referenceList.lastIndexOf(testData[i]));
        }
    }

// @Test
// public void testIterator() {
// Iterator<Integer> testIt = testList.iterator();
// Iterator<Integer> referenceIt = referenceList.iterator();
//
// while (testIt.hasNext()) {
// Integer curr = testIt.next();
// if (referenceIt.hasNext()) {
// Assert.assertSame(curr, referenceIt.next());
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
//
// }
//
// @Test
// public void testIteratorRemove() {
// Iterator<Integer> testIt = testList.iterator();
// Iterator<Integer> referenceIt = referenceList.iterator();
//
// while (testIt.hasNext()) {
// testIt.next();
// if (referenceIt.hasNext()) {
// referenceIt.next();
// referenceIt.remove();
// testIt.remove();
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
// // System.out.println("Remaining size of testList is:" + testList.size());
// // System.out.println("Remaining size of referenceList is: " + referenceList.size());
// Assert.assertEquals(testList.size(), referenceList.size());
// Assert.assertTrue(testList.isEmpty());
// }
//
// @Test(expected = NoSuchElementException.class)
// public void testInvalidIteratorNext() {
// Iterator<Integer> testIt = testList.iterator();
// for (int i = 0; i <= TEST_SIZE + 1; i++)
// testIt.next();
// }
//
// @Test
// public void testListIteratorConstructor() {
// for (int i = 0; i < TEST_SIZE; i++) {
// ListIterator<Integer> testIt = testList.listIterator(i);
// ListIterator<Integer> referenceIt = referenceList.listIterator(i);
// Assert.assertEquals(testIt.nextIndex(), referenceIt.nextIndex());
// Assert.assertEquals(testIt.previousIndex(), referenceIt.previousIndex());
// }
// }
//
// @Test
// public void testListIteratorNext() {
// ListIterator<Integer> testIt = testList.listIterator();
// ListIterator<Integer> referenceIt = referenceList.listIterator();
//
// while (testIt.hasNext()) {
// Assert.assertEquals(testIt.nextIndex(), referenceIt.nextIndex());
// Assert.assertEquals(testIt.previousIndex(), referenceIt.previousIndex());
// Integer curr = testIt.next();
// if (referenceIt.hasNext()) {
// Assert.assertSame(curr, referenceIt.next());
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
// }

// @Test
// public void testListIteratorPrevious() {
// ListIterator<Integer> testIt = testList.listIterator(TEST_SIZE - 1);
// ListIterator<Integer> referenceIt = referenceList.listIterator(TEST_SIZE - 1);
//
// while (testIt.hasPrevious()) {
// Assert.assertEquals(testIt.previousIndex(), referenceIt.previousIndex());
// Integer curr = testIt.previous();
// if (referenceIt.hasNext()) {
// Assert.assertSame(curr, referenceIt.previous());
// } else {
// Assert.fail("Reference Iterator has already reached start!");
// }
// }
// }

// @Test
// public void testListIteratorSet() {
// ListIterator<Integer> testIt = testList.listIterator();
// ListIterator<Integer> referenceIt = referenceList.listIterator();
//
// while (testIt.hasNext()) {
// Integer currTest = testIt.next();
// if (referenceIt.hasNext()) {
// Integer currReference = referenceIt.next();
// Assert.assertSame(currTest, currReference);
// Integer rInt = r.nextInt(TEST_SIZE * 2);
// testIt.set(rInt);
// referenceIt.set(rInt);
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
// Assert.assertTrue(TestUtilities.compareLists(testList, referenceList));
// }

// @Test
// public void testListIteratorAdd() {
// ListIterator<Integer> testIt = testList.listIterator();
// ListIterator<Integer> referenceIt = referenceList.listIterator();
//
// while (testIt.hasNext()) {
// Integer currTest = testIt.next();
// if (referenceIt.hasNext()) {
// Integer currReference = referenceIt.next();
// Assert.assertSame(currTest, currReference);
// Integer rInt = r.nextInt(TEST_SIZE * 2);
// referenceIt.add(rInt);
// testIt.add(rInt);
//
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
// Assert.assertTrue(TestUtilities.compareLists(testList, referenceList));
// }

// @Test
// public void testListIteratorRemove() {
// Iterator<Integer> testIt = testList.listIterator();
// Iterator<Integer> referenceIt = referenceList.listIterator();
//
// while (testIt.hasNext()) {
// testIt.next();
// if (referenceIt.hasNext()) {
// referenceIt.next();
// referenceIt.remove();
// testIt.remove();
// } else {
// Assert.fail("Reference Iterator has already reached end!");
// }
// }
// Assert.assertEquals(testList.size(), referenceList.size());
// Assert.assertTrue(testList.isEmpty());
// }

    @Test
    public void testAddNull() {
        testList = new TwoCapacitySpecifiedArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(null);
        }
    }

    // @Test
    // public void testSortWithListItr() {
    // testList = new TwoCapacitySpecifiedArrayList<>();
    // for (int i = 0; i < TEST_SIZE; i++) {
    // testList.add(r.nextInt(TEST_SIZE / 10));
    // }
    // Collections.sort(testList);
    // Collections.sort(testList, (k, j) -> k - j);
    // }

}
