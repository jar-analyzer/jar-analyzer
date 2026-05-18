/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.dfs.DFSResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaintResult {
    private DFSResult dfsResult;
    private String taintText;
    private boolean success;

    /**
     * 结构化事件流，新 UI 使用；为空时 UI 自动回退到 taintText。
     */
    private List<TaintEvent> events;

    /**
     * 简短链路标签，如 "I→G→R"，供结果表格显示。
     */
    private String badge;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DFSResult getDfsResult() {
        return dfsResult;
    }

    public void setDfsResult(DFSResult dfsResult) {
        this.dfsResult = dfsResult;
    }

    public String getTaintText() {
        return taintText;
    }

    public void setTaintText(String taintText) {
        this.taintText = taintText;
    }

    public List<TaintEvent> getEvents() {
        if (events == null) {
            return Collections.emptyList();
        }
        return events;
    }

    public void setEvents(List<TaintEvent> events) {
        this.events = events == null ? null : new ArrayList<>(events);
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    @Override
    public String toString() {
        return "TaintResult{" +
                "dfsResult=" + dfsResult +
                ", success=" + success +
                ", badge='" + badge + '\'' +
                ", eventsCount=" + (events == null ? 0 : events.size()) +
                '}';
    }
}
