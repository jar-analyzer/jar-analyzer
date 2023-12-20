package me.n1ar4.y4lang.parser;

import java.util.HashMap;

public class Operators extends HashMap<String, Precedence> {
    public static boolean LEFT = true;
    public static boolean RIGHT = false;

    public void add(String name, int pre, boolean leftAssoc) {
        put(name, new Precedence(pre, leftAssoc));
    }
}