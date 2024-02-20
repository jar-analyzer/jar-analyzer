package me.n1ar4.jar.analyzer.gui;

public class GlobalOptions {
    public static final int CHINESE = 1;
    public static final int ENGLISH = 2;
    private static int LANGUAGE;

    static {
        LANGUAGE = ENGLISH;
    }

    public static void setLang(int lang) {
        if (lang != CHINESE && lang != ENGLISH) {
            throw new RuntimeException("invalid language");
        }
        LANGUAGE = lang;
    }

    public static int getLang() {
        return LANGUAGE;
    }
}
