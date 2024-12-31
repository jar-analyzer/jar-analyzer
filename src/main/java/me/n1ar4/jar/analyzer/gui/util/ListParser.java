/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import java.util.ArrayList;

public class ListParser {
    /**
     * 黑白名单配置解析
     * 支持以下多种：
     * # 注释
     * // 注释
     * /* 注释
     * com.a.
     * com.a.Demo
     * com.a.;
     * com.a.Demo;
     * com.a.;com.b.;com.c.Demo;
     *
     * @param text 配置字符串
     * @return 解析后的结果
     */
    public static ArrayList<String> parse(String text) {
        text = text.trim();
        String[] temp = text.split("\n");
        if (temp.length == 0) {
            return new ArrayList<>();
        }
        ArrayList<String> list = new ArrayList<>();
        for (String s : temp) {
            if (s == null) {
                continue;
            }
            s = s.trim();
            if (s.isEmpty()) {
                continue;
            }
            // 防止末尾是 \r\n
            if (s.endsWith("\r")) {
                s = s.substring(0, s.length() - 1);
            }
            // 处理三种情况的注释跳过
            if (s.startsWith("#") || s.startsWith("/*") || s.startsWith("//")) {
                continue;
            }
            // 处理分号的情况
            // 如果有 com.test.a;com.test.b; 写在一行的情况
            // 应该作为多个结果处理
            if (s.contains(";")) {
                String[] items = s.split(";");
                if (items.length == 1) {
                    s = items[0];
                } else {
                    for (String item : items) {
                        if (item == null) {
                            continue;
                        }
                        item = item.trim();
                        if (item.isEmpty()) {
                            continue;
                        }
                        // 2024/09/29 兼容 java.lang.*; 类型（一个或多个 * 都要考虑）
                        while (item.endsWith("*")) {
                            item = item.substring(0, item.length() - 1);
                        }
                        item = item.replace(".", "/");
                        list.add(item);
                    }
                    // 直接跳过
                    continue;
                }
            }
            while (s.endsWith("*")) {
                s = s.substring(0, s.length() - 1);
            }
            // 2024/08/23
            // 兼容性增强 不使用分号也允许
            s = s.replace(".", "/");
            list.add(s);
        }
        return list;
    }
}
