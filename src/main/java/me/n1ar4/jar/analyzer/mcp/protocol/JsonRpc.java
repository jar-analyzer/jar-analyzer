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

import com.alibaba.fastjson2.JSONObject;

/**
 * JSON-RPC 2.0 工具方法
 * 协议规范: https://www.jsonrpc.org/specification
 * MCP 复用 JSON-RPC 2.0
 */
public class JsonRpc {

    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    /**
     * 是否为请求（包含 id 字段）
     * 仅 notification 没有 id
     */
    public static boolean isRequest(JSONObject msg) {
        return msg != null && msg.containsKey("id");
    }

    /**
     * 是否为通知（不包含 id 字段）
     */
    public static boolean isNotification(JSONObject msg) {
        return msg != null && !msg.containsKey("id");
    }

    /**
     * 构造一个成功响应
     */
    public static JSONObject result(Object id, Object result) {
        JSONObject resp = new JSONObject();
        resp.put("jsonrpc", "2.0");
        resp.put("id", id);
        resp.put("result", result);
        return resp;
    }

    /**
     * 构造一个错误响应
     */
    public static JSONObject error(Object id, int code, String message) {
        return error(id, code, message, null);
    }

    public static JSONObject error(Object id, int code, String message, Object data) {
        JSONObject resp = new JSONObject();
        resp.put("jsonrpc", "2.0");
        resp.put("id", id);
        JSONObject err = new JSONObject();
        err.put("code", code);
        err.put("message", message);
        if (data != null) {
            err.put("data", data);
        }
        resp.put("error", err);
        return resp;
    }
}
