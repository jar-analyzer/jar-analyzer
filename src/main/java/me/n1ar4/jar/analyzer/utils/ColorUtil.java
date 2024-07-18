package me.n1ar4.jar.analyzer.utils;

public class ColorUtil {
    private static final String ANSI_RESET = "\033[0m";
    private static final String ANSI_RED = "\033[31m";
    private static final String ANSI_GREEN = "\033[32m";
    private static final String ANSI_BLUE = "\033[34m";
    private static final String ANSI_YELLOW = "\033[33m";

    public static String red(String input) {
        return ANSI_RED + input + ANSI_RESET;
    }

    public static String green(String input) {
        return ANSI_GREEN + input + ANSI_RESET;
    }

    public static String blue(String input) {
        return ANSI_BLUE + input + ANSI_RESET;
    }

    public static String yellow(String input) {
        return ANSI_YELLOW + input + ANSI_RESET;
    }
}
