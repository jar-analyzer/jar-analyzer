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

/**
 * 报告写入目标。
 */
public interface ReportSink {
    /**
     * 写入一条漏洞报告。失败抛出运行时异常。
     */
    void save(VulnReport report);
}
