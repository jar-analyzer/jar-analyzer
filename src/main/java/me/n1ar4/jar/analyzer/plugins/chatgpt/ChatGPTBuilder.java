package me.n1ar4.jar.analyzer.plugins.chatgpt;

public class ChatGPTBuilder {
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
        return new ChatGPT(apiKey, apiHost);
    }
}
