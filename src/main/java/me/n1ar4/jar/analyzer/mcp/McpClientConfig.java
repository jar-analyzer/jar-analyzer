/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * Builds the MCP client configuration snippets documented in README.md.
 */
public final class McpClientConfig {
    public enum Transport {
        SSE("SSE", "sse", "/sse"),
        STREAMABLE_HTTP("Streamable HTTP", "http", "/mcp");

        private final String displayName;
        private final String type;
        private final String path;

        Transport(String displayName, String type, String path) {
            this.displayName = displayName;
            this.type = type;
            this.path = path;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private McpClientConfig() {
    }

    public static String build(Transport transport, String bind, int port) {
        if (transport == null) {
            throw new IllegalArgumentException("transport is required");
        }
        String host = bind == null ? "" : bind.trim();
        if (host.isEmpty()) {
            host = "127.0.0.1";
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }

        JSONObject server = new JSONObject();
        server.put("type", transport.type);
        server.put("url", "http://" + formatHost(host) + ":" + port + transport.path);

        JSONObject servers = new JSONObject();
        servers.put("jar-analyzer-mcp", server);

        JSONObject root = new JSONObject();
        root.put("mcpServers", servers);
        return JSON.toJSONString(root, JSONWriter.Feature.PrettyFormat)
                .replace("\t", "  ");
    }

    private static String formatHost(String host) {
        if (host.indexOf(':') >= 0 && !host.startsWith("[") && !host.endsWith("]")) {
            return "[" + host + "]";
        }
        return host;
    }
}
