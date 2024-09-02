/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    static {
        baseHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
    }

    public static final Y4Client INSTANCE = new Y4Client();

    public Y4Client() {
        this.reConfig();
    }

    public void reConfig() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public HttpResponse get(String url) {
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

    public HttpResponse post(String url, Map<String, String> headers, RequestBody body) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        return getHttpResponse(headers, requestBuilder);
    }

    public HttpResponse request(HttpRequest request) {
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
