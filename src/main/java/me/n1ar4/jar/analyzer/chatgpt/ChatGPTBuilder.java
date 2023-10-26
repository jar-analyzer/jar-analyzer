package me.n1ar4.jar.analyzer.chatgpt;

public class ChatGPTBuilder {
    private String apiKey;
    private String apiHost;

    public ChatGPTBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ChatGPTBuilder apiHost(String apiHost) {
        this.apiHost = apiHost+"/v1/chat/completions";
        return this;
    }

    public ChatGPT build() {
        return new ChatGPT(apiKey, apiHost);
    }
}
