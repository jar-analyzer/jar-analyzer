/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.gui;

import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;

import java.util.*;

/**
 * 收集所有 LoopEvent，按 (loopNodeId, index) 分组，便于 GUI 展示某次迭代的全部步骤。
 * <p>
 * 线程安全：所有可变状态都用 synchronized 保护。
 */
public final class LoopHistory implements DagContext.LoopListener {

    private final List<DagContext.LoopEvent> events = new ArrayList<>();
    /**
     * (loopNodeId + "#" + index) -> 该次迭代的所有事件
     */
    private final Map<String, List<DagContext.LoopEvent>> byIteration = new HashMap<>();

    @Override
    public synchronized void onLoopEvent(DagContext.LoopEvent event) {
        if (event == null) {
            return;
        }
        events.add(event);
        String key = event.getLoopNodeId() + "#" + event.getIndex();
        List<DagContext.LoopEvent> list = byIteration.get(key);
        if (list == null) {
            list = new ArrayList<>();
            byIteration.put(key, list);
        }
        list.add(event);
    }

    public synchronized List<DagContext.LoopEvent> snapshotAll() {
        return new ArrayList<>(events);
    }

    /**
     * 取所有该 loop 节点已经处理过 / 处理中的迭代序号。
     */
    public synchronized List<Integer> iterationsOf(String loopNodeId) {
        List<Integer> ids = new ArrayList<>();
        if (loopNodeId == null) {
            return ids;
        }
        String prefix = loopNodeId + "#";
        for (String k : byIteration.keySet()) {
            if (k.startsWith(prefix)) {
                try {
                    ids.add(Integer.parseInt(k.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Collections.sort(ids);
        return ids;
    }

    public synchronized List<DagContext.LoopEvent> get(String loopNodeId, int index) {
        List<DagContext.LoopEvent> list = byIteration.get(loopNodeId + "#" + index);
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(list);
    }

    public synchronized int totalIterations(String loopNodeId) {
        return iterationsOf(loopNodeId).size();
    }

    public synchronized void clear() {
        events.clear();
        byIteration.clear();
    }
}
