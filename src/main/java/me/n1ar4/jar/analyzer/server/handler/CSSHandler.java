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

import me.n1ar4.jar.analyzer.server.handler.base.BaseHandler;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.server.NanoHTTPD;

import java.io.InputStream;

public class CSSHandler extends BaseHandler implements HttpHandler {
    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        InputStream is = CSSHandler.class.getClassLoader().getResourceAsStream("report/BT_CSS.css");
        if (is == null) {
            return errorMsg("could not find BT_CSS.css");
        }
        String css = IOUtil.readString(is);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/css", css);
    }
}
