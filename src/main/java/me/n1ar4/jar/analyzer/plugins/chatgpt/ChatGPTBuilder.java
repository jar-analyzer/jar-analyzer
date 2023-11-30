package me.n1ar4.jar.analyzer.plugins.chatgpt;

import me.n1ar4.http.Y4Client;

public class ChatGPTBuilder {
    private static final int GPT_TIMEOUT = 30000;
    private String apiKey;
    private String apiHost;
    private String proxyHost;
    private int proxyPort;

    public ChatGPTBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ChatGPTBuilder apiHost(String apiHost) {
        this.apiHost = apiHost + "/v1/chat/completions";
        return this;
    }

    public ChatGPTBuilder socksProxy(String host, int port) {
        this.proxyHost = host;
        this.proxyPort = port;
        return this;
    }

    public ChatGPT build() {
        if (proxyHost == null || proxyHost.isEmpty()) {
            return new ChatGPT(apiKey, apiHost,
                    new Y4Client(GPT_TIMEOUT));
        } else {
            return new ChatGPT(apiKey, apiHost,
                    new Y4Client(GPT_TIMEOUT, proxyHost, proxyPort));
        }
    }
}
