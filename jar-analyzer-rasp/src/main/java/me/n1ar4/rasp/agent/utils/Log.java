package me.n1ar4.rasp.agent.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 为什么不用Log4j2而是自己包一个简易版
 * 因为Java Agent里无法使用Log4j2
 * 尝试解决但没有成功所以有了这个类
 */
public class Log {
    private static String getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d.getTime());
    }

    public static void info(String message) {
        System.out.printf("\u001B[32;1m[info] [%s] %s\n", getTime(), message);
    }

    public static void warn(String message) {
        System.out.printf("\u001B[33;1m[warn] [%s] %s\n", getTime(), message);
    }

    public static void error(String message) {
        System.out.printf("\u001B[31;1m[error] [%s] %s\n", getTime(), message);
    }

    public static void info(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("\u001B[32;1m[info] [%s] %s\n", getTime(), finalStr);
    }

    public static void warn(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("\u001B[33;1m[warn] [%s] %s\n", getTime(), finalStr);
    }

    public static void error(String message, Object... params) {
        String finalStr = String.format(message, params);
        System.out.printf("\u001B[31;1m[error] [%s] %s\n", getTime(), finalStr);
    }
}
