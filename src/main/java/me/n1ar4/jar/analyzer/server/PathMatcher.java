package me.n1ar4.jar.analyzer.server;

import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

public class PathMatcher {
    public static Map<String, HttpHandler> handlers = new HashMap<>();

    static {
        handlers.put("/api/jars/list", new JarsPathHandler());
        handlers.put("/api/jars/class", new JarsClassHandler());
    }

    public static NanoHTTPD.Response handleReq(NanoHTTPD.IHTTPSession session){
        String uri = session.getUri();
        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            if(uri.startsWith(entry.getKey())){
                return entry.getValue().handle(session);
            }
        }
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.NOT_FOUND,
                "text/plain",
                "JAR ANALYZER SERVER\n" +
                        "URI NOT FOUND");
    }
}
