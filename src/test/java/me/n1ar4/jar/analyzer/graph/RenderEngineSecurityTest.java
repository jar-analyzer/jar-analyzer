/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.graph;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.server.ServerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderEngineSecurityTest {
    private static final Pattern GRAPH_DATA = Pattern.compile(
            "<script id=\"graph-data\"[^>]*>(.*?)</script>",
            Pattern.DOTALL);
    private static final Pattern NONCE = Pattern.compile(
            "<script nonce=\"([a-f0-9]+)\">");

    @BeforeAll
    static void setUp() {
        GlobalOptions.setServerConfig(new ServerConfig());
    }

    @Test
    void serializesUntrustedMethodNamesAsInertJsonData() {
        String maliciousName = "x', pwn: alert(1), z:'"
                + "\\\r\n\u2028\u2029</script><script>alert(2)</script>"
                + "__NONCE____CSP____GRAPH_DATA__";
        MethodResult current = new MethodResult(
                "demo/Test", "safe", "()V");
        MethodResult malicious = new MethodResult(
                "evil/Payload", maliciousName, "()V");

        String html = RenderEngine.renderGraph(
                current,
                Collections.singletonList(malicious),
                Collections.<MethodResult>emptyList());

        assertNotNull(html);
        Matcher dataMatcher = GRAPH_DATA.matcher(html);
        assertTrue(dataMatcher.find(), "graph JSON data block is missing");
        String jsonText = dataMatcher.group(1);
        assertFalse(jsonText.contains("<"),
                "untrusted data must not contain an HTML tag opener");
        assertTrue(jsonText.contains("\\u003C/script\\u003E"));
        assertTrue(jsonText.contains("\\u2028"));
        assertTrue(jsonText.contains("\\u2029"));

        JSONObject graph = JSON.parseObject(jsonText);
        JSONArray nodes = graph.getJSONArray("nodes");
        assertEquals(2, nodes.size());
        assertTrue(nodes.stream()
                .map(node -> ((JSONObject) node).getString("name"))
                .anyMatch(("Payload " + maliciousName)::equals));

        assertFalse(html.contains(
                "{ id: '"), "nodes must not be JavaScript source fragments");
    }

    @Test
    void preservesNormalGraphNodesAndLinkDirections() {
        MethodResult current = new MethodResult(
                "demo/Current", "run", "()V");
        MethodResult caller = new MethodResult(
                "demo/Caller", "call", "()V");
        MethodResult callee = new MethodResult(
                "demo/Callee", "work", "()V");

        String html = RenderEngine.renderGraph(
                current,
                Collections.singletonList(caller),
                Collections.singletonList(callee));
        JSONObject graph = extractGraphData(html);
        JSONArray nodes = graph.getJSONArray("nodes");
        JSONArray links = graph.getJSONArray("links");

        assertEquals(3, nodes.size());
        assertEquals(2, links.size());
        String currentId = graph.getString("currentNodeId");
        Set<String> names = new HashSet<>();
        String callerId = null;
        String calleeId = null;
        for (Object value : nodes) {
            JSONObject node = (JSONObject) value;
            String name = node.getString("name");
            names.add(name);
            if ("Caller call".equals(name)) {
                callerId = node.getString("id");
            } else if ("Callee work".equals(name)) {
                calleeId = node.getString("id");
            }
        }
        assertEquals(new HashSet<>(Arrays.asList(
                "Current run", "Caller call", "Callee work")), names);
        assertNotNull(callerId);
        assertNotNull(calleeId);

        assertTrue(hasLink(links, callerId, currentId));
        assertTrue(hasLink(links, currentId, calleeId));
    }

    @Test
    void appliesNonceBasedContentSecurityPolicy() {
        String html = RenderEngine.renderGraph(
                new MethodResult("demo/Test", "safe", "()V"),
                Collections.<MethodResult>emptyList(),
                Collections.<MethodResult>emptyList());

        Matcher nonceMatcher = NONCE.matcher(html);
        assertTrue(nonceMatcher.find(), "inline graph script nonce is missing");
        String nonce = nonceMatcher.group(1);
        assertTrue(html.contains("'nonce-" + nonce + "'"));
        assertTrue(html.contains("default-src 'none'"));
        assertTrue(html.contains("connect-src 'none'"));
        assertTrue(html.contains(
                "script-src http://127.0.0.1:10032/static/d3v6.js"));
        assertFalse(html.contains("__NONCE__"));
        assertFalse(html.contains("__CSP__"));
        assertFalse(html.contains("__GRAPH_DATA__"));
    }

    @Test
    void processGraphStillWritesAUtf8HtmlReport() throws Exception {
        String fileName = RenderEngine.processGraph(
                new MethodResult("demo/Test", "安全方法", "()V"),
                Collections.<MethodResult>emptyList(),
                Collections.<MethodResult>emptyList());
        assertNotNull(fileName);

        Path report = Paths.get(fileName);
        try {
            String html = new String(
                    Files.readAllBytes(report), StandardCharsets.UTF_8);
            JSONObject graph = extractGraphData(html);
            JSONArray nodes = graph.getJSONArray("nodes");
            assertEquals("Test 安全方法",
                    nodes.getJSONObject(0).getString("name"));
        } finally {
            Files.deleteIfExists(report);
        }
    }

    private static JSONObject extractGraphData(String html) {
        Matcher matcher = GRAPH_DATA.matcher(html);
        assertTrue(matcher.find());
        return JSON.parseObject(matcher.group(1));
    }

    private static boolean hasLink(JSONArray links,
                                   String source, String target) {
        for (Object value : links) {
            JSONObject link = (JSONObject) value;
            if (source.equals(link.getString("source"))
                    && target.equals(link.getString("target"))) {
                return true;
            }
        }
        return false;
    }
}
