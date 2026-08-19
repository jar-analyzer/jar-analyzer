/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.simple.util;

/**
 * 字符串搜索黄金用例：javac 将 static final 常量内联进 leak() 的 LDC
 */
public class SecretHolder {
    public static final String SECRET = "GOLDEN-SECRET-1984";

    public static String leak() {
        return SECRET;
    }
}
