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
import me.n1ar4.jar.analyzer.ai.workflow.report.ReportSink;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnReport;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnTrace;

import java.util.*;

/**
 * 漏洞报告工具：模型决定漏洞存在时调用本工具，参数与 report-mcp 的 schema 对齐。
 */
public final class ReportTool implements AgentTool {

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "deserialize",
            "file_path_traversal",
            "redirect",
            "ssrf",
            "sql_injection",
            "template_injection",
            "arbitrary_file_download",
            "arbitrary_file_upload",
            "code_injection",
            "arbitrary_spring_bean_call",
            "xss",
            "command_injection",
            "other"
    ));

    private final ReportSink sink;
    private final List<VulnReport> collected = Collections.synchronizedList(new ArrayList<VulnReport>());

    public ReportTool(ReportSink sink) {
        if (sink == null) {
            throw new IllegalArgumentException("sink required");
        }
        this.sink = sink;
    }

    @Override
    public String name() {
        return "report";
    }

    @Override
    public String description() {
        return "上报已确认的漏洞结果，包括类型、原因、评分(1-10) 和调用链 trace。";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject props = new JSONObject();

        JSONObject typeProp = new JSONObject();
        typeProp.put("type", "string");
        JSONArray enums = new JSONArray();
        for (String t : ALLOWED_TYPES) {
            enums.add(t);
        }
        typeProp.put("enum", enums);
        typeProp.put("description", "vulnerable type");
        props.put("type", typeProp);

        JSONObject reasonProp = new JSONObject();
        reasonProp.put("type", "string");
        reasonProp.put("description", "vulnerable reason");
        props.put("reason", reasonProp);

        JSONObject scoreProp = new JSONObject();
        scoreProp.put("type", "integer");
        scoreProp.put("minimum", 1);
        scoreProp.put("maximum", 10);
        scoreProp.put("description", "vulnerable score (1-10)");
        props.put("score", scoreProp);

        JSONObject traceProp = new JSONObject();
        traceProp.put("type", "array");
        JSONObject item = new JSONObject();
        item.put("type", "object");
        JSONObject itemProps = new JSONObject();
        JSONObject classProp = new JSONObject();
        classProp.put("type", "string");
        JSONObject methodProp = new JSONObject();
        methodProp.put("type", "string");
        JSONObject descProp = new JSONObject();
        descProp.put("type", "string");
        itemProps.put("class", classProp);
        itemProps.put("method", methodProp);
        itemProps.put("desc", descProp);
        item.put("properties", itemProps);
        traceProp.put("items", item);
        traceProp.put("description", "vulnerability call chain");
        props.put("trace", traceProp);

        schema.put("properties", props);
        JSONArray req = new JSONArray();
        req.add("type");
        req.add("reason");
        req.add("score");
        req.add("trace");
        schema.put("required", req);
        return schema;
    }

    @Override
    public ToolResult invoke(JSONObject args) {
        if (args == null) {
            return ToolResult.error("missing args");
        }
        String type = args.getString("type");
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            return ToolResult.error("invalid type: " + type);
        }
        String reason = args.getString("reason");
        if (reason == null || reason.isEmpty()) {
            return ToolResult.error("missing reason");
        }
        Integer score = args.getInteger("score");
        if (score == null || score < 1 || score > 10) {
            return ToolResult.error("invalid score (must 1-10)");
        }
        JSONArray traceArr = args.getJSONArray("trace");
        if (traceArr == null || traceArr.isEmpty()) {
            return ToolResult.error("missing trace");
        }
        List<VulnTrace> traces = new ArrayList<>();
        for (int i = 0; i < traceArr.size(); i++) {
            JSONObject n = traceArr.getJSONObject(i);
            if (n == null) {
                continue;
            }
            VulnTrace t = new VulnTrace(
                    n.getString("class"),
                    n.getString("method"),
                    n.getString("desc"));
            traces.add(t);
        }
        VulnReport rep = new VulnReport();
        rep.setType(type);
        rep.setReason(reason);
        rep.setScore(score);
        rep.setTrace(traces);
        try {
            sink.save(rep);
            collected.add(rep);
            return ToolResult.ok("report saved: " + type + " score=" + score);
        } catch (Throwable t) {
            return ToolResult.error("save failed: " + t.getMessage());
        }
    }

    public List<VulnReport> getCollected() {
        return new ArrayList<>(collected);
    }
}
