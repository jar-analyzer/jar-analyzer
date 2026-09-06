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
 * 传播链 5（应命中）：String.trim 转换后传入 SINK（this 槽传播）
 */
public class PropTrimController {
    public String propTrim(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd.trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
