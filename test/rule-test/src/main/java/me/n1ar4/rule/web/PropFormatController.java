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
 * 传播链 6（应命中）：String.format 拼接后传入 SINK。
 * 依赖 varargs 数组元素污点传播（ANEWARRAY+AASTORE 近似）
 */
public class PropFormatController {
    public String propFormat(String cmd) {
        try {
            Runtime.getRuntime().exec(String.format("ping -c 1 %s", cmd));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
