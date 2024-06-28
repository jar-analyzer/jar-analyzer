package me.n1ar4.jar.analyzer.server.handler.base;

import fi.iki.elonen.NanoHTTPD;

import java.util.List;

public class BaseHandler {
    public String getClassName(NanoHTTPD.IHTTPSession session) {
        List<String> clazz = session.getParameters().get("class");
        String className = clazz.get(0);
        return className.replace('.', '/');
    }

    public NanoHTTPD.Response buildJSON(String json) {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                json);
    }

    public NanoHTTPD.Response error() {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/plain",
                "JAR ANALYZER SERVER\n" +
                        "CORE ENGINE IS NULL");
    }
}
