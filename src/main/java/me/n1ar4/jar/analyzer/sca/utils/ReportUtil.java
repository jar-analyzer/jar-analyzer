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

package me.n1ar4.jar.analyzer.sca.utils;

import me.n1ar4.jar.analyzer.utils.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class ReportUtil {
    private static byte[] BT_CSS = null;
    private static byte[] BT_JS = null;
    private static byte[] JQ_JS = null;
    private static byte[] POPPER_JS = null;

    static {
        try {
            InputStream bcCssIs = ClassLoader.getSystemResourceAsStream("report/BT_CSS.css");
            BT_CSS = IOUtils.readAllBytes(bcCssIs);
            InputStream btJsIs = ClassLoader.getSystemResourceAsStream("report/BT_JS.js");
            BT_JS = IOUtils.readAllBytes(btJsIs);
            InputStream jqJsIs = ClassLoader.getSystemResourceAsStream("report/JQ_JS.js");
            JQ_JS = IOUtils.readAllBytes(jqJsIs);
            InputStream popperJsIs = ClassLoader.getSystemResourceAsStream("report/POPPER_JS.js");
            POPPER_JS = IOUtils.readAllBytes(popperJsIs);
        } catch (Exception ignored) {
        }
    }

    public static void generateHtmlReport(String vulnerabilities, String filePath) throws IOException {
        String[] entries = vulnerabilities.split("\n\n");
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html><html lang=\"zh-CN\"><head>")
                .append("<meta charset=\"UTF-8\"><meta name=\"viewport\" " +
                        "content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">")
                .append("<title>Jar Analyzer 漏洞报告</title>")
                .append("<style>").append(new String(BT_CSS)).append("</style>")
                .append("<script>").append(new String(JQ_JS)).append("</script>")
                .append("<script>").append(new String(BT_JS)).append("</script>")
                .append("<script>").append(new String(POPPER_JS)).append("</script>")
                .append("<style>")
                .append(".card { margin-bottom: 1rem; }")
                .append(".card-header { font-weight: bold; font-size: 1.25rem; }")
                .append(".card-title { font-size: 1rem; margin-top: 0.5rem; }")
                .append("</style>")
                .append("</head><body><div class=\"container\">")
                .append("<h1 class=\"mt-5 mb-4\">Jar Analyzer 漏洞报告</h1>")
                .append("<div class=\"accordion\" id=\"accordionExample\">");
        for (int index = 0; index < entries.length; index++) {
            String entry = entries[index];
            String[] lines = entry.split("\n");
            String cve = lines[0].split(":")[1].trim();
            htmlContent.append("<div class=\"card\">")
                    .append("<div class=\"card-header\" id=\"heading").append(index).append("\">")
                    .append("<h2 class=\"mb-0\">")
                    .append("<button class=\"btn btn-link\" type=\"button\" " +
                            "data-toggle=\"collapse\" data-target=\"#collapse").append(index).append(
                            "\" aria-expanded=\"true\" aria-controls=\"collapse").append(index).append("\">")
                    .append(cve)
                    .append("</button>")
                    .append("</h2>")
                    .append("</div>")
                    .append("<div id=\"collapse").append(index).append(
                            "\" class=\"collapse\" aria-labelledby=\"heading").append(index).append(
                            "\" data-parent=\"#accordionExample\">")
                    .append("<div class=\"card-body\">");
            for (int i = 1; i < lines.length; i++) {
                String[] parts = lines[i].split(":");
                String title = parts[0].trim();
                if (title.equals("DESC")) {
                    title = "描述";
                }
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j < parts.length; j++) {
                    sb.append(parts[j]);
                    sb.append(":");
                }
                String result = sb.toString();
                String content = result.substring(0, result.length() - 1);
                htmlContent.append("<h5 class=\"card-title\">").append(title).append("</h5>");
                if (title.equals("CVSS")) {
                    double val = Double.parseDouble(content);
                    htmlContent.append("<p class=\"card-text\">").append(content).append("&nbsp;&nbsp;&nbsp;");
                    if (val > 8.9) {
                        // 严重
                        htmlContent.append("<button type=\"button\" class=\"btn btn-dark\">CRITICAL</button>");
                        htmlContent.append("</p>");
                    } else if (val > 6.9) {
                        // 高危
                        htmlContent.append("<button type=\"button\" class=\"btn btn-danger\">HIGH</button>");
                        htmlContent.append("</p>");
                    } else if (val > 3.9) {
                        // 中危
                        htmlContent.append("<button type=\"button\" class=\"btn btn-warning\">MODERATE</button>");
                        htmlContent.append("</p>");
                    } else {
                        // 低危
                        htmlContent.append("<button type=\"button\" class=\"btn btn-secondary\">LOW</button>");
                        htmlContent.append("</p>");
                    }
                } else {
                    htmlContent.append("<p class=\"card-text\">").append(content).append("</p>");
                }
            }
            htmlContent.append("</div></div></div>");
        }
        htmlContent.append("</div></div></body></html>");
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(htmlContent.toString());
        }
    }
}
