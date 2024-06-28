package me.n1ar4.jar.analyzer.server;

import fi.iki.elonen.NanoHTTPD;

public interface HttpHandler {
    NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session);
}
