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

import org.owasp.encoder.Encode;

import java.io.IOException;

/**
 * 净化链 1（不应命中）：OWASP Java Encoder forHtml 净化后传入 SINK
 */
public class SanEncoderController {
    public String sanEncoder(String cmd) {
        try {
            Runtime.getRuntime().exec(Encode.forHtml(cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
