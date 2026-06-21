/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server.handler;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.server.NanoHTTPD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// http://127.0.0.1:10032/api/dfs_analyze?
// sink_class=java/lang/Runtime&sink_method=exec&sink_method_desc=(Ljava/lang/String;)Ljava/lang/Process;&
// source_class=&source_method=&source_method_desc=&
// depth=10&limit=10&from_sink=true&search_null_source=true
public class DFSHandler extends BaseHandler implements HttpHandler {

    // 修复 P2: 参数边界，避免 depth=99,limit=999 致超时
    private static final int MAX_DEPTH = 20;
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_DEPTH = 10;
    private static final int DEFAULT_LIMIT = 10;

    private static int parseIntSafe(String s, int def) {
        if (StringUtil.isNull(s)) {
            return def;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        try {
            CoreEngine engine = MainForm.getEngine();
            if (engine == null || !engine.isEnabled()) {
                return error();
            }

            String sinkClass = getSinkClass(session);
            String sinkMethod = getSinkMethod(session);
            String sinkDesc = getSinkDesc(session);

            String sourceClass = getSourceClass(session);
            String sourceMethod = getSourceMethod(session);
            String sourceDesc = getSourceDesc(session);

            boolean fromSink = Boolean.parseBoolean(getFromSink(session));
            boolean searchNullSource = Boolean.parseBoolean(getSearchNullSource(session));

            // 修复 P2: 安全解析 + 边界校验
            int depth = parseIntSafe(getDepth(session), DEFAULT_DEPTH);
            int limit = parseIntSafe(getLimit(session), DEFAULT_LIMIT);

            if (depth < 1 || depth > MAX_DEPTH) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "depth must be in [1, " + MAX_DEPTH + "], got " + depth);
                return buildJSON(JSON.toJSONString(result));
            }
            if (limit < 1 || limit > MAX_LIMIT) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "limit must be in [1, " + MAX_LIMIT + "], got " + limit);
                return buildJSON(JSON.toJSONString(result));
            }

            DFSEngine dfsEngine = new DFSEngine(null, fromSink, searchNullSource, depth);
            dfsEngine.setMaxLimit(limit);
            dfsEngine.setSink(sinkClass, sinkMethod, sinkDesc);
            dfsEngine.setSource(sourceClass, sourceMethod, sourceDesc);

            dfsEngine.doAnalyze();

            List<DFSResult> dfsResults = dfsEngine.getResults();

            String json = JSON.toJSONString(dfsResults);
            return buildJSON(json);
        } catch (Exception ex) {
            // 返回结构化 JSON 错误，便于 LLM/客户端解析
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "error: " + ex.getMessage());
            return buildJSON(JSON.toJSONString(result));
        }
    }

    public String getSinkClass(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_class");
        if (p == null || p.isEmpty()) {
            return "";
        }
        String param = p.get(0);
        return param.replace('.', '/');
    }

    public String getSinkMethod(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_method");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSinkDesc(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("sink_method_desc");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSourceClass(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_class");
        if (p == null || p.isEmpty()) {
            return "";
        }
        String param = p.get(0);
        return param.replace('.', '/');
    }

    public String getSourceMethod(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_method");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSourceDesc(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("source_method_desc");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getDepth(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("depth");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getLimit(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("limit");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getFromSink(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("from_sink");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }

    public String getSearchNullSource(NanoHTTPD.IHTTPSession session) {
        List<String> p = session.getParameters().get("search_null_source");
        if (p == null || p.isEmpty()) {
            return "";
        }
        return p.get(0);
    }
}
