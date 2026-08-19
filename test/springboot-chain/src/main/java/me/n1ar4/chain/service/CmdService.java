/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.chain.service;

import me.n1ar4.chain.util.CmdUtil;

/**
 * 链路 A（无接口的精确链）：
 * DirectController.exec -&gt; CmdService.handle -&gt; CmdUtil.exec -&gt; Runtime.exec
 */
public class CmdService {
    public String handle(String cmd) {
        CmdUtil.exec(cmd);
        return "ok";
    }
}
