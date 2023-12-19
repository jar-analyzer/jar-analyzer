package me.n1ar4.y4lang.util;

public class StringUtil {
    public static boolean isEmpty(String input){
        return input==null || input.equals("")|| input.trim().equals("");
    }

    public static String getExtName(String input){
        String[] split = input.split("\\.");
        if(split.length<2){
            return "";
        }
        return split[split.length-1];
    }
}
