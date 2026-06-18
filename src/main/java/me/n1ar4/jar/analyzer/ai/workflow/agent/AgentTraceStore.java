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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 线程安全的 AI Agent 交互记录收集器。
 * <p>
 * 按发生顺序保存每一轮 {@link AgentTurn}，供 GUI 在点击 AI Agent 节点时展示。
 */
public final class AgentTraceStore implements AgentTraceSink {

    private final List<AgentTurn> turns = Collections.synchronizedList(new ArrayList<AgentTurn>());

    @Override
    public void record(AgentTurn turn) {
        if (turn == null) {
            return;
        }
        turns.add(turn);
    }

    /**
     * 返回所有交互记录的快照副本（按发生顺序）。
     */
    public List<AgentTurn> getAll() {
        synchronized (turns) {
            return new ArrayList<>(turns);
        }
    }

    public int size() {
        return turns.size();
    }

    public void clear() {
        turns.clear();
    }
}
