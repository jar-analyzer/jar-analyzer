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

import com.alibaba.fastjson2.JSONObject;

/**
 * AI Agent 可调用的工具抽象。
 * <p>
 * 与 OpenAI Function Calling 协议对齐：每个工具有 name / description / parameters JSON Schema，
 * 模型可在多轮对话中决定调用哪个工具，运行时把工具返回结果回填到对话里继续推理。
 */
public interface AgentTool {

    /**
     * 工具名（建议 snake_case，匹配 n8n 中 mcp 注册的同名工具）。
     */
    String name();

    /**
     * 描述（中文/英文均可，模型据此选择工具）。
     */
    String description();

    /**
     * JSON Schema 风格的参数声明，例如：
     * <pre>{ "type":"object", "properties":{"class":{"type":"string"}}, "required":["class"] }</pre>
     */
    JSONObject parametersSchema();

    /**
     * 真正执行。args 是模型给出的 JSON 参数，需要在 invoke 内做严格校验。
     */
    ToolResult invoke(JSONObject args) throws Exception;
}
