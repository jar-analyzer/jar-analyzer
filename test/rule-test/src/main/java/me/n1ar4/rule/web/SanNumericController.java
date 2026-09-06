/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.rule.web;

import java.io.IOException;

/**
 * 净化链 3（不应命中）：数值转换后拼接回字符串传入 SINK。
 * 若缺少 parseInt 净化规则会被通用传播误报（历史高频误报场景）
 */
public class SanNumericController {
    public String sanNumeric(String cmd) {
        try {
            Runtime.getRuntime().exec(String.valueOf(Integer.parseInt(cmd)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
