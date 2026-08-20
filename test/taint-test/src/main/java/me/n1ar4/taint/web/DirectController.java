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

import me.n1ar4.taint.util.TaintHelper;

/**
 * 链路 1（应命中）：入口参数经中间方法直传 SINK
 * DirectController.direct -> TaintHelper.run -> Runtime.exec
 */
public class DirectController {
    public String direct(String cmd) {
        TaintHelper.run(cmd);
        return "ok";
    }
}
