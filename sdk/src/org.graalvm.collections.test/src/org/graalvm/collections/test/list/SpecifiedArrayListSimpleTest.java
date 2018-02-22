package org.graalvm.collections.test.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.SpecifiedArrayListImpl;
import org.junit.Assert;

import org.junit.*;

public class SpecifiedArrayListSimpleTest {

    private static int[] testData;
    private final static int TEST_SIZE = 10;
    private ArrayList<Integer> referenceList;
    private SpecifiedArrayList<Integer> testList;
    private final Random r = new Random();

    @BeforeClass
    public static void setup() {
        testData = new int[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            testData[i] = i;
        }
    }

    @Before
    public void setupSAR() {
        // SpecifiedArrayList
        testList = new SpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(i); // Assuming Add works like intended
        }

        // ArrayList
        referenceList = new ArrayList<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            referenceList.add(i);
        }

    }

    @After
    public void teardownSAR() {
        testList = null;
        System.gc();
    }

    @Test
    public void testAdd() {
        testList = new SpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(i);
        }

        for (int i = 0; i < TEST_SIZE; i++) {
            Assert.assertEquals("Result: ", testList.get(i), referenceList.get(i));
        }
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
        Assert.assertTrue(compareIntArrays(testList.toArray(), referenceList.toArray()));
    }

    @Test
    public void testXAll() {
        // NOTE addAll(int index... not Included)
        Assert.assertTrue(testList.containsAll(referenceList));
        Assert.assertTrue(testList.removeAll(referenceList));
        Assert.assertTrue(testList.isEmpty());
        Assert.assertTrue(testList.addAll(referenceList));
        Assert.assertFalse(testList.retainAll(referenceList));
        Assert.assertTrue(testList.containsAll(referenceList));
        testList.clear();
        Assert.assertTrue(testList.isEmpty());
    }

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
            for (int j = 0; j < TEST_SIZE; j++) {
                Assert.assertEquals(testList.get(j), referenceList.get(j));
            }
        }

    }

    @Test
    public void addAt() {
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(i, i + 1);
            referenceList.add(i, i + 1);
            Assert.assertEquals(testList.size(), referenceList.size());
            for (int j = 0; j < TEST_SIZE; j++) {
                Assert.assertEquals(testList.get(j), referenceList.get(j));
            }
        }
    }

    /** Assuming they have the same length */
    private static boolean compareIntArrays(Object[] arr1, Object[] arr2) {
        for (int i = 0; i < arr1.length; i++) {
            if ((int) arr1[i] != (int) arr2[i])
                return false;
        }
        return true;
    }
}
