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
