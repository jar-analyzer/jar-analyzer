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

package me.n1ar4.jar.analyzer.graph;

import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.server.handler.CSSHandler;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;

public class HtmlGraphUtil {
    private static final Logger logger = LogManager.getLogger();

    private static final String D3DS_STR = "__D3JS__";
    private static final String NODES_STR = "__NODES__";
    private static final String LINKS_STR = "__LINKS__";
    private static final String CURRENT_STR = "__CURRENT_NODE__";

    private static String getTemplate() {
        InputStream is = CSSHandler.class.getClassLoader().getResourceAsStream("graph.html.temp");
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

        int port = GlobalOptions.getServerPort();
        String d3dsPath = String.format("http://127.0.0.1:%d/static/d3v6.js", port);
        String htmlOutput = temp.replace(D3DS_STR, d3dsPath);
        // 示例：
        // 注意单引号逗号结尾有换行
        //    { id: 'A', name: 'Method A' },
        //    { id: 'B', name: 'Method B' },
        htmlOutput = htmlOutput.replace(NODES_STR, data.getNodes());
        // 示例：
        // 注意单引号逗号结尾有换行
        //    { source: 'A', target: 'B' },
        //    { source: 'A', target: 'C' },
        htmlOutput = htmlOutput.replace(LINKS_STR, data.getLinks());
        // 示例：
        // 注意无需有引号
        // A
        htmlOutput = htmlOutput.replace(CURRENT_STR, data.getCurrentNodeId());

        return htmlOutput;
    }
}
