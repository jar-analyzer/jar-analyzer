package com.best.hello.util;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class HttpClientUtils {

    // HTTPURLConnection类，是继承了URLConnection
    public static String HTTPURLConnection(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setInstanceFollowRedirects(false); // 不允许重定向
            conn.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String content;
            StringBuilder html = new StringBuilder();

            while ((content = reader.readLine()) != null) {
                html.append(content);
            }
            reader.close();
            return html.toString();

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // URLConnection类
    public static String URLConnection(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            // 通过getInputStream() 读取 URL 所引用的资源数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String content;
            StringBuffer html = new StringBuffer();

            while ((content = reader.readLine()) != null) {
                html.append(content);
            }
            reader.close();
            return html.toString();

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 用于webhook发送post包
    public static void sendPost(String url, Map<String, Object> param, Map<String, String> headParam) {
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();

            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");

            if (headParam != null) {
                for (Map.Entry<String, String> entry : headParam.entrySet()) {
                    // 设置header头
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(JSON.toJSONString(param));
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

