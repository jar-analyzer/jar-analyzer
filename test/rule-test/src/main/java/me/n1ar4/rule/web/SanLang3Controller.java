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

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

/**
 * 净化链 4（不应命中）：commons-lang3 escapeHtml4 净化后传入 SINK
 */
public class SanLang3Controller {
    public String sanLang3(String cmd) {
        try {
            Runtime.getRuntime().exec(StringEscapeUtils.escapeHtml4(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
