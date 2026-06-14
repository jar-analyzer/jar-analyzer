/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenAI 兼容协议客户端（DeepSeek / GLM / 自定义后端通用）
 */
public class LLMClient {
    private static final Logger logger = LogManager.getLogger();
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 流式回调
     */
    public interface StreamListener {
        /**
         * 每接收到一段文本（增量）
         */
        void onDelta(String delta);

        /**
         * 完成
         */
        void onDone();

        /**
         * 出错
         */
        void onError(Throwable t);
    }

    /**
     * 取消句柄（同时支持标记 + 真正取消底层 OkHttp Call）
     */
    public static class CancelHandle {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private volatile Call call;

        public void cancel() {
            cancelled.set(true);
            Call c = call;
            if (c != null) {
                try {
                    c.cancel();
                } catch (Throwable ignored) {
                }
            }
        }

        public boolean isCancelled() {
            return cancelled.get();
        }

        void bindCall(Call c) {
            this.call = c;
            // 如果在绑定前已经被取消，立即取消 call
            if (cancelled.get() && c != null) {
                try {
                    c.cancel();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private final AIConfig config;
    private final OkHttpClient http;

    public LLMClient(AIConfig config) {
        this.config = config;
        int t = Math.max(10, config.getTimeoutSeconds());
        this.http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(t, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                // 禁用自动重定向，避免恶意跳转
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    /**
     * 校验 endpoint：仅允许 http/https
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
            throw new IllegalArgumentException("baseUrl 必须包含 scheme (http/https)");
        }
        scheme = scheme.toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("仅支持 http/https，禁止 scheme=" + scheme);
        }
        // 拼 chat completions 路径
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith("/chat/completions")) {
            return s;
        }
        return s + "/chat/completions";
    }

    /**
     * 构造请求体
     */
    private byte[] buildBody(List<ChatMessage> messages, boolean stream) {
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        body.put("stream", stream);

        JSONArray msgs = new JSONArray();
        for (ChatMessage m : messages) {
            JSONObject obj = new JSONObject();
            obj.put("role", m.role);
            obj.put("content", m.content);
            msgs.add(obj);
        }
        body.put("messages", msgs);

        return body.toJSONString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 同步调用（非流式）
     */
    public String chat(List<ChatMessage> messages) throws IOException {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new IOException("API Key 未配置，请先在 AI 设置中填写");
        }
        String url = resolveEndpoint(config.getBaseUrl());
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(JSON_TYPE, buildBody(messages, false)))
                .build();
        try (Response resp = http.newCall(request).execute()) {
            ResponseBody body = resp.body();
            String text = body == null ? "" : body.string();
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + ": " + truncate(text, 500));
            }
            JSONObject jr = JSON.parseObject(text);
            JSONArray choices = jr.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            JSONObject first = choices.getJSONObject(0);
            JSONObject msg = first.getJSONObject("message");
            if (msg == null) {
                return "";
            }
            return msg.getString("content");
        }
    }

    /**
     * 流式调用（SSE）
     *
     * @return CancelHandle，可中途取消
     */
    public CancelHandle chatStream(List<ChatMessage> messages, StreamListener listener) {
        final CancelHandle handle = new CancelHandle();
        Thread t = new Thread(() -> {
            try {
                if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                    listener.onError(new IOException("API Key 未配置，请先在 AI 设置中填写"));
                    return;
                }
                String url = resolveEndpoint(config.getBaseUrl());
                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/event-stream")
                        .post(RequestBody.create(JSON_TYPE, buildBody(messages, true)))
                        .build();
                Call call = http.newCall(request);
                handle.bindCall(call);
                try (Response resp = call.execute()) {
                    if (!resp.isSuccessful()) {
                        ResponseBody body = resp.body();
                        String err = body == null ? "" : body.string();
                        listener.onError(new IOException("HTTP " + resp.code() + ": " + truncate(err, 500)));
                        return;
                    }
                    ResponseBody body = resp.body();
                    if (body == null) {
                        listener.onError(new IOException("空响应体"));
                        return;
                    }
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (handle.isCancelled()) {
                            break;
                        }
                        if (line.isEmpty()) {
                            continue;
                        }
                        if (!line.startsWith("data:")) {
                            continue;
                        }
                        String payload = line.substring(5).trim();
                        if (payload.isEmpty()) {
                            continue;
                        }
                        if ("[DONE]".equals(payload)) {
                            break;
                        }
                        try {
                            JSONObject obj = JSON.parseObject(payload);
                            JSONArray choices = obj.getJSONArray("choices");
                            if (choices == null || choices.isEmpty()) {
                                continue;
                            }
                            JSONObject first = choices.getJSONObject(0);
                            JSONObject delta = first.getJSONObject("delta");
                            if (delta == null) {
                                continue;
                            }
                            String content = delta.getString("content");
                            if (content != null && !content.isEmpty()) {
                                listener.onDelta(content);
                            }
                        } catch (Exception parseEx) {
                            // 单行解析失败不要中断整个流
                            logger.debug("sse parse skip: {}", parseEx.toString());
                        }
                    }
                    listener.onDone();
                }
            } catch (Throwable ex) {
                if (handle.isCancelled()) {
                    // 用户取消导致的 IO 异常：作为正常完成处理
                    listener.onDone();
                } else {
                    listener.onError(ex);
                }
            }
        }, "ai-stream");
        t.setDaemon(true);
        t.start();
        return handle;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }

    /**
     * 简单聊天消息
     */
    public static class ChatMessage {
        public final String role;
        public final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static ChatMessage system(String c) {
            return new ChatMessage("system", c);
        }

        public static ChatMessage user(String c) {
            return new ChatMessage("user", c);
        }

        public static ChatMessage assistant(String c) {
            return new ChatMessage("assistant", c);
        }
    }

    /**
     * 便捷构造：插入 system + user
     */
    public static List<ChatMessage> singleTurn(String systemPrompt, String userInput) {
        List<ChatMessage> list = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            list.add(ChatMessage.system(systemPrompt));
        }
        list.add(ChatMessage.user(userInput));
        return list;
    }
}
