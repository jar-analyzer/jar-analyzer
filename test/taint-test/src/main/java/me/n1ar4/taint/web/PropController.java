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
 * 链路 2（应命中）：入口参数经字符串拼接传播
 * "前缀" + cmd 编译为 StringBuilder.append/toString，
 * 命中 propagation.json 精细传播规则后到达 SINK
 */
public class PropController {
    public String prop(String cmd) {
        try {
            Runtime.getRuntime().exec("ping -c 1 " + cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
