/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

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
            return errorMsg("could not find favicon.ico");
        }
        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/x-icon", faviconIs);
    }
}
