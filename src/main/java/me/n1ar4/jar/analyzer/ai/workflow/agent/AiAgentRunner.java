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
            String text;
            try (Response resp = http.newCall(request).execute()) {
                ResponseBody body = resp.body();
                text = body == null ? "" : body.string();
                if (!resp.isSuccessful()) {
                    throw new IOException("HTTP " + resp.code() + ": " + truncate(text, 500));
                }
            }
            JSONObject json = JSON.parseObject(text);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            JSONObject first = choices.getJSONObject(0);
            JSONObject msg = first.getJSONObject("message");
            if (msg == null) {
                return "";
            }
            // 兼容字段：message.tool_calls
            JSONArray toolCalls = msg.getJSONArray("tool_calls");
            String content = msg.getString("content");
            if (toolCalls == null || toolCalls.isEmpty()) {
                // 模型给出最终结果
                logger.debug("agent finished after {} round(s)", round + 1);
                return content == null ? "" : content;
            }
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
