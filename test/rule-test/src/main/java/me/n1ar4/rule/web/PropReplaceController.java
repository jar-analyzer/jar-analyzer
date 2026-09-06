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
 * 传播链 3（应命中）：String.replace 替换字符后传入 SINK。
 * 常被误当作净化手段但无法真正消除命令注入语义上仍应报
 */
public class PropReplaceController {
    public String propReplace(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd.replace(";", " "));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }
}
