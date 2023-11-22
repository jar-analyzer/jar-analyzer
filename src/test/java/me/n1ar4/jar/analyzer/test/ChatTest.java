package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.plugins.chatgpt.ChatGPT;
import me.n1ar4.jar.analyzer.plugins.chatgpt.ChatGPTBuilder;

public class ChatTest {
    public static void main(String[] args) {
        ChatGPT gpt = new ChatGPTBuilder()
                .apiHost(ChatGPT.chatAnywhereHost)
                .apiKey("")
                .build();
        gpt.init();
        String chat = gpt.chat("你好，你是谁");
        System.out.println(chat);
    }
}
