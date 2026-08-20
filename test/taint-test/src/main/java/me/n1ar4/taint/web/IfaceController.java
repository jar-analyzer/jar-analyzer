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

import me.n1ar4.taint.service.TaintService;
import me.n1ar4.taint.service.TaintServiceImpl;

/**
 * 链路 3（应命中）：入口参数经接口方法透传到实现类
 * IfaceController.iface -> TaintService.run(接口)
 * -> TaintServiceImpl.run -> Runtime.exec
 */
public class IfaceController {
    public String iface(String cmd) {
        TaintService service = new TaintServiceImpl();
        return service.run(cmd);
    }
}
