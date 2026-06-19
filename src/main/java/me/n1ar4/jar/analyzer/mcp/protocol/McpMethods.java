/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp.protocol;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.mcp.tools.ToolDefinition;
import me.n1ar4.jar.analyzer.mcp.tools.ToolRegistry;

/**
 * 处理 MCP（基于 JSON-RPC 2.0）的核心方法
 * 支持: initialize, ping, notifications/initialized,
 *       tools/list, tools/call
 */
public class McpMethods {

    /**
     * 服务端协议版本（与多数 MCP Client 兼容的稳定版本）
     */
    public static final String PROTOCOL_VERSION = "2024-11-05";

    /**
     * 服务端名字 + 版本
     */
    public static final String SERVER_NAME = "jar-analyzer-mcp";
    public static final String SERVER_VERSION = "1.0.0-java";

    /**
     * 处理一条入站 JSON-RPC 消息
     *
     * @return 若是请求，返回响应 JSON；若是通知（无 id），返回 null（不响应）
     */
    public static JSONObject dispatch(JSONObject msg) {
        if (msg == null) {
            return JsonRpc.error(null, JsonRpc.PARSE_ERROR, "empty message");
        }
        Object id = msg.get("id");
        String method = msg.getString("method");

        if (method == null || method.isEmpty()) {
            // 没有 method 字段：当作错误响应（来自客户端的响应消息），忽略
            return null;
        }

        try {
            switch (method) {
                case "initialize":
                    return handleInitialize(id, msg.getJSONObject("params"));
                case "ping":
                    return JsonRpc.result(id, new JSONObject());
                case "notifications/initialized":
                case "initialized":
                    // notification 无 id，忽略
                    return null;
                case "tools/list":
                    return handleToolsList(id);
                case "tools/call":
                    return handleToolsCall(id, msg.getJSONObject("params"));
                default:
                    if (JsonRpc.isNotification(msg)) {
                        return null;
                    }
                    return JsonRpc.error(id, JsonRpc.METHOD_NOT_FOUND,
                            "method not found: " + method);
            }
        } catch (Exception ex) {
            if (JsonRpc.isNotification(msg)) {
                return null;
            }
            return JsonRpc.error(id, JsonRpc.INTERNAL_ERROR,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private static JSONObject handleInitialize(Object id, JSONObject params) {
        JSONObject result = new JSONObject();
        result.put("protocolVersion", PROTOCOL_VERSION);

        // 服务端能力声明
        JSONObject capabilities = new JSONObject();
        JSONObject toolsCap = new JSONObject();
        toolsCap.put("listChanged", false);
        capabilities.put("tools", toolsCap);
        result.put("capabilities", capabilities);

        JSONObject info = new JSONObject();
        info.put("name", SERVER_NAME);
        info.put("version", SERVER_VERSION);
        result.put("serverInfo", info);

        return JsonRpc.result(id, result);
    }

    private static JSONObject handleToolsList(Object id) {
        JSONArray arr = ToolRegistry.getInstance().listAsJson();
        JSONObject result = new JSONObject();
        result.put("tools", arr);
        return JsonRpc.result(id, result);
    }

    private static JSONObject handleToolsCall(Object id, JSONObject params) {
        if (params == null) {
            return JsonRpc.error(id, JsonRpc.INVALID_PARAMS, "missing params");
        }
        String name = params.getString("name");
        if (name == null || name.isEmpty()) {
            return JsonRpc.error(id, JsonRpc.INVALID_PARAMS, "missing tool name");
        }
        JSONObject args = params.getJSONObject("arguments");
        ToolDefinition def = ToolRegistry.getInstance().get(name);
        if (def == null) {
            return JsonRpc.error(id, JsonRpc.METHOD_NOT_FOUND,
                    "tool not found: " + name);
        }
        try {
            String text = def.getHandler().invoke(args);
            return buildToolResult(id, text, false);
        } catch (Exception ex) {
            // 工具内部错误以 isError + content 形式返回（不当作 JSON-RPC error）
            return buildToolResult(id, "error: " + ex.getMessage(), true);
        }
    }

    /**
     * 构造 tools/call 标准返回
     */
    public static JSONObject buildToolResult(Object id, String text, boolean isError) {
        JSONObject result = new JSONObject();
        JSONArray content = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "text");
        item.put("text", text == null ? "" : text);
        content.add(item);
        result.put("content", content);
        result.put("isError", isError);
        return JsonRpc.result(id, result);
    }
}
