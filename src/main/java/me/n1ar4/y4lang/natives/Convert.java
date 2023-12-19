package me.n1ar4.y4lang.natives;

public class Convert {
    public static int toInt(Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            throw new NumberFormatException(value.toString());
        }
    }

    public static String toStr(Object value) {
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        } else {
            return value.toString();
        }
    }
}
