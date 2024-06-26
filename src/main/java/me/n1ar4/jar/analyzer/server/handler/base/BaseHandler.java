package me.n1ar4.jar.analyzer.server.handler.base;

import fi.iki.elonen.NanoHTTPD;

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
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    json);
        }
    }

    public NanoHTTPD.Response error() {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>CORE ENGINE IS NULL</h2>");
    }
}
