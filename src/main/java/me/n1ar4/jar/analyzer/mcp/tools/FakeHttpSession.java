/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp.tools;

import me.n1ar4.server.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于在进程内伪造 IHTTPSession
 * 直接调用 jar-analyzer 内置 HttpHandler，无需走真实 HTTP 通信
 */
public class FakeHttpSession implements NanoHTTPD.IHTTPSession {
    private final String uri;
    private final Map<String, List<String>> parameters;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public FakeHttpSession(String uri, Map<String, List<String>> parameters) {
        this.uri = uri;
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
    }

    public static FakeHttpSession of(String uri) {
        return new FakeHttpSession(uri, new LinkedHashMap<>());
    }

    public FakeHttpSession addParam(String key, String value) {
        if (value == null) return this;
        List<String> list = new ArrayList<>();
        list.add(value);
        parameters.put(key, list);
        return this;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public String getRemoteIpAddress() {
        return "127.0.0.1";
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }
}
