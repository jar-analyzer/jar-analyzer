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

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

/**
 * 净化链 5（不应命中）：FilenameUtils.getName 剥离目录后传入 SINK
 */
public class SanPathController {
    public String sanPath(String cmd) {
        try {
            Runtime.getRuntime().exec(FilenameUtils.getName(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
