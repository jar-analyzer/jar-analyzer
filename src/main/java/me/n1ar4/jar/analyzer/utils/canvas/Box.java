/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
