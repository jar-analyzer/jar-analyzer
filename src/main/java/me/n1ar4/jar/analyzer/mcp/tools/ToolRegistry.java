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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具注册中心
 * 进程内共享一个全局注册表
 */
public class ToolRegistry {
    private static final ToolRegistry INSTANCE = new ToolRegistry();
    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    private ToolRegistry() {
    }

    public static ToolRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(ToolDefinition tool) {
        if (tool == null || tool.getName() == null) return;
        tools.put(tool.getName(), tool);
    }

    public synchronized ToolDefinition get(String name) {
        return tools.get(name);
    }

    public synchronized List<ToolDefinition> list() {
        return new ArrayList<>(tools.values());
    }

    public synchronized JSONArray listAsJson() {
        JSONArray arr = new JSONArray();
        for (ToolDefinition def : tools.values()) {
            arr.add(def.toJson());
        }
        return arr;
    }

    public synchronized int size() {
        return tools.size();
    }

    /**
     * 工具一定要注册之后才会被 MCP Client 看到
     * 此处只暴露给 list/call，不做任何额外处理
     */
    public synchronized void clear() {
        tools.clear();
    }

    /**
     * 简易 Schema 构造
     * 形如：buildSchema(new String[][]{ {"class", "string", "类名", "true"} })
     */
    public static JSONObject buildSchema(String[][] params) {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject properties = new JSONObject();
        JSONArray required = new JSONArray();
        if (params != null) {
            for (String[] p : params) {
                if (p == null || p.length < 3) continue;
                String name = p[0];
                String type = p[1];
                String desc = p[2];
                boolean req = p.length > 3 && "true".equalsIgnoreCase(p[3]);
                JSONObject prop = new JSONObject();
                prop.put("type", type);
                if (desc != null && !desc.isEmpty()) {
                    prop.put("description", desc);
                }
                properties.put(name, prop);
                if (req) {
                    required.add(name);
                }
            }
        }
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }
}
