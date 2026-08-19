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

public class GadgetServiceImpl implements GadgetService {
    @Override
    public String trigger(String cmd) {
        return gadgetWork(cmd);
    }

    private String gadgetWork(String cmd) {
        CmdUtil.exec(cmd);
        return "gadget";
    }
}
