package me.n1ar4.jar.analyzer.draw.canvas;

import java.util.Formatter;
import java.util.List;

public class StringUtils {
    public static byte[] array2Bytes(String str) {
        String[] array = str.replace("[", "").replace("]", "").split(",");
        int length = array.length;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int val = Integer.parseInt(array[i].trim());
            bytes[i] = (byte) val;
        }
        return bytes;
    }

    public static String list2str(List<String> list) {
        StringBuilder sb = new StringBuilder();
        Formatter fm = new Formatter(sb);
        for (String item : list) {
            fm.format("%s%n", item);
        }
        return sb.toString();
    }
}
