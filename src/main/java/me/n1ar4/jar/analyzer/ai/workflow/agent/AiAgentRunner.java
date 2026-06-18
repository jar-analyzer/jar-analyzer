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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AI Agent 主循环。
 * <p>
 * 遵循 OpenAI Function Calling 协议（兼容 DeepSeek/GLM/Anthropic 兼容模式）：
 * 多轮 chat completion -> 解析 tool_calls -> 调用本地工具 -> 把结果作为 tool message 回填 -> 再次 chat。
 * 直到模型给出 finish_reason=stop 或达到 maxIterations。
 * <p>
 * 与 n8n langchain Agent 比较：
 * - 我们不要求 LLM 协议必须是 anthropic；OpenAI 兼容协议可直接用，且 jar-analyzer 已有 LLMClient 同款配置
 * - 对工具结果做长度截断，避免单步爆 token
 */
public final class AiAgentRunner {

    private static final Logger logger = LogManager.getLogger();
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 单工具结果最大字符数（避免单次工具返回打爆 LLM 输入窗口）。
     */
    private static final int MAX_TOOL_RESULT_CHARS = 12000;

    private final AIConfig cfg;
    private final AgentToolRegistry registry;
    private final int maxIterations;

    /**
     * 交互记录接收器（可选）。设置后，每一轮发送的 prompt 与收到的 response 都会被记录。
     */
    private AgentTraceSink traceSink;
    /**
     * Token 用量接收器（可选）。每次成功的 chat 调用都会上报一次，无论是否触发 tool_calls。
     */
    private TokenUsageSink tokenSink;
    /**
     * 交互记录的上下文标签（一般为当前分析的 className）。
     */
    private String contextLabel = "";

    public AiAgentRunner(AIConfig cfg, AgentToolRegistry registry, int maxIterations) {
        if (cfg == null) {
            throw new IllegalArgumentException("ai config required");
        }
        if (registry == null) {
            throw new IllegalArgumentException("registry required");
        }
        this.cfg = cfg;
        this.registry = registry;
        this.maxIterations = Math.max(1, maxIterations);
    }

    public void setTraceSink(AgentTraceSink traceSink) {
        this.traceSink = traceSink;
    }

    public void setTokenSink(TokenUsageSink tokenSink) {
        this.tokenSink = tokenSink;
    }

    public void setContextLabel(String contextLabel) {
        this.contextLabel = contextLabel == null ? "" : contextLabel;
    }

    /**
     * 执行一轮 Agent，返回最终自然语言回复。
     */
    public String run(String systemPrompt, String userPrompt) throws IOException {
        if (cfg.getApiKey() == null || cfg.getApiKey().isEmpty()) {
            throw new IOException("API Key 未配置");
        }
        // 构造对话历史
        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject sys = new JSONObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userPrompt == null ? "" : userPrompt);
        messages.add(user);

        OkHttpClient http = newHttpClient(cfg.getTimeoutSeconds());
        String endpoint = resolveEndpoint(cfg.getBaseUrl());
        JSONArray toolsArr = registry.toOpenAIToolsArray();

        for (int round = 0; round < maxIterations; round++) {
            JSONObject reqBody = new JSONObject();
            reqBody.put("model", cfg.getModel());
            reqBody.put("temperature", cfg.getTemperature());
            reqBody.put("max_tokens", cfg.getMaxTokens());
            reqBody.put("messages", messages);
            if (!toolsArr.isEmpty()) {
                reqBody.put("tools", toolsArr);
                reqBody.put("tool_choice", "auto");
            }
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("Authorization", "Bearer " + cfg.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .post(RequestBody.create(JSON_TYPE,
                            reqBody.toJSONString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            // 在发送前对本轮 prompt（完整对话消息）做一次快照，避免后续 add 影响记录
            String promptSnapshot = formatMessages(messages);
            String text;
            try (Response resp = http.newCall(request).execute()) {
                ResponseBody body = resp.body();
                text = body == null ? "" : body.string();
                if (!resp.isSuccessful()) {
                    recordTurn(round, promptSnapshot,
                            "[HTTP " + resp.code() + "]\n" + truncate(text, 2000));
                    throw new IOException("HTTP " + resp.code() + ": " + truncate(text, 500));
                }
            }
            JSONObject json = JSON.parseObject(text);
            // 上报 token 用量（若服务商返回 usage）。在 choices 解析之前调用，
            // 即使响应里没有 choices 也至少能记录到本次调用的消耗。
            reportUsage(json);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                recordTurn(round, promptSnapshot, "(no choices)\n" + truncate(text, 2000));
                return "";
            }
            JSONObject first = choices.getJSONObject(0);
            JSONObject msg = first.getJSONObject("message");
            if (msg == null) {
                recordTurn(round, promptSnapshot, "(no message)\n" + truncate(text, 2000));
                return "";
            }
            // 兼容字段：message.tool_calls
            JSONArray toolCalls = msg.getJSONArray("tool_calls");
            String content = msg.getString("content");
            if (toolCalls == null || toolCalls.isEmpty()) {
                // 模型给出最终结果
                logger.debug("agent finished after {} round(s)", round + 1);
                recordTurn(round, promptSnapshot, content == null ? "" : content);
                return content == null ? "" : content;
            }
            // 记录本轮 response（含工具调用）
            recordTurn(round, promptSnapshot, buildResponseText(content, toolCalls));
            // 把 assistant message 原样添加进对话
            JSONObject assistantMsg = new JSONObject();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", content == null ? "" : content);
            assistantMsg.put("tool_calls", toolCalls);
            messages.add(assistantMsg);

            // 逐个执行工具
            for (int i = 0; i < toolCalls.size(); i++) {
                JSONObject tc = toolCalls.getJSONObject(i);
                String tcId = tc.getString("id");
                if (tcId == null || tcId.isEmpty()) {
                    tcId = "call_" + UUID.randomUUID();
                }
                JSONObject fn = tc.getJSONObject("function");
                if (fn == null) {
                    appendToolResult(messages, tcId, "function missing");
                    continue;
                }
                String fname = fn.getString("name");
                JSONObject fargs;
                try {
                    String rawArgs = fn.getString("arguments");
                    fargs = rawArgs == null || rawArgs.isEmpty()
                            ? new JSONObject()
                            : JSON.parseObject(rawArgs);
                } catch (Throwable t) {
                    appendToolResult(messages, tcId, "invalid args json: " + t.getMessage());
                    continue;
                }
                AgentTool tool = registry.get(fname);
                if (tool == null) {
                    appendToolResult(messages, tcId, "tool not found: " + fname);
                    continue;
                }
                String toolText;
                try {
                    ToolResult tr = tool.invoke(fargs);
                    toolText = (tr.isOk() ? "" : "[ERROR] ") + tr.getContent();
                } catch (Throwable t) {
                    toolText = "[ERROR] " + t.toString();
                }
                appendToolResult(messages, tcId,
                        truncate(toolText, MAX_TOOL_RESULT_CHARS));
            }
        }
        // 达到最大轮数
        return "[agent reached max iterations " + maxIterations + "]";
    }

    private static void appendToolResult(JSONArray messages, String tcId, String content) {
        JSONObject m = new JSONObject();
        m.put("role", "tool");
        m.put("tool_call_id", tcId);
        m.put("content", content == null ? "" : content);
        messages.add(m);
    }

    /**
     * 记录一轮交互（若设置了 sink）。任何异常都不应影响主流程。
     */
    private void recordTurn(int round, String prompt, String response) {
        if (traceSink == null) {
            return;
        }
        try {
            traceSink.record(new AgentTurn(contextLabel, round, prompt, response));
        } catch (Throwable ignored) {
        }
    }

    /**
     * 解析响应里 OpenAI 兼容的 {@code usage} 对象并上报。
     * <p>
     * 兼容字段：{@code prompt_tokens / completion_tokens / total_tokens}。
     * 任何字段缺失则按 0 处理；若 usage 整体缺失，则不上报（避免污染计数）。
     */
    private void reportUsage(JSONObject json) {
        if (tokenSink == null || json == null) {
            return;
        }
        try {
            JSONObject u = json.getJSONObject("usage");
            if (u == null) {
                return;
            }
            long pt = readLong(u, "prompt_tokens");
            long ct = readLong(u, "completion_tokens");
            long tt = readLong(u, "total_tokens");
            if (tt == 0) {
                tt = pt + ct;
            }
            tokenSink.onUsage(new TokenUsage(pt, ct, tt));
        } catch (Throwable ignored) {
        }
    }

    private static long readLong(JSONObject o, String key) {
        if (o == null) {
            return 0L;
        }
        Object v = o.get(key);
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v).trim());
        } catch (Throwable t) {
            return 0L;
        }
    }

    /**
     * 把对话消息数组格式化为可读文本（用于展示本轮发送的 prompt 快照）。
     */
    private static String formatMessages(JSONArray messages) {
        StringBuilder sb = new StringBuilder();
        if (messages == null) {
            return "";
        }
        for (int i = 0; i < messages.size(); i++) {
            JSONObject m = messages.getJSONObject(i);
            if (m == null) {
                continue;
            }
            String role = m.getString("role");
            sb.append("================ ")
                    .append(role == null ? "?" : role.toUpperCase())
                    .append(" ================\n");
            String content = m.getString("content");
            if (content != null && !content.isEmpty()) {
                sb.append(content).append('\n');
            }
            String tcId = m.getString("tool_call_id");
            if (tcId != null && !tcId.isEmpty()) {
                sb.append("(tool_call_id=").append(tcId).append(")\n");
            }
            JSONArray tc = m.getJSONArray("tool_calls");
            if (tc != null && !tc.isEmpty()) {
                sb.append(formatToolCalls(tc));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * 组装本轮 response 文本：自然语言 content + 工具调用概要。
     */
    private static String buildResponseText(String content, JSONArray toolCalls) {
        StringBuilder sb = new StringBuilder();
        if (content != null && !content.isEmpty()) {
            sb.append(content).append('\n');
        }
        if (toolCalls != null && !toolCalls.isEmpty()) {
            sb.append("\n[工具调用 tool_calls]\n");
            sb.append(formatToolCalls(toolCalls));
        }
        return sb.toString();
    }

    private static String formatToolCalls(JSONArray toolCalls) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toolCalls.size(); i++) {
            JSONObject tc = toolCalls.getJSONObject(i);
            if (tc == null) {
                continue;
            }
            JSONObject fn = tc.getJSONObject("function");
            String name = fn == null ? "?" : fn.getString("name");
            String args = fn == null ? "" : fn.getString("arguments");
            sb.append("  - ").append(name).append("(")
                    .append(args == null ? "" : args).append(")\n");
        }
        return sb.toString();
    }

    private static OkHttpClient newHttpClient(int timeoutSec) {
        int t = Math.max(10, timeoutSec);
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(t, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    /**
     * 校验 endpoint 安全性（仅允许 http/https），并补全 chat completions 路径。
     */
    private static String resolveEndpoint(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl 为空");
        }
        URI uri;
        try {
            uri = URI.create(baseUrl.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("baseUrl 非法: " + e.getMessage());
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("baseUrl 必须包含 scheme");
        }
        scheme = scheme.toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("仅支持 http/https，scheme=" + scheme);
        }
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith("/chat/completions")) {
            return s;
        }
        return s + "/chat/completions";
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "\n... [truncated " + (s.length() - max) + " chars]";
    }

    @SuppressWarnings("unused")
    private static List<String> emptyList() {
        return new ArrayList<>();
    }
}
