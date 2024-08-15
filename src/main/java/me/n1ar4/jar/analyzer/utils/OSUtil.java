package me.n1ar4.jar.analyzer.utils;

public class OSUtil {
    public static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("windows");
    }

    public static boolean isLinux() {
        String osName = System.getProperty("os.name");
        return osName.toLowerCase().contains("linux");
    }
}
