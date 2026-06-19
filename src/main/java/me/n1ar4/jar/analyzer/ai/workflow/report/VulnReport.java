/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.report;

import java.util.ArrayList;
import java.util.List;

/**
 * 漏洞报告（与 n8n workflow 中 report-mcp 工具的 schema 对齐）。
 */
public final class VulnReport {

    /**
     * 漏洞类型，例如 deserialize / sql_injection / ssrf 等。
     */
    private String type;
    /**
     * 由大模型命名的漏洞独特标题（中文，简短）。
     * 例如："UserController#login 接口 SQL 注入"。
     */
    private String title;
    /**
     * AI 给出的判断依据（中文）。
     */
    private String reason;
    /**
     * 攻击方式（中文）：描述攻击者可以如何触发该漏洞，
     * 以及触发所需的前置条件 / 入口点 / 数据流。
     */
    private String attackVector;
    /**
     * 推断的 PoC（中文 + RAW HTTP）：
     * 一段可读的 PoC 描述，必须包含一段 RAW HTTP 请求示例
     * （含请求行、Host、Content-Type、关键 Header 与 Body）。
     */
    private String poc;
    /**
     * 风险评分 1-10。
     */
    private int score;
    /**
     * 漏洞调用链。
     */
    private List<VulnTrace> trace = new ArrayList<>();
    /**
     * 报告生成时间戳（毫秒）。
     */
    private long timestamp = System.currentTimeMillis();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAttackVector() {
        return attackVector;
    }

    public void setAttackVector(String attackVector) {
        this.attackVector = attackVector;
    }

    public String getPoc() {
        return poc;
    }

    public void setPoc(String poc) {
        this.poc = poc;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<VulnTrace> getTrace() {
        return trace;
    }

    public void setTrace(List<VulnTrace> trace) {
        this.trace = trace == null ? new ArrayList<VulnTrace>() : trace;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
