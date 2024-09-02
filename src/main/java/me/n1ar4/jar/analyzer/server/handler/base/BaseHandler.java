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

package me.n1ar4.jar.analyzer.server.handler.base;

import fi.iki.elonen.NanoHTTPD;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseHandler {
    public String getClassName(NanoHTTPD.IHTTPSession session) {
        List<String> clazz = session.getParameters().get("class");
        if (clazz == null || clazz.isEmpty()) {
            return "";
        }
        String className = clazz.get(0);
        return className.replace('.', '/');
    }

    public String getMethodName(NanoHTTPD.IHTTPSession session) {
        List<String> m = session.getParameters().get("method");
        if (m == null || m.isEmpty()) {
            return "";
        }
        return m.get(0);
    }

    public String getMethodDesc(NanoHTTPD.IHTTPSession session) {
        List<String> d = session.getParameters().get("desc");
        if (d == null || d.isEmpty()) {
            return "";
        }
        return d.get(0);
    }

    public String getStr(NanoHTTPD.IHTTPSession session) {
        List<String> d = session.getParameters().get("str");
        if (d == null || d.isEmpty()) {
            return "";
        }
        return d.get(0);
    }

    public NanoHTTPD.Response buildJSON(String json) {
        if (json == null || json.isEmpty()) {
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    "{}");
        } else {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            int lengthInBytes = bytes.length;
            if (lengthInBytes > 3 * 1024 * 1024) {
                return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.INTERNAL_ERROR,
                        "text/html",
                        "<h1>JAR ANALYZER SERVER</h1>" +
                                "<h2>JSON IS TOO LARGE</h2>" +
                                "<h2>MAX SIZE 3 MB</h2>");
            } else {
                return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.OK,
                        "application/json",
                        json);
            }
        }
    }

    public NanoHTTPD.Response needParam(String s) {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                String.format("<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>NEED PARAM: %s</h2>", s));
    }

    public NanoHTTPD.Response error() {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>CORE ENGINE IS NULL</h2>");
    }
}
