/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.gui;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

/**
 * 轻量 HTML 高亮渲染器（无第三方依赖）。
 * <p>
 * 用于 Workflow GUI 里若干富文本展示场景：
 * <ul>
 *   <li>{@link #renderJson(String)}        – JSON 美化 + key/string/number/bool 着色</li>
 *   <li>{@link #renderAgentTurn}           – Agent prompt/response 分块着色</li>
 * </ul>
 * <p>
 * 输出样式被设计为可直接塞进 {@link javax.swing.JEditorPane} (text/html)。
 * 字体使用等宽，行宽不限制（外层 ScrollPane 自带横向滚动），
 * 关键长文本通过 {@code white-space:pre-wrap} 自动换行。
 * <p>
 */
public final class HtmlSyntaxRenderer {

    private HtmlSyntaxRenderer() {
    }

    /* ------------------------- 公共配色（接近 GitHub light） ------------------------- */

    private static final String CLR_KEY = "#005cc5";   // JSON key
    private static final String CLR_STR = "#22863a";   // string literal
    private static final String CLR_NUM = "#b08800";   // number / boolean
    private static final String CLR_NULL = "#a9a9a9";  // null
    private static final String CLR_PUNC = "#586069";  // : , { } [ ]

    private static final String CLR_ROLE_BG_USER = "#eef6ff";
    private static final String CLR_ROLE_BG_ASSISTANT = "#f0f9ee";
    private static final String CLR_ROLE_BG_SYSTEM = "#fdf2f2";
    private static final String CLR_ROLE_BG_TOOL = "#fff8e1";

    /* ============================================================================
     * 入口 1：JSON
     * ============================================================================ */

    /**
     * 把 JSON 字符串渲染为高亮 HTML（可直接 setText 给 JEditorPane）。
     * 输入若不是合法 JSON，则原样转义后包裹 pre 返回（避免抛错）。
     */
    public static String renderJson(String raw) {
        StringBuilder html = new StringBuilder(4096);
        html.append("<html><body style='margin:0; padding:8px; "
                + "font-family:Consolas,\"Courier New\",monospace; font-size:12px;'>");
        html.append("<pre style='white-space:pre-wrap; word-break:break-word; margin:0;'>");
        if (raw == null || raw.isEmpty()) {
            html.append("</pre></body></html>");
            return html.toString();
        }
        Object obj;
        try {
            obj = JSON.parse(raw);
        } catch (Throwable t) {
            html.append(escape(raw));
            html.append("</pre></body></html>");
            return html.toString();
        }
        String pretty;
        try {
            pretty = JSON.toJSONString(obj,
                    JSONWriter.Feature.PrettyFormat,
                    JSONWriter.Feature.WriteMapNullValue);
        } catch (Throwable t) {
            pretty = raw;
        }
        html.append(highlightJson(pretty));
        html.append("</pre></body></html>");
        return html.toString();
    }

    /**
     * 对已经 pretty-printed 的 JSON 字符串做 token 着色（行级状态机）。
     */
    private static String highlightJson(String pretty) {
        StringBuilder out = new StringBuilder(pretty.length() * 2);
        int i = 0;
        int n = pretty.length();
        while (i < n) {
            char c = pretty.charAt(i);
            if (c == '"') {
                // 收集一个完整字符串字面量（含转义），然后判断是否是 key（后跟可选空白 + ":"）
                int start = i;
                i++;
                StringBuilder lit = new StringBuilder();
                lit.append('"');
                while (i < n) {
                    char d = pretty.charAt(i);
                    lit.append(d);
                    if (d == '\\' && i + 1 < n) {
                        lit.append(pretty.charAt(i + 1));
                        i += 2;
                        continue;
                    }
                    i++;
                    if (d == '"') {
                        break;
                    }
                }
                int j = i;
                // 跳过空白
                while (j < n && (pretty.charAt(j) == ' ' || pretty.charAt(j) == '\t')) {
                    j++;
                }
                boolean isKey = j < n && pretty.charAt(j) == ':';
                String esc = escape(lit.toString());
                if (isKey) {
                    out.append("<span style='color:").append(CLR_KEY).append(";'>")
                            .append(esc).append("</span>");
                } else {
                    out.append("<span style='color:").append(CLR_STR).append(";'>")
                            .append(esc).append("</span>");
                }
            } else if (c == ':' || c == ',' || c == '{' || c == '}' || c == '[' || c == ']') {
                out.append("<span style='color:").append(CLR_PUNC).append(";'>")
                        .append(c).append("</span>");
                i++;
            } else if ((c >= '0' && c <= '9') || c == '-') {
                int s = i;
                while (i < n) {
                    char d = pretty.charAt(i);
                    if ((d >= '0' && d <= '9') || d == '-' || d == '.'
                            || d == 'e' || d == 'E' || d == '+') {
                        i++;
                    } else {
                        break;
                    }
                }
                out.append("<span style='color:").append(CLR_NUM).append(";'>")
                        .append(escape(pretty.substring(s, i))).append("</span>");
            } else if ((c == 't' && i + 3 < n && pretty.startsWith("true", i))
                    || (c == 'f' && i + 4 < n && pretty.startsWith("false", i))) {
                int len = c == 't' ? 4 : 5;
                out.append("<span style='color:").append(CLR_NUM).append(";'>")
                        .append(pretty, i, i + len).append("</span>");
                i += len;
            } else if (c == 'n' && i + 3 < n && pretty.startsWith("null", i)) {
                out.append("<span style='color:").append(CLR_NULL).append(";'>null</span>");
                i += 4;
            } else if (c == '<' || c == '>' || c == '&') {
                out.append(escape(String.valueOf(c)));
                i++;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /* ============================================================================
     * 入口 2：Agent Turn（Prompt + Response）
     * ============================================================================ */

    /**
     * 渲染 Agent 单轮 prompt + response 的整页 HTML。
     *
     * @param classLabel 上下文标签（一般是 className）
     * @param round      轮序号（1-based）
     * @param time       时间字符串
     * @param prompt     发送的 prompt 原文（含多 role 段落，由 AiAgentRunner.formatMessages 生成）
     * @param response   返回的 response 原文（自然语言 + 工具调用概要）
     */
    public static String renderAgentTurn(String classLabel, int round, String time,
                                         String prompt, String response) {
        StringBuilder html = new StringBuilder(8192);
        html.append("<html><body style='margin:0; padding:0; "
                + "font-family:\"Segoe UI\",\"Microsoft YaHei\",sans-serif; font-size:12px; "
                + "color:#222;'>");

        // 元信息卡片
        html.append("<div style='background:#f6f8fa; border-bottom:1px solid #e1e4e8; "
                + "padding:10px 14px;'>");
        html.append("<div style='font-weight:bold; font-size:13px; color:#24292e;'>")
                .append(escape(classLabel == null || classLabel.isEmpty() ? "(no class)" : classLabel))
                .append("&nbsp;&nbsp;<span style='color:#6a737d; font-weight:normal;'>· round ")
                .append(round).append("</span></div>");
        html.append("<div style='color:#6a737d; margin-top:2px;'>")
                .append(escape(time == null ? "" : time))
                .append("</div>");
        html.append("</div>");

        // PROMPT
        html.append(sectionHeader("PROMPT (发送)", "#0366d6"));
        html.append(renderPromptBody(prompt));

        // RESPONSE
        html.append(sectionHeader("RESPONSE (返回)", "#28a745"));
        html.append(renderResponseBody(response));

        html.append("</body></html>");
        return html.toString();
    }

    private static String sectionHeader(String title, String accent) {
        return "<div style='margin:14px 14px 6px 14px; "
                + "border-left:3px solid " + accent + "; padding:2px 8px; "
                + "font-weight:bold; color:" + accent + "; font-size:12px;'>"
                + escape(title)
                + "</div>";
    }

    /**
     * 解析 prompt 文本：原始格式由 AiAgentRunner.formatMessages 产生：
     * <pre>
     * ================ SYSTEM ================
     * ...content...
     *
     * ================ USER ================
     * ...
     * </pre>
     * 每一段渲染为带角色色块的卡片。
     */
    private static String renderPromptBody(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return "<div style='margin:0 14px 8px 14px; color:#6a737d;'>(空)</div>";
        }
        StringBuilder out = new StringBuilder(prompt.length() * 2);
        out.append("<div style='margin:0 14px 8px 14px;'>");
        String[] segments = splitByRole(prompt);
        if (segments.length == 0) {
            out.append(plainBlock(prompt));
        } else {
            for (String seg : segments) {
                if (seg == null || seg.isEmpty()) {
                    continue;
                }
                int newline = seg.indexOf('\n');
                String role;
                String body;
                if (newline > 0) {
                    role = seg.substring(0, newline).trim();
                    body = seg.substring(newline + 1);
                } else {
                    role = seg.trim();
                    body = "";
                }
                out.append(roleCard(role, body));
            }
        }
        out.append("</div>");
        return out.toString();
    }

    /**
     * 按 "================ ROLE ================" 分隔符把 formatMessages 的输出切片。
     * 返回数组里每一项形如 "ROLE\nbody..."。
     */
    private static String[] splitByRole(String text) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "^=+\\s*([A-Z?]+)\\s*=+\\s*$",
                java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher m = p.matcher(text);
        java.util.List<int[]> matches = new java.util.ArrayList<>();
        java.util.List<String> roles = new java.util.ArrayList<>();
        while (m.find()) {
            matches.add(new int[]{m.start(), m.end()});
            roles.add(m.group(1));
        }
        if (matches.isEmpty()) {
            return new String[0];
        }
        String[] result = new String[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            int bodyStart = matches.get(i)[1];
            int bodyEnd = (i + 1 < matches.size()) ? matches.get(i + 1)[0] : text.length();
            String body = text.substring(bodyStart, bodyEnd).trim();
            result[i] = roles.get(i) + "\n" + body;
        }
        return result;
    }

    private static String roleCard(String role, String body) {
        String r = role == null ? "" : role.toUpperCase();
        String bg;
        String accent;
        switch (r) {
            case "USER":
                bg = CLR_ROLE_BG_USER;
                accent = "#0366d6";
                break;
            case "ASSISTANT":
                bg = CLR_ROLE_BG_ASSISTANT;
                accent = "#28a745";
                break;
            case "SYSTEM":
                bg = CLR_ROLE_BG_SYSTEM;
                accent = "#d73a49";
                break;
            case "TOOL":
                bg = CLR_ROLE_BG_TOOL;
                accent = "#b08800";
                break;
            default:
                bg = "#f6f8fa";
                accent = "#586069";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background:").append(bg)
                .append("; border-left:3px solid ").append(accent)
                .append("; padding:6px 10px; margin:6px 0; "
                        + "border-radius:0 4px 4px 0;'>");
        sb.append("<div style='font-weight:bold; color:").append(accent)
                .append("; font-size:11px; margin-bottom:4px; "
                        + "letter-spacing:0.5px;'>")
                .append(escape(r))
                .append("</div>");
        sb.append("<pre style='white-space:pre-wrap; word-break:break-word; "
                        + "margin:0; font-family:Consolas,\"Courier New\",monospace; "
                        + "font-size:12px; color:#24292e;'>")
                .append(highlightInlineCode(escape(body)))
                .append("</pre>");
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 渲染 response 主体。Response 结构相对简单：
     * 自然语言 + (可选) "[工具调用 tool_calls]" 区块。
     * 这里把工具调用部分识别出来单独着色。
     */
    private static String renderResponseBody(String response) {
        if (response == null || response.isEmpty()) {
            return "<div style='margin:0 14px 12px 14px; color:#6a737d;'>(空)</div>";
        }
        int idx = response.indexOf("[工具调用 tool_calls]");
        if (idx < 0) {
            return "<div style='margin:0 14px 12px 14px;'>"
                    + plainBlock(response) + "</div>";
        }
        String head = response.substring(0, idx).trim();
        String tail = response.substring(idx + "[工具调用 tool_calls]".length()).trim();
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='margin:0 14px 8px 14px;'>");
        if (!head.isEmpty()) {
            sb.append(plainBlock(head));
        }
        sb.append("<div style='background:").append(CLR_ROLE_BG_TOOL)
                .append("; border-left:3px solid #b08800; padding:6px 10px; "
                        + "margin:6px 0; border-radius:0 4px 4px 0;'>");
        sb.append("<div style='font-weight:bold; color:#b08800; font-size:11px; "
                + "margin-bottom:4px;'>TOOL CALLS</div>");
        sb.append("<pre style='white-space:pre-wrap; word-break:break-word; "
                        + "margin:0; font-family:Consolas,\"Courier New\",monospace; "
                        + "font-size:12px;'>")
                .append(highlightToolCalls(escape(tail)))
                .append("</pre>");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 普通文本块（带轻盒）。
     */
    private static String plainBlock(String text) {
        return "<pre style='white-space:pre-wrap; word-break:break-word; "
                + "background:#f6f8fa; border:1px solid #e1e4e8; "
                + "border-radius:4px; padding:8px 10px; margin:6px 0; "
                + "font-family:Consolas,\"Courier New\",monospace; font-size:12px;'>"
                + highlightInlineCode(escape(text))
                + "</pre>";
    }

    /**
     * 在已转义文本中给三反引号代码块、行内反引号、{...} JSON 简单染色。
     * 这里只做轻量增强（避免破坏整体 pre 结构）。
     */
    private static String highlightInlineCode(String escapedText) {
        if (escapedText == null || escapedText.isEmpty()) {
            return "";
        }
        // 行内反引号：`code`
        String s = escapedText.replaceAll(
                "`([^`\\n]+)`",
                "<span style='background:#fff5b1; color:#24292e; "
                        + "padding:0 3px; border-radius:3px;'>$1</span>");
        return s;
    }

    /**
     * 工具调用块的着色：行 "  - name(args)" 形式
     * name 高亮成绿色，args 用 JSON 风格再高亮一次（best-effort）。
     */
    private static String highlightToolCalls(String escapedText) {
        if (escapedText == null || escapedText.isEmpty()) {
            return "";
        }
        // 匹配 "  - name(args)"，name 是函数名，args 可能是 JSON
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "(^|\\n)\\s*-\\s+([A-Za-z_][A-Za-z0-9_]*)\\(([^\\n]*)\\)");
        java.util.regex.Matcher m = p.matcher(escapedText);
        StringBuilder out = new StringBuilder(escapedText.length() + 256);
        int last = 0;
        while (m.find()) {
            out.append(escapedText, last, m.start());
            out.append(m.group(1)); // 前导换行/起始
            out.append("  <span style='color:#586069;'>-</span> ");
            out.append("<span style='color:#0366d6; font-weight:bold;'>")
                    .append(m.group(2)).append("</span>");
            out.append("<span style='color:#586069;'>(</span>");
            out.append("<span style='color:#22863a;'>")
                    .append(m.group(3)).append("</span>");
            out.append("<span style='color:#586069;'>)</span>");
            last = m.end();
        }
        out.append(escapedText, last, escapedText.length());
        return out.toString();
    }

    /* ============================================================================
     * 工具方法
     * ============================================================================ */

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
