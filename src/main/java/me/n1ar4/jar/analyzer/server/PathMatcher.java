/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
    private final ServerConfig config;

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

        handlers.put("/api/get_abs_path", new GetAbsPathHandler());

        //
        // 以上 API 不应该被 MCP 调用 内部使用
        // 以下 API 允许被 MCP 调用 对外
        //

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

        handlers.put("/api/get_all_servlets", new GetAllServletsHandler());

        handlers.put("/api/get_all_listeners", new GetAllListenersHandler());

        handlers.put("/api/get_all_filters", new GetAllFiltersHandler());

        handlers.put("/api/get_class_by_class", new GetClassByClassHandler());

        handlers.put("/api/fernflower_code", new GetCodeFernflowerHandler());
        handlers.put("/api/cfr_code", new GetCodeCFRHandler());
    }

    public PathMatcher(ServerConfig config) {
        this.config = config;
    }

    private NanoHTTPD.Response buildTokenError() {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>NEED TOKEN HEADER</h2>");
    }

    public NanoHTTPD.Response handleReq(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();

        if (config.isAuth()) {
            // 这个框架有 BUG 大小写问题
            String authA = session.getHeaders().get("Token");
            String authB = session.getHeaders().get("token");
            boolean access = false;
            if (authA != null && !authA.trim().isEmpty()) {
                if (authA.equals(config.getToken())) {
                    access = true;
                }
            }
            if (authB != null && !authB.trim().isEmpty()) {
                if (authB.equals(config.getToken())) {
                    access = true;
                }
            }
            if (!access) {
                return buildTokenError();
            }
        }

        logger.debug("receive {} from {}", session.getRemoteIpAddress(), uri);

        for (Map.Entry<String, HttpHandler> entry : handlers.entrySet()) {
            if (uri.equals(entry.getKey())) {
                return entry.getValue().handle(session);
            }
        }
        IndexHandler handler = new IndexHandler();
        return handler.handle(session);
    }
}
