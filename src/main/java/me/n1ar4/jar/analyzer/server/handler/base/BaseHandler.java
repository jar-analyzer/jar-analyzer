/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server.handler.base;

import fi.iki.elonen.NanoHTTPD;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseHandler {
    private static final Logger logger = LogManager.getLogger();

    public String getClassName(NanoHTTPD.IHTTPSession session) {
        List<String> clazz = session.getParameters().get("class");
        if (clazz == null || clazz.isEmpty()) {
            return "";
        }
        String className = clazz.get(0);
        return className.replace('.', '/');
    }

    public String getMethodName(NanoHTTPD.IHTTPSession session) {
        List<String> m = session.getParameters().get("method");
        if (m == null || m.isEmpty()) {
            return "";
        }
        return m.get(0);
    }

    public String getMethodDesc(NanoHTTPD.IHTTPSession session) {
        List<String> d = session.getParameters().get("desc");
        if (d == null || d.isEmpty()) {
            return "";
        }
        return d.get(0);
    }

    public String getStr(NanoHTTPD.IHTTPSession session) {
        List<String> d = session.getParameters().get("str");
        if (d == null || d.isEmpty()) {
            return "";
        }
        return d.get(0);
    }

    public NanoHTTPD.Response buildJSON(String json) {
        if (json == null || json.isEmpty()) {
            return NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "application/json",
                    "{}");
        } else {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            int lengthInBytes = bytes.length;
            if (lengthInBytes > 3 * 1024 * 1024) {
                return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.INTERNAL_ERROR,
                        "text/html",
                        "<h1>JAR ANALYZER SERVER</h1>" +
                                "<h2>JSON IS TOO LARGE</h2>" +
                                "<h2>MAX SIZE 3 MB</h2>");
            } else {
                return NanoHTTPD.newFixedLengthResponse(
                        NanoHTTPD.Response.Status.OK,
                        "application/json",
                        json);
            }
        }
    }

    public NanoHTTPD.Response needParam(String s) {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                String.format("<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>NEED PARAM: %s</h2>", s));
    }

    public NanoHTTPD.Response error() {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>CORE ENGINE IS NULL</h2>");
    }

    /**
     * 从反编译的类代码中提取指定方法的代码
     *
     * @param classCode  完整的类代码
     * @param methodName 方法名
     * @param methodDesc 方法描述符（可选）
     * @return 方法代码，如果未找到则返回null
     */
    protected String extractMethodCode(String classCode, String methodName, String methodDesc) {
        if (StringUtil.isNull(classCode) || StringUtil.isNull(methodName)) {
            return null;
        }

        try {
            String[] lines = classCode.split("\n");
            StringBuilder methodCode = new StringBuilder();
            boolean inMethod = false;
            int braceCount = 0;
            boolean foundMethod = false;

            // 构建方法匹配的正则表达式
            // 匹配方法声明，考虑各种修饰符和参数
            String methodPattern;
            if (!StringUtil.isNull(methodDesc)) {
                // 如果有方法描述符，尝试更精确的匹配
                methodPattern = ".*\\b" + Pattern.quote(methodName) + "\\s*\\(.*\\).*\\{?";
            } else {
                // 只根据方法名匹配
                methodPattern = ".*\\b" + Pattern.quote(methodName) + "\\s*\\(.*\\).*\\{?";
            }

            Pattern pattern = Pattern.compile(methodPattern);

            for (String line : lines) {
                if (!inMethod) {
                    Matcher matcher = pattern.matcher(line.trim());
                    if (matcher.matches()) {
                        // 找到方法开始
                        inMethod = true;
                        foundMethod = true;
                        methodCode.append(line).append("\n");

                        // 计算大括号
                        braceCount += countChar(line, '{') - countChar(line, '}');

                        // 如果方法声明在一行内完成且没有大括号，可能是抽象方法或接口方法
                        if (line.trim().endsWith(";")) {
                            break;
                        }
                    }
                } else {
                    // 在方法内部
                    methodCode.append(line).append("\n");
                    braceCount += countChar(line, '{') - countChar(line, '}');

                    // 如果大括号平衡，方法结束
                    if (braceCount <= 0) {
                        break;
                    }
                }
            }

            if (foundMethod) {
                return methodCode.toString().trim();
            } else {
                return findMethodByFuzzyMatch(classCode, methodName);
            }

        } catch (Exception e) {
            logger.warn("Error extracting method code: " + e.getMessage());
            return null;
        }
    }

    /**
     * 模糊匹配方法
     */
    protected String findMethodByFuzzyMatch(String classCode, String methodName) {
        try {
            String[] lines = classCode.split("\n");
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains(methodName) &&
                        (line.contains("(") && line.contains(")"))) {

                    // 找到可能的方法，收集完整的方法代码
                    result.append(line).append("\n");

                    if (line.trim().endsWith(";")) {
                        // 抽象方法或接口方法
                        break;
                    }

                    int braceCount = countChar(line, '{') - countChar(line, '}');

                    for (int j = i + 1; j < lines.length && braceCount > 0; j++) {
                        String nextLine = lines[j];
                        result.append(nextLine).append("\n");
                        braceCount += countChar(nextLine, '{') - countChar(nextLine, '}');
                    }
                    break;
                }
            }
            return result.length() > 0 ? result.toString().trim() : null;
        } catch (Exception e) {
            logger.warn("error in fuzzy method matching: " + e.getMessage());
            return null;
        }
    }

    /**
     * 计算字符串中指定字符的数量
     */
    protected int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }
}
