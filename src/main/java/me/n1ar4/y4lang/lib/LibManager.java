package me.n1ar4.y4lang.lib;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.function.NativeFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibManager {
    public static final String SEP = "::";
    private static final Map<String, List<NativeFunction>> LIB_LIST = new HashMap<>();

    static {
        LIB_LIST.put("string", StringLib.lib);
        LIB_LIST.put("base64", Base64Lib.lib);
        LIB_LIST.put("tool", ToolLib.lib);
    }

    public static void addLib(String libName, Environment env) {
        if (LIB_LIST.containsKey(libName)) {
            List<NativeFunction> list = LIB_LIST.get(libName);
            for (NativeFunction function : list) {
                if (env.get(function.getName()) != null) {
                    continue;
                }
                env.put(function.getName(), function);
            }
        }
    }
}
