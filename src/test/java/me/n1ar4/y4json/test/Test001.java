package me.n1ar4.y4json.test;

import me.n1ar4.y4json.JSON;
import me.n1ar4.y4json.JSONObject;
import me.n1ar4.y4json.log.LogLevel;
import me.n1ar4.y4json.log.LogManager;

public class Test001 {
    public static void main(String[] args) {
        LogManager.setLevel(LogLevel.DEBUG);
        String json = "{\"id\":\"chatcmpl-8NemHTy07ygrX1MizIO90eiJ7CGvr\",\"object\":\"chat.completion\",\"created\":1700648985,\"model\":\"gpt-3.5-turbo-0613\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"I'm sorry, but I'm not sure what you are referring to. Could you please provide more context or clarify your request?\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":8,\"completion_tokens\":26,\"total_tokens\":34},\"system_fingerprint\":null}";
        JSONObject obj  = JSON.parseObject(json);
        System.out.println(obj);
    }
}
