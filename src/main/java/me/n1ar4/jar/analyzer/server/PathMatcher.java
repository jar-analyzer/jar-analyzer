/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.server;

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.server.handler.*;
import me.n1ar4.jar.analyzer.server.handler.base.HttpHandler;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class PathMatcher {
    private static final Logger logger = LogManager.getLogger();
    public static Map<String, HttpHandler> handlers = new HashMap<>();

    static {
        IndexHandler handler = new IndexHandler();
        handlers.put("/index", handler);
        handlers.put("/index.html", handler);
        handlers.put("/index.jsp", handler);
        handlers.put("/favicon.ico", new FaviconHandler());
        handlers.put("/static/boot.js", new JSHandler());
        handlers.put("/static/d3v6.js", new D3Handler());
        handlers.put("/static/boot.css", new CSSHandler());

        handlers.put("/api/get_jars_list", new GetJarListHandler());
        handlers.put("/api/get_jar_by_class", new GetJarByClassHandler());

        handlers.put("/api/get_callers", new GetCallersHandler());
        handlers.put("/api/get_callers_like", new GetCallersLikeHandler());
        handlers.put("/api/get_callee", new GetCalleeHandler());

        handlers.put("/api/get_method", new GetMethodHandler());
        handlers.put("/api/get_method_like", new GetMethodLikeHandler());
        handlers.put("/api/get_methods_by_str", new GetMethodsByStrHandler());
        handlers.put("/api/get_methods_by_class", new GetMethodsByClassHandler());

        handlers.put("/api/get_impls", new GetImplsHandler());
        handlers.put("/api/get_super_impls", new GetSuperImplsHandler());

        handlers.put("/api/get_all_spring_controllers", new GetAllSpringControllersHandler());
        handlers.put("/api/get_spring_mappings", new GetSpringMappingsHandler());

        handlers.put("/api/get_abs_path", new GetAbsPathHandler());

        handlers.put("/api/get_class_by_class", new GetClassByClassHandler());
    }

    public static NanoHTTPD.Response handleReq(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();

        logger.info("receive {} from {}", session.getRemoteIpAddress(), uri);

        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue().handle(session);
            }
        }
        IndexHandler handler = new IndexHandler();
        return handler.handle(session);
    }
}
