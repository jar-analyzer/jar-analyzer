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

import me.n1ar4.chain.service.CmdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 链路 A 的 SOURCE（Spring 入口，无其他调用者）
 */
@RestController
public class DirectController {
    @GetMapping("/direct/exec")
    public String exec(String cmd) {
        return new CmdService().handle(cmd);
    }
}
