package me.n1ar4.jar.analyzer.sca;

import java.io.FileWriter;
import java.io.IOException;

public class ReportGenerator {
    public static void generateHtmlReport(String vulnerabilities, String filePath) throws IOException {
        String[] entries = vulnerabilities.split("\n\n");
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" " +
                        "content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n")
                .append("<title>Jar Analyzer 漏洞报告</title>\n")
                .append("<link href=\"https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css\" rel=\"stylesheet\">\n")
                .append("<script src=\"https://cdn.bootcdn.net/ajax/libs/jquery/3.7.1/jquery.min.js\"></script>\n")
                .append("<script src=\"https://cdn.bootcdn.net/ajax/libs/popper.js/2.11.8/cjs/popper.min.js\"></script>\n")
                .append("<script src=\"https://cdn.bootcdn.net/ajax/libs/twitter-bootstrap/5.3.3/js/bootstrap.min.js\"></script>\n")
                .append("<style>\n")
                .append(".card { margin-bottom: 1rem; }\n")
                .append(".card-header { font-weight: bold; font-size: 1.25rem; }\n")
                .append(".card-title { font-size: 1rem; margin-top: 0.5rem; }\n")
                .append("</style>\n")
                .append("</head>\n<body>\n<div class=\"container\">\n")
                .append("<h1 class=\"mt-5 mb-4\">Jar Analyzer 漏洞报告</h1>\n")
                .append("<div class=\"accordion\" id=\"accordionExample\">\n");
        for (int index = 0; index < entries.length; index++) {
            String entry = entries[index];
            String[] lines = entry.split("\n");
            String cve = lines[0].split(":")[1].trim();
            htmlContent.append("<div class=\"card\">\n")
                    .append("<div class=\"card-header\" id=\"heading").append(index).append("\">\n")
                    .append("<h2 class=\"mb-0\">\n")
                    .append("<button class=\"btn btn-link\" type=\"button\" " +
                            "data-toggle=\"collapse\" data-target=\"#collapse").append(index).append(
                            "\" aria-expanded=\"true\" aria-controls=\"collapse").append(index).append("\">\n")
                    .append(cve).append("\n")
                    .append("</button>\n")
                    .append("</h2>\n")
                    .append("</div>\n")
                    .append("<div id=\"collapse").append(index).append(
                            "\" class=\"collapse\" aria-labelledby=\"heading").append(index).append(
                            "\" data-parent=\"#accordionExample\">\n")
                    .append("<div class=\"card-body\">\n");
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
                htmlContent.append("<h5 class=\"card-title\">").append(title).append("</h5>\n");
                htmlContent.append("<p class=\"card-text\">").append(content).append("</p>\n");
            }
            htmlContent.append("</div>\n</div>\n</div>\n");
        }
        htmlContent.append("</div>\n</div>\n</body>\n</html>");
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(htmlContent.toString());
        }
    }
}
