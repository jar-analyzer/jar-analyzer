package me.n1ar4.jar.analyzer.gui.util;

import com.formdev.flatlaf.FlatIntelliJLaf;

public class JarAnalyzerLaf extends FlatIntelliJLaf {
    public static boolean setup() {
        return setup(new JarAnalyzerLaf());
    }
}
