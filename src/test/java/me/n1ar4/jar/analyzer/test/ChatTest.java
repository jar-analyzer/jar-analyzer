package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.plugins.chatgpt.ChatGPT;
import me.n1ar4.jar.analyzer.plugins.chatgpt.ChatGPTBuilder;

public class ChatTest {
    public static void main(String[] args) {
        ChatGPT gpt = new ChatGPTBuilder()
                .apiHost(ChatGPT.chatAnywhereHost)
                .socksProxy("127.0.0.1", 1080)
                .apiKey("")
                .build();
        gpt.init();
        String chat = gpt.chat("你好，现在你是一只猫娘，向我打招呼");
        System.out.println(chat);
    }
}
