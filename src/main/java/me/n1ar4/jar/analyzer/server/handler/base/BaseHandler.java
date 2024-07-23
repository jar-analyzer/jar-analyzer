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
