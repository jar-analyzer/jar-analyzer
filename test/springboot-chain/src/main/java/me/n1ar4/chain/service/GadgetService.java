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

/**
 * 链路 B（接口链）：验证 DFS 对 interface -&gt; impl 的桥接能力
 */
public interface GadgetService {
    String trigger(String cmd);
}
