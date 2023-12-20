package me.n1ar4.y4lang.lib;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.function.NativeFunction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StringLib {

    private static final String LIB_NAME = "string";
    public static List<NativeFunction> lib = new ArrayList<>();

    static {
        try {
            Method isEmpty = StringLib.class.getMethod("isEmpty", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "isEmpty", isEmpty));
            Method contains = StringLib.class.getMethod("contains", String.class, String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "contains", contains));
            Method split = StringLib.class.getMethod("split", String.class, String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "split", split));
            Method substr = StringLib.class.getMethod("substr", String.class, int.class, int.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "substr", substr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int isEmpty(String data) {
        if (data == null) {
            return Environment.TRUE;
        }
        if (data.trim().isEmpty()) {
            return Environment.TRUE;
        }
        return Environment.FALSE;
    }

    public static int contains(String str, String data) {
        if (str == null || data == null) {
            return Environment.FALSE;
        }
        if (str.contains(data)) {
            return Environment.TRUE;
        }
        return Environment.FALSE;
    }

    public static Object[] split(String str, String data) {
        return str.split(data);
    }

    public static String substr(String str, int start, int end) {
        return str.substring(start, end);
    }
}
