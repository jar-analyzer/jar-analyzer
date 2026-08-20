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

/**
 * 链路 5（不应命中）：入口参数与 SINK 无数据流关系，
 * SINK 收到的是常量，污点从未传播（误报对照样本）
 */
public class ConstController {
    public String constant(String cmd) {
        try {
            Runtime.getRuntime().exec("echo golden-const");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cmd;
    }
}
