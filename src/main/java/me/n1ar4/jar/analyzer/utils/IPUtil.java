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

package me.n1ar4.jar.analyzer.utils;

import java.util.regex.Pattern;

public class IPUtil {
    private static final String IPV4_PATTERN =
            "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})\\." +
                    "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})$";
    private static final String IPV6_PATTERN =
            "(([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:))|" +
                    "(([0-9a-fA-F]{1,4}:){1,7}:)|" +
                    "(([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})|" +
                    "(([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2})|" +
                    "(([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3})|" +
                    "(([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4})|" +
                    "(([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5})|" +
                    "([0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6}))|" +
                    "(::([0-9a-fA-F]{1,4}:){0,5}([0-9a-fA-F]{1,4}))|" +
                    "(::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4})";
    private static final Pattern IPv4_PATTERN_COMPILED = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPv6_PATTERN_COMPILED = Pattern.compile(IPV6_PATTERN);

    public static boolean isValidIPAddress(String ipAddress) {
        return isValidIPv4(ipAddress) || isValidIPv6(ipAddress);
    }

    private static boolean isValidIPv4(String ipAddress) {
        return IPv4_PATTERN_COMPILED.matcher(ipAddress).matches();
    }

    private static boolean isValidIPv6(String ipAddress) {
        return IPv6_PATTERN_COMPILED.matcher(ipAddress).matches();
    }
}
