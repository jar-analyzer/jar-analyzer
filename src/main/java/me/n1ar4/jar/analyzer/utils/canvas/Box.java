/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils.canvas;


public enum Box {
    SPACE(0b0000, " "),
    HORIZONTAL(0b1010, "─"),
    VERTICAL(0b0101, "│"),
    DOWN_AND_RIGHT(0b0011, "┌"),
    DOWN_AND_LEFT(0b1001, "┐"),
    UP_AND_RIGHT(0b0110, "└"),
    UP_AND_LEFT(0b1100, "┘"),
    VERTICAL_AND_RIGHT(0b0111, "├"),
    VERTICAL_AND_LEFT(0b1101, "┤"),
    DOWN_AND_HORIZONTAL(0b1011, "┬"),
    UP_AND_HORIZONTAL(0b1110, "┴"),
    VERTICAL_AND_HORIZONTAL(0b1111, "┼"),
    ;

    public final int flag;
    public final String val;

    Box(int flag, String val) {
        this.flag = flag;
        this.val = val;
    }

    public Box merge(Box another) {
        int flag = this.flag | another.flag;
        return fromFlag(flag);
    }

    public static Box merge(String val1, String val2) {
        Box one = fromString(val1);
        Box another = fromString(val2);
        return one.merge(another);
    }

    public static Box fromString(String val) {
        Box[] values = values();
        for (Box item : values) {
            if (item.val.equals(val)) {
                return item;
            }
        }
        throw new RuntimeException("Unexpected Value: " + val);
    }

    public static Box fromFlag(int flag) {
        Box[] values = values();
        for (Box item : values) {
            if (item.flag == flag) {
                return item;
            }
        }
        throw new RuntimeException("Unexpected flag: " + flag);
    }

    public static boolean isValid(String val) {
        Box[] values = values();
        for (Box item : values) {
            if (item.val.equals(val)) {
                return true;
            }
        }
        return false;
    }
}
