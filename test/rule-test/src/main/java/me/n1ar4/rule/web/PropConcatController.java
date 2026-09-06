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
 * 传播链 1（应命中）：String.concat 拼接后传入 SINK。
 * 注意不能用 String.format/String.join：varargs 参数被 javac
 * 包装为 Object[] 数组而引擎不建模数组元素存储会断链（已知限制）
 */
public class PropConcatController {
    public String propConcat(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd.concat(" -c 1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
