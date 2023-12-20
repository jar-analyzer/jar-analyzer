package me.n1ar4.y4lang.lib;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.y4lang.function.NativeFunction;
import me.n1ar4.y4lang.util.EncodeUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ToolLib {
    private static final Logger logger = LogManager.getLogger();
    private static final String LIB_NAME = "tool";
    public static List<NativeFunction> lib = new ArrayList<>();

    static {
        try {
            Method exec = ToolLib.class.getMethod("exec", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "exec", exec));
            Method getPowershellCommand = ToolLib.class.getMethod("getPowershellCommand", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "getPowershellCommand", getPowershellCommand));
            Method getBashCommand = ToolLib.class.getMethod("getBashCommand", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "getBashCommand", getBashCommand));
            Method getStringCommand = ToolLib.class.getMethod("getStringCommand", String.class);
            lib.add(new NativeFunction(LIB_NAME + LibManager.SEP + "getStringCommand", getStringCommand));
        } catch (Exception e) {
            logger.error("load natives error: {}",e.toString());
        }
    }

    public static String exec(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            StringBuilder outStr = new StringBuilder();
            java.io.InputStreamReader resultReader = new java.io.InputStreamReader(process.getInputStream());
            java.io.BufferedReader stdInput = new java.io.BufferedReader(resultReader);
            String s;
            while ((s = stdInput.readLine()) != null) {
                outStr.append(s).append("\n");
            }
            return outStr.toString();
        } catch (Exception e) {
            logger.error("exec error: {}",e.toString());
        }
        return null;
    }

    public static String getPowershellCommand(String cmd) {
        return EncodeUtil.getPowershellCommand(cmd);
    }

    public static String getBashCommand(String cmd) {
        return EncodeUtil.getBashCommand(cmd);
    }

    public static String getStringCommand(String cmd) {
        return EncodeUtil.getStringCommand(cmd);
    }
}
