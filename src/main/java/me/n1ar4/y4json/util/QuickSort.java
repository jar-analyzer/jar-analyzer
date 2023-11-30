package me.n1ar4.y4json.util;

import java.lang.reflect.Field;

/**
 * 一个简单的快排
 */
public class QuickSort {
    public static void quickSort(Field[] fields, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(fields, low, high);
            quickSort(fields, low, pivotIndex - 1);
            quickSort(fields, pivotIndex + 1, high);
        }
    }

    public static int partition(Field[] fields, int low, int high) {
        Field pivot = fields[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (fields[j].getName().compareTo(pivot.getName()) < 0) {
                i++;
                swap(fields, i, j);
            }
        }
        swap(fields, i + 1, high);
        return i + 1;
    }

    public static void swap(Field[] fields, int i, int j) {
        Field temp = fields[i];
        fields[i] = fields[j];
        fields[j] = temp;
    }
}