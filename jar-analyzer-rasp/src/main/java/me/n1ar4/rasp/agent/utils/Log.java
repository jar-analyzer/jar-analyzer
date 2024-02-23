package me.n1ar4.rasp.agent.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static String getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d.getTime());
    }

    public static void info(String message) {
        System.out.printf("[info] [%s] %s\n", getTime(), message);
    }

    public static void warn(String message) {
        System.out.printf("[warn] [%s] %s\n", getTime(), message);
    }

    public static void error(String message) {
        System.out.printf("[error] [%s] %s\n", getTime(), message);
    }

    public static void info(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("[info] [%s] %s\n", getTime(), finalStr);
    }

    public static void warn(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("[warn] [%s] %s\n", getTime(), finalStr);
    }

    public static void error(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("[error] [%s] %s\n", getTime(), finalStr);
    }
}
