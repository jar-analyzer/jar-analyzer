package me.n1ar4.jar.analyzer.plugins.chatgpt;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.utils.http.Http;
import me.n1ar4.jar.analyzer.utils.http.HttpRequest;
import me.n1ar4.jar.analyzer.utils.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class ChatGPT {
    public static final String jsonType = "application/json";
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

        String key = "Bearer " + this.apiKey;

        HttpRequest request = new HttpRequest();
        request.setUrl(this.apiHost);
        request.setMethod("POST");
        request.setBody(json.getBytes());

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("User-Agent", Http.ua);
        headers.put("Connection", "keep-alive");
        headers.put("Authorization", key);

        request.setHeaders(headers);

        HttpResponse response = Http.doRequest(request);
        if (response.getBody().length == 0) {
            return "none";
        }
        String respBody = new String(response.getBody());
        GPTResponse resp = JSON.parseObject(respBody, GPTResponse.class);
        if (resp.getChoices().length < 1) {
            return "none";
        }
        return resp.getChoices()[0].getMessage().getContent();
    }

    public static ChatGPTBuilder builder() {
        return new ChatGPTBuilder();
    }
}
