package me.n1ar4.jar.analyzer.plugins.chatgpt;

import me.n1ar4.http.*;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.y4json.JSON;
import me.n1ar4.y4json.JSONArray;
import me.n1ar4.y4json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ChatGPT {
    private static final Logger logger = LogManager.getLogger();
    public static final String openaiHost = "https://api.openai.com";
    public static final String chatAnywhereHost = "https://api.chatanywhere.com.cn";
    private final String apiKey;
    private final String apiHost;
    private boolean initialized;
    private final Y4Client client;

    public ChatGPT(String apiKey, String apiHost, Y4Client client) {
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.client = client;
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
        logger.info("start chat");
        HttpRequest request = getHttpRequest(json);
        HttpResponse response = client.request(request);
        if (response.getBody().length == 0) {
            return "none";
        }
        if (!response.getHeaders().get(HttpHeaders.ContentType).contains("json")) {
            return "none";
        }
        String respBody = new String(response.getBody());
        JSONObject resp = JSON.parseObject(respBody);
        Object choices = resp.get("choices");
        if (choices == null) {
            return "none";
        }
        if (!(choices instanceof JSONArray)) {
            return "none";
        }
        JSONArray array = (JSONArray) choices;
        JSONObject choice = (JSONObject) array.get(0);
        JSONObject message = (JSONObject) choice.get("message");
        return (String) message.get("content");
    }

    private HttpRequest getHttpRequest(String json) {
        String key = "Bearer " + this.apiKey;

        HttpRequest request = new HttpRequest();
        try {
            request.setUrl(new URL(this.apiHost));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        request.setMethod("POST");
        request.setBody(json.getBytes());

        Map<String, String> headers = new HashMap<>();

        headers.put(HttpHeaders.ContentType, ContentType.JSON);
        headers.put(HttpHeaders.UserAgent, HttpRequest.DefaultUA);
        headers.put(HttpHeaders.Connection, "close");
        headers.put(HttpHeaders.Authorization, key);

        request.setHeaders(headers);
        return request;
    }

    public static ChatGPTBuilder builder() {
        return new ChatGPTBuilder();
    }
}
