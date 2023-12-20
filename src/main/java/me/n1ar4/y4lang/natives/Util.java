package me.n1ar4.y4lang.natives;

import me.n1ar4.y4lang.env.Environment;

import java.util.HashMap;

public class Util {
    public static int length(Object o) {
        if (o instanceof String) {
            return ((String) o).length();
        }
        if (o instanceof Object[]) {
            return ((Object[]) o).length;
        }
        if (o instanceof HashMap) {
            return ((HashMap<?, ?>) o).size();
        }
        return Environment.FALSE;
    }
}
