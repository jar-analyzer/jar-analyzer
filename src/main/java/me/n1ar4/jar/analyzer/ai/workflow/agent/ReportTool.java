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
        return "上报已确认的漏洞结果，包括类型、独特标题、原因、攻击方式、推断 PoC（含 RAW HTTP）、评分(1-10) 和调用链 trace。所有文本字段必须使用中文。";
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
        typeProp.put("description", "漏洞类型枚举");
        props.put("type", typeProp);

        JSONObject titleProp = new JSONObject();
        titleProp.put("type", "string");
        titleProp.put("description",
                "漏洞独特标题（中文，简短，10-30 字），需包含目标类/接口/方法等关键信息，便于区分。"
                        + "示例：UserController#login 接口 SQL 注入。严禁使用千篇一律的标题。");
        props.put("title", titleProp);

        JSONObject reasonProp = new JSONObject();
        reasonProp.put("type", "string");
        reasonProp.put("description", "漏洞判断依据，必须使用中文，详细解释源到汇的数据流与触发条件。");
        props.put("reason", reasonProp);

        JSONObject attackProp = new JSONObject();
        attackProp.put("type", "string");
        attackProp.put("description",
                "攻击方式，必须使用中文。描述攻击者如何触发该漏洞、所需前置条件、入口点和数据流路径。");
        props.put("attack_vector", attackProp);

        JSONObject pocProp = new JSONObject();
        pocProp.put("type", "string");
        pocProp.put("description",
                "推断的 PoC，必须使用中文做说明，并且必须包含一段完整的 RAW HTTP 请求示例（含请求行、Host、关键 Header、Content-Type 和 Body）。"
                        + "若无法明确路径，请基于代码合理推断并标注【推断】。");
        props.put("poc", pocProp);

        JSONObject scoreProp = new JSONObject();
        scoreProp.put("type", "integer");
        scoreProp.put("minimum", 1);
        scoreProp.put("maximum", 10);
        scoreProp.put("description", "风险评分 1-10");
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
        traceProp.put("description", "漏洞调用链");
        props.put("trace", traceProp);

        schema.put("properties", props);
        JSONArray req = new JSONArray();
        req.add("type");
        req.add("title");
        req.add("reason");
        req.add("attack_vector");
        req.add("poc");
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
        String title = args.getString("title");
        if (title == null || title.trim().isEmpty()) {
            return ToolResult.error("missing title (中文独特标题必填)");
        }
        String reason = args.getString("reason");
        if (reason == null || reason.isEmpty()) {
            return ToolResult.error("missing reason");
        }
        String attackVector = args.getString("attack_vector");
        if (attackVector == null || attackVector.trim().isEmpty()) {
            return ToolResult.error("missing attack_vector (攻击方式必填)");
        }
        String poc = args.getString("poc");
        if (poc == null || poc.trim().isEmpty()) {
            return ToolResult.error("missing poc (推断 PoC 必填，需包含 RAW HTTP)");
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
        rep.setTitle(title.trim());
        rep.setReason(reason);
        rep.setAttackVector(attackVector);
        rep.setPoc(poc);
        rep.setScore(score);
        rep.setTrace(traces);
        try {
            sink.save(rep);
            collected.add(rep);
            return ToolResult.ok("report saved: " + type + " score=" + score + " title=" + title);
        } catch (Throwable t) {
            return ToolResult.error("save failed: " + t.getMessage());
        }
    }

    public List<VulnReport> getCollected() {
        return new ArrayList<>(collected);
    }
}
