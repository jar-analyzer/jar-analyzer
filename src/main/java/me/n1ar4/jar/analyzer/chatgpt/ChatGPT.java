package me.n1ar4.jar.analyzer.chatgpt;

import com.alibaba.fastjson2.JSON;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ChatGPT {
    public static final MediaType JSONType
            = MediaType.parse("application/json; charset=utf-8");
    private final String apiKey;
    private final String apiHost;
    private boolean initialized;

    public ChatGPT(String apiKey, String apiHost) {
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.initialized = false;
    }

    public ChatGPT init() {
        initialized = true;
        return this;
    }

    public String chat(String input) {
        if (!initialized) {
            throw new IllegalStateException("need init chat gpt");
        }
        String json = JSON.toJSONString(new GPTRequest(input));
        RequestBody body = RequestBody.create(json, JSONType);

        OkHttpClient client = new OkHttpClient();
        String key = "Bearer " + this.apiKey;
        Request request = new Request.Builder()
                .url(this.apiHost)
                .method("POST", body)
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", key)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.body() == null) {
                return "none";
            }
            String respBody = response.body().string();
            response.close();
            GPTResponse resp = JSON.parseObject(respBody, GPTResponse.class);
            if (resp.getChoices().length < 1) {
                return "none";
            }
            return resp.getChoices()[0].getMessage().getContent();
        } catch (Exception ignored) {
            return "none";
        }
    }

    public static ChatGPTBuilder builder() {
        return new ChatGPTBuilder();
    }
}
