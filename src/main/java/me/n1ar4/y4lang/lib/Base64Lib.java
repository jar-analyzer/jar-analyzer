package me.n1ar4.y4lang.lib;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.function.NativeFunction;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Base64Lib {
    private static final Logger logger = LogManager.getLogger();
    private static final String LIB_NAME = "base64";
    public static List<NativeFunction> lib = new ArrayList<>();

    static {
        try {
            Method encode = Base64Lib.class.getMethod("encode", Object.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "encode", encode));
            Method decode = Base64Lib.class.getMethod("decode", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "decode", decode));
        } catch (Exception e) {
            logger.error("load natives error: {}",e.toString());
        }
    }

    public static String encode(Object data) {
        if (data instanceof String) {
            return Base64.getEncoder().encodeToString(((String) data).getBytes(StandardCharsets.UTF_8));
        }
        if (data instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) data);
        }
        throw new Y4LangException("base64 encode error");
    }

    public static Object decode(String data) {
        return Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
    }
}
