/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.agent;

/**
 * Token 用量上报 sink。
 * <p>
 * 实现需要保证线程安全（Agent 在后台线程执行）。
 */
public interface TokenUsageSink {

    /**
     * 报告一次成功的 chat/completions 调用消耗。
     *
     * @param usage 本轮 usage（不为 null；若服务商未返回 usage，调用方传 0/0/0）
     */
    void onUsage(TokenUsage usage);
}
