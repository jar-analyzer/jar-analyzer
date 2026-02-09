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

import me.n1ar4.jar.analyzer.engine.LRUCache;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.server.NanoHTTPD;
import org.objectweb.asm.Type;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseHandler {
    private static final Logger logger = LogManager.getLogger();

    private static final LRUCache lruCache = new LRUCache();

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
                String.format("<h1>JAR ANALYZER SERVER</h1>" +
                                "<h2>CORE ENGINE IS NULL</h2>" +
                                "<h2>CHECK %s FILE AND %s DIR</h2>",
                        Const.dbFile, Const.tempDir));
    }

    public NanoHTTPD.Response errorMsg(String s) {
        return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.INTERNAL_ERROR,
                "text/html",
                "<h1>JAR ANALYZER SERVER</h1>" +
                        "<h2>" + s + "</h2>");
    }

    /**
     * 从反编译的类代码中提取指定方法的代码
     *
     * @param classCode  完整的类代码
     * @param methodName 方法名
     * @param methodDesc 方法描述符（可选）
     * @return 方法代码，如果未找到则返回null
     */
    protected String extractMethodCode(String className, String classCode, String methodName, String methodDesc) {
        if (StringUtil.isNull(classCode) || StringUtil.isNull(methodName)) {
            return null;
        }

        // 2026-02-09 LRU CACHE
        String key = className + "#" + methodName + "#" + methodDesc;
        String resultCode = lruCache.get(key);
        if (resultCode != null && !resultCode.isEmpty()) {
            logger.info("return lru cache : {}", className);
            return resultCode;
        }

        try {
            String[] lines = classCode.split("\n");
            StringBuilder methodCode = new StringBuilder();
            boolean inMethod = false;
            int braceCount = 0;
            boolean foundMethod = false;

            // 构建方法匹配的正则表达式
            // 匹配方法声明，考虑各种修饰符和参数
            String methodPattern = ".*\\b" + Pattern.quote(methodName) + "\\s*\\(.*\\).*\\{?";

            Pattern pattern = Pattern.compile(methodPattern);

            for (String line : lines) {
                if (!inMethod) {
                    Matcher matcher = pattern.matcher(line.trim());
                    if (matcher.matches()) {
                        // 检查参数是否匹配
                        if (!StringUtil.isNull(methodDesc)) {
                            if (!checkParams(line, methodName, methodDesc)) {
                                continue;
                            }
                        }

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
                String foundResult = methodCode.toString().trim();
                lruCache.put(key, foundResult);
                return foundResult;
            } else {
                String fuzzResult = findMethodByFuzzyMatch(classCode, methodName);
                lruCache.put(key, fuzzResult);
                return fuzzResult;
            }

        } catch (Exception e) {
            logger.warn("Error extracting method code: " + e.getMessage());
            return null;
        }
    }

    private boolean checkParams(String line, String methodName, String methodDesc) {
        try {
            // 提取括号内的内容
            int nameIndex = line.indexOf(methodName);
            int leftParen = line.indexOf('(', nameIndex);
            if (leftParen == -1) return false;

            // 找到对应的右括号
            int rightParen = -1;
            int balance = 0;
            for (int i = leftParen; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '(') balance++;
                else if (c == ')') balance--;

                if (balance == 0) {
                    rightParen = i;
                    break;
                }
            }
            if (rightParen == -1) return false;

            String paramsStr = line.substring(leftParen + 1, rightParen);

            // 解析描述符
            Type[] argTypes = Type.getArgumentTypes(methodDesc);

            // 分割参数字符串
            List<String> params = splitParams(paramsStr);

            // 检查参数数量
            if (params.size() != argTypes.length) {
                return false;
            }

            // 检查每个参数类型
            for (int i = 0; i < argTypes.length; i++) {
                String paramCode = params.get(i);
                Type type = argTypes[i];
                String className = type.getClassName();
                // 获取简单类名
                String simpleName = getSimpleName(className);

                // 检查 paramCode 是否包含 simpleName
                // 使用单词边界检查
                if (!containsWord(paramCode, simpleName)) {
                    // 特殊处理 varargs: String[] -> String...
                    if (simpleName.endsWith("[]")) {
                        String varargName = simpleName.substring(0, simpleName.length() - 2) + "...";
                        if (!containsWord(paramCode, varargName)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.warn("check params error: " + e.getMessage());
            return false;
        }
    }

    private List<String> splitParams(String paramsStr) {
        List<String> params = new ArrayList<>();
        int balance = 0;
        int lastStart = 0;
        for (int i = 0; i < paramsStr.length(); i++) {
            char c = paramsStr.charAt(i);
            if (c == '<') balance++;
            else if (c == '>') balance--;

            if (c == ',' && balance == 0) {
                params.add(paramsStr.substring(lastStart, i).trim());
                lastStart = i + 1;
            }
        }
        String last = paramsStr.substring(lastStart).trim();
        if (!last.isEmpty()) {
            params.add(last);
        }
        return params;
    }

    private String getSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot != -1) {
            return className.substring(lastDot + 1);
        }
        return className;
    }

    private boolean containsWord(String text, String word) {
        // 使用正则匹配单词边界
        // 转义 word 中的特殊字符（如 []）
        String pattern = ".*\\b" + Pattern.quote(word) + "\\b.*";
        // 注意：对于 [] 结尾的，\b 可能不起作用，因为 ] 是非单词字符
        // 如果 word 包含非单词字符，我们需要小心
        if (!word.matches(".*\\w$")) {
            // 如果以非单词字符结尾（如 []），则不能用右侧 \b
            pattern = ".*\\b" + Pattern.quote(word) + ".*";
            // 这里简化了，只要包含且左侧是边界即可
            // 更好的做法是手动检查
            return text.contains(word);
        }
        return text.matches(pattern);
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
