package me.n1ar4.test.util;

import java.io.IOException;

public class RunUtil {
    public static void run(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
