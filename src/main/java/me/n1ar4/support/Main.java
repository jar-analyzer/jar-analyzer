package me.n1ar4.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.core.Proxy;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.utils.ColorUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Y4Client client = new Y4Client();
        Proxy.setSystemProxy();
        HttpResponse resp = client.get("https://api.github.com/repos/jar-analyzer/jar-analyzer/contributors");
        String body = new String(resp.getBody());
        JSONArray array = JSONArray.parse(body);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            sb.append(" -> ");
            sb.append(ColorUtil.blue((String) obj.get("login")));
            sb.append(ColorUtil.yellow(" ("));
            sb.append(ColorUtil.yellow((String) obj.get("html_url")));
            sb.append(ColorUtil.yellow(") "));
            sb.append(ColorUtil.red("COMMITS: "));
            sb.append(ColorUtil.red(String.valueOf((int) obj.get("contributions"))));
            sb.append("\n");
        }

        try {
            Files.write(Paths.get("src/main/resources/thanks.txt"), sb.toString().getBytes());
        } catch (Exception ignored) {
        }
    }
}
