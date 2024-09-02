package me.n1ar4.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.core.Proxy;
import me.n1ar4.jar.analyzer.http.HttpRequest;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ContributorMD {
    public static void main(String[] args) throws Exception {
        Proxy.setSystemProxy();
        Y4Client client = new Y4Client();
        HttpRequest req = new HttpRequest();
        req.setMethod("GET");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        headers.put("Authorization", "Bearer " +
                new String(Files.readAllBytes(Paths.get("token.txt"))).trim());
        req.setHeaders(headers);
        req.setUrl(new URL("https://api.github.com/repos/jar-analyzer/jar-analyzer/contributors"));
        HttpResponse resp = client.request(req);
        JSONArray array = JSONArray.parse(new String(resp.getBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        sb.append("JAR ANALYZER 感谢以下贡献者\n\n");
        sb.append("感谢列表:\n\n");
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            String url = (String) obj.get("url");
            req.setUrl(new URL(url));
            resp = client.request(req);
            JSONObject newObject = JSON.parseObject(new String(resp.getBody(), StandardCharsets.UTF_8));
            sb.append((i + 1));
            sb.append(".");
            sb.append(" ");
            sb.append(newObject.get("name"));
            sb.append(" (").append(obj.get("html_url")).append(") ");
            sb.append("COMMITS: ").append((int) obj.get("contributions"));
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("感谢以上师傅们，也希望其他师傅们也可以贡献代码！");
        Path thanksPath = Paths.get("src/main/resources/thanks.md");
        System.out.println(sb);
        try {
            Files.delete(thanksPath);
        } catch (Exception ignored) {
        }
        try {
            Files.write(thanksPath, sb.toString().getBytes());
        } catch (Exception ignored) {
        }
    }
}
