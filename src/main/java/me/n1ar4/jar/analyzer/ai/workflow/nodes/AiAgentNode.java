/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.nodes;

import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.jar.analyzer.ai.workflow.agent.AgentToolRegistry;
import me.n1ar4.jar.analyzer.ai.workflow.agent.AiAgentRunner;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagNode;
import me.n1ar4.jar.analyzer.ai.workflow.core.NodeResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Agent 节点。对应 n8n 的 @n8n/n8n-nodes-langchain.agent。
 * <p>
 * 输入：上游传入一个 {@code Map}（一般包含 {@code chatInput} 字段）。
 * 输出：{@code {"output": "<assistant final text>"}}
 */
public final class AiAgentNode extends DagNode {

    private static final Logger logger = LogManager.getLogger();

    private final AIConfig cfg;
    private final AgentToolRegistry registry;
    private final String systemPrompt;
    private final int maxIterations;

    public AiAgentNode(String id, AIConfig cfg, AgentToolRegistry registry,
                       String systemPrompt, int maxIterations) {
        super(id, "AI Agent");
        if (cfg == null) {
            throw new IllegalArgumentException("ai config required");
        }
        if (registry == null) {
            throw new IllegalArgumentException("registry required");
        }
        this.cfg = cfg;
        this.registry = registry;
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.maxIterations = Math.max(1, maxIterations);
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) {
        Object data = (inputs == null || inputs.isEmpty()) ? null : inputs.get(0).getData();
        String userPrompt = "";
        if (data instanceof Map) {
            Object v = ((Map<String, Object>) data).get("chatInput");
            if (v != null) {
                userPrompt = String.valueOf(v);
            }
        } else if (data != null) {
            userPrompt = String.valueOf(data);
        }
        if (userPrompt.isEmpty()) {
            logger.warn("ai agent received empty user prompt");
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("output", "");
            return NodeResult.ok(empty);
        }
        AiAgentRunner runner = new AiAgentRunner(cfg, registry, maxIterations);
        String out;
        try {
            out = runner.run(systemPrompt, userPrompt);
        } catch (Throwable t) {
            logger.warn("ai agent failed: {}", t.toString());
            Map<String, Object> map = new HashMap<>();
            map.put("output", "");
            map.put("error", String.valueOf(t.getMessage()));
            return NodeResult.ok(map);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("output", out);
        return NodeResult.ok(map);
    }
}
