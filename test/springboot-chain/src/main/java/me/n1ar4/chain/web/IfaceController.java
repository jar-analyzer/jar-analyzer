/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.chain.web;

import me.n1ar4.chain.service.GadgetService;
import me.n1ar4.chain.service.GadgetServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 链路 B 的 SOURCE：经过 GadgetService 接口调用实现类
 */
@RestController
public class IfaceController {
    @GetMapping("/iface/exec")
    public String exec(String cmd) {
        GadgetService service = new GadgetServiceImpl();
        return service.trigger(cmd);
    }
}
