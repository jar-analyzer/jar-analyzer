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
 * AI Agent 单轮交互记录。
 * <p>
 * 记录 Agent 主循环中某一轮（round）向 LLM 发送的 prompt（完整对话消息快照）
 * 以及收到的 response（自然语言回复 + 工具调用概要）。
 * <p>
 */
public final class AgentTurn {

    /**
     * 关联的上下文标签（一般为当前分析的 className）。
     */
    private final String label;
    /**
     * 该次 Agent 调用内的轮序号（0-based）。
     */
    private final int round;
    /**
     * 本轮发送给 LLM 的完整对话内容（已格式化为可读文本）。
     */
    private final String prompt;
    /**
     * 本轮 LLM 返回的内容（content + tool_calls 概要）。
     */
    private final String response;
    /**
     * 记录创建时间戳（毫秒）。
     */
    private final long timestamp = System.currentTimeMillis();

    public AgentTurn(String label, int round, String prompt, String response) {
        this.label = label == null ? "" : label;
        this.round = round;
        this.prompt = prompt == null ? "" : prompt;
        this.response = response == null ? "" : response;
    }

    public String getLabel() {
        return label;
    }

    public int getRound() {
        return round;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getResponse() {
        return response;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String l = label.isEmpty() ? "agent" : label;
        return l + "  · round " + (round + 1);
    }
}
