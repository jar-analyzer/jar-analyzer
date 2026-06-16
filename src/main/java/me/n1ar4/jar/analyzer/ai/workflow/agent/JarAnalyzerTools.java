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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 基于本地 jar-analyzer HTTP API 的工具集。
 * <p>
 * 这些工具与 mcp/pkg/tools 提供的 SSE-MCP 工具一一对应，但走纯 Java HTTP，不需要再起一个 Go MCP Server。
 * <p>
 */
public final class JarAnalyzerTools {

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    private final String baseUrl;

    public JarAnalyzerTools(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl required");
        }
        try {
            URI u = URI.create(baseUrl);
            String s = u.getScheme();
            if (s == null || (!s.equalsIgnoreCase("http") && !s.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("scheme must be http/https");
            }
            String host = u.getHost();
            if (host == null) {
                throw new IllegalArgumentException("missing host");
            }
            for (InetAddress ia : InetAddress.getAllByName(host)) {
                if (!(ia.isLoopbackAddress() || ia.isSiteLocalAddress())) {
                    throw new IllegalArgumentException(
                            "non-private host blocked: " + ia.getHostAddress());
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid base url: " + ex.getMessage());
        }
        // 标准化 baseUrl，去除尾部斜杠
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        this.baseUrl = s;
    }

    /**
     * 把所有 jar-analyzer HTTP API 工具注册进 registry。
     */
    public void registerAll(AgentToolRegistry registry) {
        // 列举类工具
        registry.register(simpleTool("get_all_servlets", "查询所有 Servlet",
                "/api/get_all_servlets", Collections.<String>emptyList()));
        registry.register(simpleTool("get_all_filters", "查询所有 Filter",
                "/api/get_all_filters", Collections.<String>emptyList()));
        registry.register(simpleTool("get_all_listeners", "查询所有 Listener",
                "/api/get_all_listeners", Collections.<String>emptyList()));
        registry.register(simpleTool("get_all_spring_controllers", "查询所有 Spring Controller",
                "/api/get_all_spring_controllers", Collections.<String>emptyList()));

        // 类信息工具
        registry.register(simpleTool("get_class_by_class", "查询 class 详细信息",
                "/api/get_class_by_class", Collections.singletonList("class")));
        registry.register(simpleTool("get_methods_by_class", "查询 class 的所有方法",
                "/api/get_methods_by_class", Collections.singletonList("class")));
        registry.register(simpleTool("get_jar_by_class", "根据类查 jar",
                "/api/get_jar_by_class", Collections.singletonList("class")));

        // 调用图
        registry.register(simpleTool("get_callers", "查询方法调用者",
                "/api/get_callers", Arrays.asList("class", "method", "desc")));
        registry.register(simpleTool("get_callee", "查询方法被调用者",
                "/api/get_callee", Arrays.asList("class", "method", "desc")));
        registry.register(simpleTool("get_method", "精确查询方法",
                "/api/get_method", Arrays.asList("class", "method", "desc")));
        registry.register(simpleTool("get_impls", "查询接口/抽象方法的实现",
                "/api/get_impls", Arrays.asList("class", "method", "desc")));
        registry.register(simpleTool("get_super_impls", "查询父类/接口的实现",
                "/api/get_super_impls", Arrays.asList("class", "method", "desc")));

        // 反编译
        registry.register(simpleTool("get_code_fernflower", "反编译指定方法 (Fernflower)",
                "/api/fernflower_code", Arrays.asList("class", "method", "desc")));
        registry.register(simpleTool("get_code_cfr", "反编译指定方法 (CFR)",
                "/api/cfr_code", Arrays.asList("class", "method", "desc")));

        // DFS 调用链分析
        registry.register(new AgentTool() {
            @Override
            public String name() {
                return "dfs_analyze";
            }

            @Override
            public String description() {
                return "DFS 调用链分析（sink/source 可点或斜杠分隔）";
            }

            @Override
            public JSONObject parametersSchema() {
                JSONObject s = new JSONObject();
                s.put("type", "object");
                JSONObject props = new JSONObject();
                props.put("sink_class", strProp("sink 类名"));
                props.put("sink_method", strProp("sink 方法名"));
                props.put("sink_method_desc", strProp("sink 描述"));
                props.put("source_class", strProp("source 类名（可选）"));
                props.put("source_method", strProp("source 方法名（可选）"));
                props.put("source_method_desc", strProp("source 描述（可选）"));
                props.put("depth", strProp("深度（默认 10）"));
                props.put("limit", strProp("最多结果数（默认 10）"));
                props.put("from_sink", strProp("是否从 sink 反向（默认 true）"));
                props.put("search_null_source", strProp("是否搜索无 source 路径（默认 true）"));
                s.put("properties", props);
                JSONArray req = new JSONArray();
                req.add("sink_class");
                req.add("sink_method");
                s.put("required", req);
                return s;
            }

            @Override
            public ToolResult invoke(JSONObject args) {
                JSONObject q = new JSONObject();
                String[] keys = {"sink_class", "sink_method", "sink_method_desc",
                        "source_class", "source_method", "source_method_desc",
                        "depth", "limit", "from_sink", "search_null_source"};
                for (String k : keys) {
                    String v = args.getString(k);
                    if (v != null && !v.isEmpty()) {
                        q.put(k, v);
                    }
                }
                return doGet("/api/dfs_analyze", q);
            }
        });
    }

    private AgentTool simpleTool(final String name, final String desc,
                                 final String path, final List<String> queryFields) {
        return new AgentTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return desc;
            }

            @Override
            public JSONObject parametersSchema() {
                JSONObject s = new JSONObject();
                s.put("type", "object");
                JSONObject props = new JSONObject();
                JSONArray req = new JSONArray();
                for (String f : queryFields) {
                    props.put(f, strProp(f + " 参数"));
                    if (!"desc".equals(f)) {
                        req.add(f);
                    }
                }
                s.put("properties", props);
                if (!req.isEmpty()) {
                    s.put("required", req);
                }
                return s;
            }

            @Override
            public ToolResult invoke(JSONObject args) {
                JSONObject q = new JSONObject();
                Set<String> allow = new HashSet<>(queryFields);
                if (args != null) {
                    for (Map.Entry<String, Object> en : args.entrySet()) {
                        if (allow.contains(en.getKey()) && en.getValue() != null) {
                            q.put(en.getKey(), String.valueOf(en.getValue()));
                        }
                    }
                }
                return doGet(path, q);
            }
        };
    }

    private static JSONObject strProp(String desc) {
        JSONObject p = new JSONObject();
        p.put("type", "string");
        p.put("description", desc);
        return p;
    }

    private ToolResult doGet(String path, JSONObject query) {
        StringBuilder sb = new StringBuilder(baseUrl).append(path);
        if (query != null && !query.isEmpty()) {
            sb.append('?');
            boolean first = true;
            for (Map.Entry<String, Object> e : query.entrySet()) {
                if (!first) {
                    sb.append('&');
                }
                first = false;
                sb.append(e.getKey()).append('=');
                try {
                    sb.append(URLEncoder.encode(String.valueOf(e.getValue()), "UTF-8"));
                } catch (Exception ex) {
                    return ToolResult.error("encode error: " + ex.getMessage());
                }
            }
        }
        Request req = new Request.Builder().url(sb.toString()).get().build();
        try (Response resp = HTTP.newCall(req).execute()) {
            ResponseBody body = resp.body();
            String text = body == null ? "" : body.string();
            if (!resp.isSuccessful()) {
                return ToolResult.error("http " + resp.code() + ": " + truncate(text, 300));
            }
            return ToolResult.ok(text);
        } catch (Exception ex) {
            return ToolResult.error("io error: " + ex.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
