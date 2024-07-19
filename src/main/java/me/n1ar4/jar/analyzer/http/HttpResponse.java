package me.n1ar4.jar.analyzer.http;

import okhttp3.Headers;

public class HttpResponse {
    private final int statusCode;
    private final Headers headers;
    private final byte[] body;

    public HttpResponse(int statusCode, Headers headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}