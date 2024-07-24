package me.n1ar4.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.http.HttpRequest;
import me.n1ar4.jar.analyzer.http.HttpResponse;
import me.n1ar4.jar.analyzer.http.Y4Client;
import me.n1ar4.jar.analyzer.utils.ColorUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * RELEASE 下载量分析程序
 */
public class Counter {
    public static void main(String[] args) throws Exception {
        String target = "https://api.github.com/repos/jar-analyzer/jar-analyzer/releases";
        Y4Client client = new Y4Client();
        HttpRequest req = new HttpRequest();
        req.setMethod("GET");
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        headers.put("Authorization", "Bearer " +
                new String(Files.readAllBytes(Paths.get("token.txt"))).trim());
        req.setHeaders(headers);
        req.setUrl(new URL(target));
        HttpResponse resp = client.request(req);
        if (resp.getStatusCode() != 200) {
            System.out.println(resp.getStatusCode());
            throw new RuntimeException("request error");
        }
        JSONArray obj = JSONArray.parse(new String(resp.getBody(), StandardCharsets.UTF_8));
        for (int i = 0; i < obj.size(); i++) {
            JSONObject object = obj.getJSONObject(i);
            System.out.println(ColorUtil.yellow("RELEASE NAME: " + object.get("name")));
            JSONArray assets = (JSONArray) object.get("assets");
            int totalDown = 0;
            for (int j = 0; j < assets.size(); j++) {
                JSONObject asset = assets.getJSONObject(j);
                System.out.print(asset.get("name"));
                int count = (int) asset.get("download_count");
                System.out.println("\tDOWNLOAD: " + count);
                totalDown = totalDown + count;
            }
            System.out.println(ColorUtil.green("TOTAL DOWNLOAD: " + totalDown));
            System.out.println("--------------------------------------------------------------");
        }
    }
}
