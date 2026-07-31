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

import me.n1ar4.jar.analyzer.entity.MethodResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RenderEngine {
    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    private static String getShortClassName(String fullClassName) {
        fullClassName = fullClassName.replace("/", ".");
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }

    public static String processGraph(MethodResult cur,
                                      List<MethodResult> caller,
                                      List<MethodResult> callee) {
        String html = renderGraph(cur, caller, callee);
        if (html == null) {
            return null;
        }
        try {
            String fileName = String.format(
                    "jar-analyzer-graph-%d.html", System.currentTimeMillis());
            Files.write(Paths.get(fileName),
                    html.getBytes(StandardCharsets.UTF_8));
            return fileName;
        } catch (Exception ignored) {
        }
        return null;
    }

    static String renderGraph(MethodResult cur,
                              List<MethodResult> caller,
                              List<MethodResult> callee) {
        Map<String, MethodResult> methods = new LinkedHashMap<>();
        String curId = generateId();
        List<String> callerIds = new ArrayList<>();
        methods.put(curId, cur);
        for (MethodResult c : caller) {
            String id = generateId();
            methods.put(id, c);
            callerIds.add(id);
        }
        List<String> calleeIds = new ArrayList<>();
        for (MethodResult c : callee) {
            String id = generateId();
            methods.put(id, c);
            calleeIds.add(id);
        }

        List<Map<String, String>> nodes = new ArrayList<>();
        for (Map.Entry<String, MethodResult> entry : methods.entrySet()) {
            MethodResult mr = entry.getValue();
            Map<String, String> node = new LinkedHashMap<>();
            node.put("id", entry.getKey());
            node.put("name", getShortClassName(mr.getClassName())
                    + " " + mr.getMethodName());
            nodes.add(node);
        }

        List<Map<String, String>> links = new ArrayList<>();
        for (String callerId : callerIds) {
            links.add(link(callerId, curId));
        }
        for (String calleeId : calleeIds) {
            links.add(link(curId, calleeId));
        }

        GraphData graphData = new GraphData();
        graphData.setCurrentNodeId(curId);
        graphData.setNodes(nodes);
        graphData.setLinks(links);
        return HtmlGraphUtil.render(graphData);
    }

    private static Map<String, String> link(String source, String target) {
        Map<String, String> link = new LinkedHashMap<>();
        link.put("source", source);
        link.put("target", target);
        return link;
    }
}
