package me.n1ar4.y4lang.lib;

import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.function.NativeFunction;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class HttpLib {
    private static final String LIB_NAME = "http";
    public static List<NativeFunction> lib = new ArrayList<>();

    static {
        try {
            Method doGet = HttpLib.class.getMethod("doGet",
                    String.class, Object.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "doGet", doGet));
            Method doPost = HttpLib.class.getMethod("doPost",
                    String.class, Object.class, String.class, String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "doPost", doPost));
            Method doRequest = HttpLib.class.getMethod("doRequest", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "doRequest", doRequest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object doGet(String url, Object headers) {
        checkMap(headers);
        try {
           //todo
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object doPost(String url, Object headers, String body, String contentType) {
        checkMap(headers);
        try {
            //todo
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object doRequest(String request) {
        try {
            //todo
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void checkMap(Object headers) {
        if (!(headers instanceof HashMap)) {
            throw new Y4LangException("headers error");
        }
    }
}
