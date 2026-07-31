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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class McpClientConfigTest {
    @Test
    void buildsReadmeSseConfiguration() {
        JSONObject server = server(McpClientConfig.build(
                McpClientConfig.Transport.SSE, "127.0.0.1", 20032));

        assertEquals("sse", server.getString("type"));
        assertEquals("http://127.0.0.1:20032/sse", server.getString("url"));
    }

    @Test
    void buildsReadmeStreamableConfiguration() {
        JSONObject server = server(McpClientConfig.build(
                McpClientConfig.Transport.STREAMABLE_HTTP,
                "127.0.0.1", 20032));

        assertEquals("http", server.getString("type"));
        assertEquals("http://127.0.0.1:20032/mcp", server.getString("url"));
    }

    @Test
    void usesCurrentAddressAndFormatsIpv6() {
        JSONObject server = server(McpClientConfig.build(
                McpClientConfig.Transport.SSE, "::1", 23456));

        assertEquals("http://[::1]:23456/sse", server.getString("url"));
    }

    @Test
    void rejectsInvalidPorts() {
        assertThrows(IllegalArgumentException.class, () ->
                McpClientConfig.build(McpClientConfig.Transport.SSE,
                        "127.0.0.1", 0));
    }

    @Test
    void usesCompactTwoSpaceIndentation() {
        String config = McpClientConfig.build(
                McpClientConfig.Transport.SSE, "127.0.0.1", 20032);

        assertFalse(config.contains("\t"));
        assertTrue(config.contains("\n  \"mcpServers\""));
        assertTrue(config.contains("\n    \"jar-analyzer-mcp\""));
    }

    private static JSONObject server(String config) {
        return JSON.parseObject(config)
                .getJSONObject("mcpServers")
                .getJSONObject("jar-analyzer-mcp");
    }
}
