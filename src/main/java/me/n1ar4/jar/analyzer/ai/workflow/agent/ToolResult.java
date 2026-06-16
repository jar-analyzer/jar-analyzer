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

/**
 * 工具调用返回值。
 */
public final class ToolResult {

    private final boolean ok;
    private final String content;

    private ToolResult(boolean ok, String content) {
        this.ok = ok;
        this.content = content == null ? "" : content;
    }

    public static ToolResult ok(String text) {
        return new ToolResult(true, text);
    }

    public static ToolResult error(String text) {
        return new ToolResult(false, text);
    }

    public boolean isOk() {
        return ok;
    }

    public String getContent() {
        return content;
    }
}
