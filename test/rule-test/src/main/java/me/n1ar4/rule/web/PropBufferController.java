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
 * 传播链 4（应命中）：StringBuffer append/toString 链式传播后传入 SINK
 */
public class PropBufferController {
    public String propBuffer(String cmd) {
        try {
            String v = new StringBuffer().append("ping ").append(cmd).toString();
            Runtime.getRuntime().exec(v);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
