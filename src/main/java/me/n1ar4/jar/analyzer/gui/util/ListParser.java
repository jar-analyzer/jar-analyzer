package me.n1ar4.jar.analyzer.gui.util;

import java.util.ArrayList;

public class ListParser {
    public static ArrayList<String> parse(String text) {
        text = text.trim();
        String[] temp = text.split("\n");
        ArrayList<String> list = new ArrayList<>();
        for (String s : temp) {
            if (s.startsWith("#")) {
                continue;
            }
            s = s.trim();
            if (s.contains(";")) {
                s = s.split(";")[0];
            } else {
                continue;
            }
            s = s.replace(".", "/");
            list.add(s);
        }
        return list;
    }
}
