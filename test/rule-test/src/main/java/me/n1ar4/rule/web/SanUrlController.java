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
import java.net.URLEncoder;

/**
 * 净化链 6（不应命中）：URLEncoder 编码后传入 SINK
 */
public class SanUrlController {
    public String sanUrl(String cmd) {
        try {
            Runtime.getRuntime().exec(URLEncoder.encode(cmd, "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
