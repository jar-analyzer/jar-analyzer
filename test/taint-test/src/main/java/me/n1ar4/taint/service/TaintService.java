/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.taint.service;

/**
 * 链路 3 的接口层：验证污点经接口默认透传到实现类
 */
public interface TaintService {
    String run(String cmd);
}
