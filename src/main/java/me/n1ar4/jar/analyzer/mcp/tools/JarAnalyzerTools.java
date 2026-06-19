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
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.mcp.McpContext;
import me.n1ar4.jar.analyzer.server.PathMatcher;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.server.NanoHTTPD;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * 注册全部 Jar Analyzer 相关的 MCP 工具
 * 与 Go 版 mcp 工具一一对应
 */
public class JarAnalyzerTools {

    private JarAnalyzerTools() {
    }

    public static void registerAll() {
        ToolRegistry r = ToolRegistry.getInstance();

        // ---------- method / class ----------
        r.register(new ToolDefinition(
                "get_methods_by_class",
                "查询指定类中的所有方法信息",
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "类名（点或斜杠分隔均可）", "true"}
                }),
                args -> callApi("/api/get_methods_by_class", new String[][]{
                        {"class", str(args, "class")}
                })
        ));

        r.register(new ToolDefinition(
                "get_methods_by_str",
                "搜索包含指定字符串(String类型的变量、注解)的方法（模糊）",
                ToolRegistry.buildSchema(new String[][]{
                        {"str", "string", "搜索关键字", "true"}
                }),
                args -> callApi("/api/get_methods_by_str", new String[][]{
                        {"str", str(args, "str")}
                })
        ));

        r.register(new ToolDefinition(
                "get_class_by_class",
                "查询类的基本信息",
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "类名（点或斜杠分隔均可）", "true"}
                }),
                args -> callApi("/api/get_class_by_class", new String[][]{
                        {"class", str(args, "class")}
                })
        ));

        // ---------- callgraph ----------
        registerCallGraph(r, "get_callers", "查询方法的所有调用者", "/api/get_callers");
        registerCallGraph(r, "get_callers_like", "模糊查询方法的调用者", "/api/get_callers_like");
        registerCallGraph(r, "get_callee", "查询方法的被调用者", "/api/get_callee");
        registerCallGraph(r, "get_method", "精确查询方法", "/api/get_method");
        registerCallGraph(r, "get_method_like", "模糊查询方法", "/api/get_method_like");
        registerCallGraph(r, "get_impls", "查询接口/抽象方法的实现", "/api/get_impls");
        registerCallGraph(r, "get_super_impls", "查询父类/接口的实现", "/api/get_super_impls");

        // ---------- spring ----------
        r.register(new ToolDefinition(
                "get_all_spring_controllers",
                "列出所有 Spring 控制器类",
                ToolRegistry.buildSchema(new String[][]{}),
                args -> callApi("/api/get_all_spring_controllers", new String[][]{})
        ));

        r.register(new ToolDefinition(
                "get_spring_mappings",
                "查询某 Spring 控制器的映射方法",
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "控制器类名", "true"}
                }),
                args -> callApi("/api/get_spring_mappings", new String[][]{
                        {"class", str(args, "class")}
                })
        ));

        // ---------- java web ----------
        r.register(new ToolDefinition(
                "get_all_filters",
                "列出所有 Java Web Filters 的实现类",
                ToolRegistry.buildSchema(new String[][]{}),
                args -> callApi("/api/get_all_filters", new String[][]{})
        ));
        r.register(new ToolDefinition(
                "get_all_servlets",
                "列出所有 Java Web Servlets 的实现类",
                ToolRegistry.buildSchema(new String[][]{}),
                args -> callApi("/api/get_all_servlets", new String[][]{})
        ));
        r.register(new ToolDefinition(
                "get_all_listeners",
                "列出所有 Java Web Listeners 的实现类",
                ToolRegistry.buildSchema(new String[][]{}),
                args -> callApi("/api/get_all_listeners", new String[][]{})
        ));

        // ---------- decompile code ----------
        r.register(new ToolDefinition(
                "get_code_fernflower",
                "反编译并提取指定方法代码（Fernflower）",
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "类名（点或斜杠分隔均可）", "true"},
                        {"method", "string", "方法名", "true"},
                        {"desc", "string", "方法描述（可选）", "false"}
                }),
                args -> callApi("/api/fernflower_code", new String[][]{
                        {"class", str(args, "class")},
                        {"method", str(args, "method")},
                        {"desc", str(args, "desc")}
                })
        ));
        r.register(new ToolDefinition(
                "get_code_cfr",
                "反编译并提取指定方法代码（CFR）",
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "类名（点或斜杠分隔均可）", "true"},
                        {"method", "string", "方法名", "true"},
                        {"desc", "string", "方法描述（可选）", "false"}
                }),
                args -> callApi("/api/cfr_code", new String[][]{
                        {"class", str(args, "class")},
                        {"method", str(args, "method")},
                        {"desc", str(args, "desc")}
                })
        ));

        // ---------- DFS analyze ----------
        JSONObject dfsSchema = ToolRegistry.buildSchema(new String[][]{
                {"sink_class", "string", "sink 类名（点或斜杠分隔均可）", "true"},
                {"sink_method", "string", "sink 方法名", "true"},
                {"sink_method_desc", "string", "sink 方法描述（可选）", "false"},
                {"source_class", "string", "source 类名（可选）", "false"},
                {"source_method", "string", "source 方法名（可选）", "false"},
                {"source_method_desc", "string", "source 方法描述（可选）", "false"},
                {"depth", "string", "搜索深度（默认 10）", "false"},
                {"limit", "string", "最大返回数量（默认 10）", "false"},
                {"from_sink", "string", "是否从 sink 开始搜索（默认 true）", "false"},
                {"search_null_source", "string", "是否搜索无 source 的路径（默认 true）", "false"}
        });
        r.register(new ToolDefinition(
                "dfs_analyze",
                "DFS 调用链分析（sink/source 可点或斜杠分隔）",
                dfsSchema,
                args -> {
                    String depth = strOrDefault(args, "depth", "10");
                    String limit = strOrDefault(args, "limit", "10");
                    String fromSink = strOrDefault(args, "from_sink", "true");
                    String searchNull = strOrDefault(args, "search_null_source", "true");
                    return callApi("/api/dfs_analyze", new String[][]{
                            {"sink_class", str(args, "sink_class")},
                            {"sink_method", str(args, "sink_method")},
                            {"sink_method_desc", str(args, "sink_method_desc")},
                            {"source_class", str(args, "source_class")},
                            {"source_method", str(args, "source_method")},
                            {"source_method_desc", str(args, "source_method_desc")},
                            {"depth", depth},
                            {"limit", limit},
                            {"from_sink", fromSink},
                            {"search_null_source", searchNull}
                    });
                }
        ));
    }

    private static void registerCallGraph(ToolRegistry r, String name, String desc, String api) {
        r.register(new ToolDefinition(
                name,
                desc,
                ToolRegistry.buildSchema(new String[][]{
                        {"class", "string", "类名", "true"},
                        {"method", "string", "方法名", "true"},
                        {"desc", "string", "方法描述（可选）", "false"}
                }),
                args -> callApi(api, new String[][]{
                        {"class", str(args, "class")},
                        {"method", str(args, "method")},
                        {"desc", str(args, "desc")}
                })
        ));
    }

    /**
     * 调用本地 jar-analyzer handler，零网络开销
     * 在执行前后维护 McpContext，使下游 API 可识别 MCP 调用、避免弹窗与 GUI 阻塞
     */
    private static String callApi(String uri, String[][] params) throws Exception {
        // 在调用任何 handler 前显式校验 engine 状态，给出对 LLM 友好的明确错误
        // 否则 jar 未加载时 handler 会返回 HTML（CORE ENGINE IS NULL），
        // LLM 难以解析，常被误判为"无数据/空结果"
        String notReady = checkEngineReady();
        if (notReady != null) {
            return notReady;
        }

        HttpHandler handler = lookupHandler(uri);
        if (handler == null) {
            return jsonError("handler not found: " + uri,
                    "MCP_HANDLER_NOT_FOUND", null);
        }
        FakeHttpSession session = FakeHttpSession.of(uri);
        if (params != null) {
            for (String[] p : params) {
                if (p == null || p.length < 2) continue;
                if (p[1] == null || p[1].isEmpty()) continue;
                session.addParam(p[0], p[1]);
            }
        }
        // 标记 MCP 上下文（McpServer 已在外层 enter/leave，这里幂等增强一次以兜底）
        boolean owned = !McpContext.isInMcp();
        if (owned) {
            McpContext.enter(0);
        }
        try {
            NanoHTTPD.Response resp = handler.handle(session);
            return readResponse(resp);
        } finally {
            if (owned) {
                McpContext.leave();
            }
        }
    }

    /**
     * 检查 jar-analyzer 引擎是否已就绪（jar 已被加载/分析）
     *
     * @return null 表示已就绪；非 null 表示一段对 LLM 友好的错误 JSON
     */
    private static String checkEngineReady() {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null) {
            return jsonError(
                    "no jar loaded: please open jar-analyzer GUI, drop a jar (or open a project), wait for analysis to finish, then retry",
                    "JAR_NOT_LOADED",
                    "engine is null - user has not loaded any jar yet");
        }
        try {
            if (!engine.isEnabled()) {
                return jsonError(
                        "jar analysis not ready: database or temp dir missing, please rebuild the jar in jar-analyzer GUI and retry",
                        "JAR_NOT_READY",
                        "engine.isEnabled() == false");
            }
        } catch (Throwable t) {
            return jsonError(
                    "jar analysis state check failed: " + t.getMessage(),
                    "ENGINE_CHECK_FAILED",
                    t.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * 构造一段对 LLM 友好的、稳定结构的错误 JSON
     * 形如:
     * {"success":false,"error":{"code":"JAR_NOT_LOADED","message":"...","detail":"..."}}
     */
    private static String jsonError(String message, String code, String detail) {
        JSONObject err = new JSONObject();
        err.put("code", code);
        err.put("message", message);
        if (detail != null) {
            err.put("detail", detail);
        }
        JSONObject obj = new JSONObject();
        obj.put("success", false);
        obj.put("error", err);
        return obj.toJSONString();
    }

    /**
     * 反射读取 PathMatcher.handlers
     * 这样可以避免在 PathMatcher 上加新方法即可获取 handlers
     */
    @SuppressWarnings("unchecked")
    private static HttpHandler lookupHandler(String uri) {
        try {
            Field f = PathMatcher.class.getDeclaredField("handlers");
            f.setAccessible(true);
            Object value = f.get(null);
            if (value instanceof java.util.Map) {
                java.util.Map<String, HttpHandler> map = (java.util.Map<String, HttpHandler>) value;
                return map.get(uri);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * 读取 NanoHTTPD.Response 内容
     * Response 内部 data/stream 字段为 private，使用反射读取
     */
    private static String readResponse(NanoHTTPD.Response resp) {
        if (resp == null) return "";
        try {
            Field dataField = NanoHTTPD.Response.class.getDeclaredField("data");
            dataField.setAccessible(true);
            byte[] data = (byte[]) dataField.get(resp);
            if (data != null) {
                return new String(data, StandardCharsets.UTF_8);
            }
            Field streamField = NanoHTTPD.Response.class.getDeclaredField("stream");
            streamField.setAccessible(true);
            InputStream is = (InputStream) streamField.get(resp);
            if (is != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) {
                    bos.write(buf, 0, r);
                }
                return new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private static String str(JSONObject args, String key) {
        if (args == null) return "";
        Object v = args.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private static String strOrDefault(JSONObject args, String key, String def) {
        String v = str(args, key);
        return (v == null || v.isEmpty()) ? def : v;
    }
}
