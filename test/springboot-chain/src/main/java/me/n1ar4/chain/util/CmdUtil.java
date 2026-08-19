/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.chain.util;

import java.io.IOException;

/**
 * 危险方法（SINK）：直接执行命令
 * 黄金测试中的 DFS 目标：me/n1ar4/chain/util/CmdUtil.exec (Ljava/lang/String;)V
 */
public class CmdUtil {
    public static void exec(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
