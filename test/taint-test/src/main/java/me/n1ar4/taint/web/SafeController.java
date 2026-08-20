/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.taint.web;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 链路 4（不应命中）：入口参数经 Pattern.quote 净化后
 * 才传入 SINK，命中 sanitizer.json 清洗规则，污点中断
 */
public class SafeController {
    public String safe(String cmd) {
        try {
            Runtime.getRuntime().exec(Pattern.quote(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
