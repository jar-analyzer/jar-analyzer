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
 * 模型一次工具调用的请求载体。
 */
public final class ToolCall {

    private final String id;
    private final String name;
    private final JSONObject arguments;

    public ToolCall(String id, String name, JSONObject arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments == null ? new JSONObject() : arguments;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JSONObject getArguments() {
        return arguments;
    }
}
