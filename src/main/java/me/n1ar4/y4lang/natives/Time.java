package me.n1ar4.y4lang.natives;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time {
    private static final long startTime = System.currentTimeMillis();

    public static int currentTime() {
        return (int) (System.currentTimeMillis() - startTime);
    }

    public static String formatTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }
}
