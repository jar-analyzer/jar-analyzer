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

package me.n1ar4.jar.analyzer.graph;

import me.n1ar4.jar.analyzer.entity.MethodResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RenderEngine {
    private static final Map<String, MethodResult> methodIdStringMap = new HashMap<>();

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
        methodIdStringMap.clear();
        MethodData data = new MethodData();
        String curId = generateId();
        data.setUuid(curId);
        data.setMethodString(getShortClassName(cur.getClassName()) + " " + cur.getMethodName());
        List<String> callerIds = new ArrayList<>();
        methodIdStringMap.put(curId, cur);
        for (MethodResult c : caller) {
            String id = generateId();
            methodIdStringMap.put(id, c);
            callerIds.add(id);
        }
        List<String> calleeIds = new ArrayList<>();
        for (MethodResult c : callee) {
            String id = generateId();
            methodIdStringMap.put(id, c);
            calleeIds.add(id);
        }
        data.setCalleeIds(calleeIds);
        data.setCallerIds(callerIds);

        StringBuilder nodesBuffer = new StringBuilder();
        for (Map.Entry<String, MethodResult> entry : methodIdStringMap.entrySet()) {
            String id = entry.getKey();
            MethodResult mr = entry.getValue();
            String temp = String.format("{ id: '%s', name: '%s' },\n", id,
                    getShortClassName(mr.getClassName()) + " " + mr.getMethodName());
            nodesBuffer.append(temp);
        }

        StringBuilder linksBuffer = new StringBuilder();
        for (String callerId : callerIds) {
            String temp = String.format("{ source: '%s', target: '%s' },\n", callerId, curId);
            linksBuffer.append(temp);
        }
        for (String calleeId : calleeIds) {
            String temp = String.format("{ source: '%s', target: '%s' },\n", curId, calleeId);
            linksBuffer.append(temp);
        }

        GraphData graphData = new GraphData();
        graphData.setCurrentNodeId(data.getUuid());
        graphData.setNodes(nodesBuffer.toString());
        graphData.setLinks(linksBuffer.toString());

        String html = HtmlGraphUtil.render(graphData);
        if (html == null) {
            return null;
        }
        try {
            String fileName = String.format("jar-analyzer-graph-%d.html", System.currentTimeMillis());
            Files.write(Paths.get(fileName), html.getBytes());
            return fileName;
        } catch (Exception ignored) {
        }
        return null;
    }
}
