/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.graph;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.server.handler.CSSHandler;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;
import java.util.UUID;

public class HtmlGraphUtil {
    private static final Logger logger = LogManager.getLogger();

    private static final String D3DS_STR = "__D3JS__";
    private static final String GRAPH_DATA_STR = "__GRAPH_DATA__";
    private static final String CSP_STR = "__CSP__";
    private static final String NONCE_STR = "__NONCE__";

    private static String getTemplate() {
        InputStream is = CSSHandler.class.getClassLoader()
                .getResourceAsStream("graph.html.temp");
        if (is == null) {
            return null;
        }
        return IOUtil.readString(is);
    }

    public static String render(GraphData data) {
        String temp = getTemplate();
        if (temp == null) {
            logger.error("templates is null");
            return null;
        }

        int port = GlobalOptions.getServerConfig().getPort();
        String d3dsPath = String.format(
                "http://127.0.0.1:%d/static/d3v6.js", port);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String csp = String.format(
                "default-src 'none'; script-src %s 'nonce-%s'; "
                        + "style-src 'unsafe-inline'; connect-src 'none'",
                d3dsPath, nonce);
        String graphJson = escapeJsonForHtmlScript(JSON.toJSONString(data));

        return temp.replace(D3DS_STR, d3dsPath)
                .replace(CSP_STR, csp)
                .replace(NONCE_STR, nonce)
                .replace(GRAPH_DATA_STR, graphJson);
    }

    static String escapeJsonForHtmlScript(String json) {
        return json.replace("&", "\\u0026")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
    }
}
