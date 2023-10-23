package me.n1ar4.jar.analyzer.utils;

public class StringUtil {
    public static boolean isNull(String str) {
        if (str == null) {
            return true;
        }
        return str.trim().isEmpty();
    }
}
