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
 * 对照链（不应命中）：SINK 收到常量与入口参数无数据流关系
 */
public class ConstController {
    public String constant(String cmd) {
        try {
            Runtime.getRuntime().exec("ping 127.0.0.1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
