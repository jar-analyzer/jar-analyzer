package me.n1ar4.jar.analyzer.server.handler;

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;

import java.io.InputStream;

public class FaviconHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        InputStream faviconIs = CSSHandler.class.getClassLoader().getResourceAsStream("favicon.ico");
        if (faviconIs == null) {
            return error();
        }
        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/x-icon", faviconIs);
    }
}
