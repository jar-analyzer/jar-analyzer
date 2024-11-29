/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.http;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Y4Client {
    private static final Logger logger = LogManager.getLogger();
    private OkHttpClient client;
    public static Map<String, String> baseHeaders = new HashMap<>();

    public static boolean enabled = true;

    static {
        baseHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
    }

    public static final Y4Client INSTANCE = new Y4Client();

    public Y4Client() {
        if (enabled) {
            this.reConfig();
        }
    }

    public void reConfig() {
        if (enabled) {
            this.client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
    }

    public HttpResponse get(String url) {
        if (enabled) {
            try {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .get();
                return getHttpResponse(baseHeaders, requestBuilder);
            } catch (Exception ex) {
                logger.error("http get error: {}", ex.toString());
                return null;
            }
        }
        return null;
    }

    public HttpResponse post(String url, Map<String, String> headers, RequestBody body) throws IOException {
        if (enabled) {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body);
            return getHttpResponse(headers, requestBuilder);
        }
        return null;
    }

    public HttpResponse request(HttpRequest request) {
        if (enabled) {
            try {
                Request.Builder requestBuilder;
                if (request.getMethod().equals("GET")) {
                    requestBuilder = new Request.Builder()
                            .url(request.getUrl())
                            .method(request.getMethod(), null);
                } else {
                    requestBuilder = new Request.Builder()
                            .url(request.getUrl())
                            .method(request.getMethod(), RequestBody.create(request.getBody()));
                }
                return getHttpResponse(request.getHeaders(), requestBuilder);
            } catch (Exception ex) {
                logger.error("http request error: {}", ex.toString());
                return null;
            }
        }
        return null;
    }

    private HttpResponse getHttpResponse(Map<String, String> headers, Request.Builder requestBuilder) throws IOException {
        headers.forEach(requestBuilder::addHeader);
        Request request = requestBuilder.build();
        logger.debug("http request: {}", request.url().toString());
        try (Response response = client.newCall(request).execute()) {
            return new HttpResponse(response.code(), response.headers(),
                    response.body() != null ? response.body().bytes() : new byte[0]);
        }
    }
}
