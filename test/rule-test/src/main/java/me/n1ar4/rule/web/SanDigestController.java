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

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;

/**
 * 净化链 7（不应命中）：摘要后传入 SINK（内容已不可控）
 */
public class SanDigestController {
    public String sanDigest(String cmd) {
        try {
            Runtime.getRuntime().exec(DigestUtils.md5Hex(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
