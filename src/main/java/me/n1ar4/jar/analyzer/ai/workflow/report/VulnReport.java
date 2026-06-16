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
     * AI 给出的判断依据。
     */
    private String reason;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
