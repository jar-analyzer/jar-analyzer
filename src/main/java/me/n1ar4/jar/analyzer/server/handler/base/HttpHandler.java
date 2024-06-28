package me.n1ar4.jar.analyzer.server.handler.base;

import fi.iki.elonen.NanoHTTPD;

public interface HttpHandler {
    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}
