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

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;

/**
 * 净化链 2（不应命中）：jsoup 白名单清洗后传入 SINK
 */
public class SanJsoupController {
    public String sanJsoup(String cmd) {
        try {
            Runtime.getRuntime().exec(Jsoup.clean(cmd, Safelist.basic()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
