package me.n1ar4.jar.analyzer.server;

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.List;

public class JarsClassHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        CoreEngine engine = MainForm.getEngine();
        if (engine == null) {
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain",
                    "JAR ANALYZER SERVER\n" +
                            "CORE ENGINE IS NULL");
        }
        List<String> clazz = session.getParameters().get("class");
        String className = clazz.get(0);
        className = className.replace('.', '/');
        String res = engine.getJarByClass(className);
        String json = String.format("{\"%s\":\"%s\"}", "jar_name", res);
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "application/json",
                json);
    }
}
