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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;

/**
 * 工具注册表。
 */
public final class AgentToolRegistry {

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();

    public AgentToolRegistry register(AgentTool tool) {
        if (tool != null && tool.name() != null && !tool.name().isEmpty()) {
            tools.put(tool.name(), tool);
        }
        return this;
    }

    public AgentTool get(String name) {
        return tools.get(name);
    }

    public List<AgentTool> list() {
        return new ArrayList<>(tools.values());
    }

    public Map<String, AgentTool> asMap() {
        return Collections.unmodifiableMap(tools);
    }

    /**
     * 转换为 OpenAI function calling 协议的 tools 数组。
     */
    public JSONArray toOpenAIToolsArray() {
        JSONArray arr = new JSONArray();
        for (AgentTool t : tools.values()) {
            JSONObject fn = new JSONObject();
            fn.put("name", t.name());
            fn.put("description", t.description());
            JSONObject params = t.parametersSchema();
            if (params == null) {
                params = new JSONObject();
                params.put("type", "object");
                params.put("properties", new JSONObject());
            }
            fn.put("parameters", params);
            JSONObject wrap = new JSONObject();
            wrap.put("type", "function");
            wrap.put("function", fn);
            arr.add(wrap);
        }
        return arr;
    }
}
