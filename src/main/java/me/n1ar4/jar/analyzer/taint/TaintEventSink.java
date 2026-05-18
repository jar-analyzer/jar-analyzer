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

import java.util.*;

/**
 * 污点分析事件收集器。
 * <p>
 * 组合了：
 * <ul>
 *   <li>结构化事件列表（供新 UI 使用）</li>
 *   <li>纯文本回退（供 {@link TaintResult#getTaintText()} 兼容旧 UI / 旧导出）</li>
 *   <li>事件类型计数（用于结果列表概览）</li>
 * </ul>
 */
public final class TaintEventSink {

    private final List<TaintEvent> events = new ArrayList<>();
    private final StringBuilder text = new StringBuilder();
    private final Map<TaintEvent.Type, Integer> counters = new EnumMap<>(TaintEvent.Type.class);

    public void emit(TaintEvent ev) {
        events.add(ev);
        text.append(ev.toPlainLine()).append('\n');
        counters.merge(ev.getType(), 1, Integer::sum);
    }

    public List<TaintEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public String getText() {
        return text.toString();
    }

    public StringBuilder getTextBuilder() {
        return text;
    }

    public int countOf(TaintEvent.Type type) {
        Integer v = counters.get(type);
        return v == null ? 0 : v;
    }

    /**
     * 简短的"链路标签"，例如：I→S→G→R。
     * <ul>
     *   <li>I = INTERFACE_PASSTHROUGH</li>
     *   <li>S = SANITIZER_HIT</li>
     *   <li>P = PROPAGATION_RULE_HIT</li>
     *   <li>G = GENERIC_PROPAGATE</li>
     *   <li>D = INVOKEDYNAMIC_PROPAGATE</li>
     *   <li>R = REACH_NEXT</li>
     * </ul>
     */
    public String shortBadge() {
        StringBuilder sb = new StringBuilder();
        for (TaintEvent ev : events) {
            char c;
            switch (ev.getType()) {
                case INTERFACE_PASSTHROUGH:
                    c = 'I';
                    break;
                case SANITIZER_HIT:
                    c = 'S';
                    break;
                case PROPAGATION_RULE_HIT:
                    c = 'P';
                    break;
                case GENERIC_PROPAGATE:
                    c = 'G';
                    break;
                case INVOKEDYNAMIC_PROPAGATE:
                    c = 'D';
                    break;
                case REACH_NEXT:
                    c = 'R';
                    break;
                default:
                    continue;
            }
            if (sb.length() > 0) sb.append('→');
            sb.append(c);
        }
        return sb.toString();
    }
}
