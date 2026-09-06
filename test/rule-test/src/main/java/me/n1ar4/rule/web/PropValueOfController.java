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
 * 传播链 2（应命中）：String.valueOf 转换后传入 SINK
 */
public class PropValueOfController {
    public String propValueOf(String cmd) {
        try {
            Runtime.getRuntime().exec(String.valueOf(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
