package org.graalvm.collections.test.list;

import java.util.ArrayList;

import org.graalvm.collections.list.SpecifiedArrayList;
import org.graalvm.collections.list.TwoCapacitySpecifiedArrayList;

public class TestUtilities {

    public static boolean compareLists(SpecifiedArrayList<?> sar, ArrayList<?> ar) {
        return compareArrays(sar.toArray(), ar.toArray());

    }

    public static boolean compareLists(TwoCapacitySpecifiedArrayList<?> sar, ArrayList<?> ar) {
        return compareArrays(sar.toArray(), ar.toArray());

    }

    public static boolean compareArrays(Object[] arr1, Object[] arr2) {
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
//
// static boolean compareIntArrays(Object[] arr1, Object[] arr2) {
// for (int i = 0; i < arr1.length; i++) {
// if (arr1[i] == null && arr2[i] == null) {
// continue;
// } else if (arr1[i] == null || arr2[i] == null) {
// return false;
// } else if (((Integer) arr1[i]).intValue() != ((Integer) arr2[i]).intValue()) {
//
// return false;
// }
//
// }
// return true;
// }
//
// public static boolean compareIntLists(SpecifiedArrayList<Integer> sar, ArrayList<Integer> ar) {
// return compareIntArrays(sar.toArray(), ar.toArray());
// }
//
// /** Assuming they have the same length */
// static boolean compareStringArrays(Object[] arr1, Object[] arr2) {
// for (int i = 0; i < arr1.length; i++) {
// if (arr1[i] == null && arr2[i] == null) {
// continue;
// } else if (arr1[i] == null || arr2[i] == null) {
// return false;
// } else if (!arr1[i].equals(arr2[i])) {
// return false;
// }
// }
// return true;
// }
//
// static boolean compareStringLists(SpecifiedArrayList<String> sar, ArrayList<String> ar) {
// return compareStringArrays(sar.toArray(), ar.toArray());
// }

}
