/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp.tools;

import com.alibaba.fastjson2.JSONObject;

/**
 * MCP 工具定义
 * 包含工具元数据 + 调用处理器
 */
public class ToolDefinition {
    private final String name;
    private final String description;
    private final JSONObject inputSchema;
    private final ToolHandler handler;

    public ToolDefinition(String name, String description,
                          JSONObject inputSchema, ToolHandler handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public JSONObject getInputSchema() {
        return inputSchema;
    }

    public ToolHandler getHandler() {
        return handler;
    }

    /**
     * 转 MCP tools/list 中的元素
     */
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        if (description != null) {
            obj.put("description", description);
        }
        obj.put("inputSchema", inputSchema);
        return obj;
    }

    /**
     * 工具调用回调接口
     */
    public interface ToolHandler {
        /**
         * @param arguments 入参（可能为 null）
         * @return 返回 MCP 文本结果（不抛出 RPC 错误，只表达工具内部错误）
         */
        String invoke(JSONObject arguments) throws Exception;
    }
}
