package org.graalvm.collections.test.list.statistics;

import java.util.ArrayList;
import java.util.Random;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.statistics.CSVGenerator;
import org.graalvm.collections.list.statistics.StatisticalSpecifiedArrayListImpl;
import org.graalvm.collections.list.statistics.Statistics;

public class StatisticsSimpleTest {
    private final static int TEST_SIZE = 2031;

    private final static char DATA_SEPARATOR = ';';

    public static void main(String[] args) {
        StatisticalSpecifiedArrayListImpl<Integer> testList = new StatisticalSpecifiedArrayListImpl<>(10);
        Random r = new Random();
        for (int i = 0; i < TEST_SIZE; i++) {
            testList.add(new Integer(r.nextInt(100)));
        }

        StatisticalSpecifiedArrayListImpl<char[]> testList2 = new StatisticalSpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            Integer integer = new Integer(r.nextInt(100));
            testList2.add(Integer.toBinaryString(integer).toCharArray());
        }
        testList2.size();
        testList2.contains(new Integer(10));
        testList2.get(TEST_SIZE - 1);
        testList2.ensureCapacity(TEST_SIZE * 4);
        testList2.iterator();

        StatisticalSpecifiedArrayListImpl<String[]> testList3 = new StatisticalSpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            Integer integer = new Integer(r.nextInt(100));
            String[] s = new String[1];
            s[0] = Integer.toHexString(integer);
            testList3.add(s);
        }

        StatisticalSpecifiedArrayListImpl<ArrayList<String>> testList4 = new StatisticalSpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            Integer integer = new Integer(r.nextInt(100));
            ArrayList<String> list = new ArrayList<>();
            list.add(Integer.toOctalString(integer));
            testList4.add(list);
        }

        StatisticalSpecifiedArrayListImpl<ArrayList<String>> testList5 = new StatisticalSpecifiedArrayListImpl<>();
        for (int i = 0; i < TEST_SIZE; i++) {
            Integer integer = new Integer(r.nextInt(100));
            ArrayList<String> list = new ArrayList<>();
            list.add(Integer.toOctalString(integer));
            testList5.add(list);
        }

        StatisticalSpecifiedArrayListImpl<Integer> testList6 = new StatisticalSpecifiedArrayListImpl<>();

        for (int i = 0; i < TEST_SIZE; i++) {
            testList6.add(new Integer(r.nextInt(100)));
        }

        SpecifiedArrayList<Object> testList7 = StatisticalSpecifiedArrayListImpl.createNew();
        testList7.add(new Object());
        testList7.add(new Integer(0));
        testList7.add(new String("ab"));
        testList7.add(new String("abc"));
        testList7.add(new ArrayList<Integer>());
        testList7.add(null);

        SpecifiedArrayList<Object> testList8 = SpecifiedArrayList.createNew();
        testList8.add(new NullPointerException());
        testList8.add(new Integer(0));
        testList8.add(new String("ab"));
        testList8.add(new String("abc"));
        testList8.add(new ArrayList<Integer>());
        testList8.add(null);
        testList8.add(null);

        Statistics.printOverallSummary();

        String[] data = Statistics.getOpDataLines(DATA_SEPARATOR);
        for (String s : data)
            System.out.println(s);
        System.out.println();

        data = Statistics.getTypeDataLines(DATA_SEPARATOR);
        for (String s : data)
            System.out.println(s);
        System.out.println();

        data = Statistics.getLoadFactorDataLines(DATA_SEPARATOR);
        for (String s : data)
            System.out.println(s);
        System.out.println();

        // CSVGenerator.createFileOfGlobalInfo(StatisticsSimpleTest.class.getSimpleName());

        // CSVGenerator.createFileOfTracker(1, "");

        // CSVGenerator.createFileOfEverything("");

        // CSVGenerator.createFileOfOperationDistributions(StatisticsSimpleTest.class.getSimpleName());

        // CSVGenerator.createFileOfTypeOperationDistributions(StatisticsSimpleTest.class.getSimpleName());

        // CSVGenerator.createFileOfAllocationSites(StatisticsSimpleTest.class.getSimpleName());

    }
}
