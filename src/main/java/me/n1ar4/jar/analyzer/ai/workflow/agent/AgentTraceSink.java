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
 * AI Agent 单轮交互记录的接收器。
 */
public interface AgentTraceSink {

    /**
     * 记录一轮交互。实现需保证线程安全（Agent 在后台线程执行）。
     */
    void record(AgentTurn turn);
}
