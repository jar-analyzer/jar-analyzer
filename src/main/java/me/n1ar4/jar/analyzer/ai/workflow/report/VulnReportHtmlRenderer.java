/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.report;

import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 将漏洞报告列表渲染成一个完整、自包含的 HTML 报告。
 * <p>
 * 设计约束：
 * <ul>
 *   <li>不引入任何第三方依赖（无 CDN / 无外链），CSS 与 JS 全部内嵌在单个 HTML 中</li>
 *   <li>所有来自 AI / 数据层的文本都经过 HTML 转义，避免 XSS</li>
 *   <li>提供概览统计、类型分布、风险等级、搜索 / 筛选 / 折叠等交互</li>
 * </ul>
 */
public final class VulnReportHtmlRenderer {

    private static final Logger logger = LogManager.getLogger();

    private static final String REPORT_TITLE = "jar-analyzer ai workflow 报告";

    private VulnReportHtmlRenderer() {
    }

    /**
     * 漏洞类型 -> 中文名（用于更友好的展示）。
     */
    private static final Map<String, String> TYPE_CN = new LinkedHashMap<>();

    static {
        TYPE_CN.put("deserialize", "反序列化");
        TYPE_CN.put("file_path_traversal", "文件路径穿越");
        TYPE_CN.put("redirect", "URL 重定向");
        TYPE_CN.put("ssrf", "SSRF");
        TYPE_CN.put("sql_injection", "SQL 注入");
        TYPE_CN.put("template_injection", "模板注入");
        TYPE_CN.put("arbitrary_file_download", "任意文件下载");
        TYPE_CN.put("arbitrary_file_upload", "任意文件上传");
        TYPE_CN.put("code_injection", "代码注入");
        TYPE_CN.put("arbitrary_spring_bean_call", "任意 Spring Bean 调用");
        TYPE_CN.put("xss", "XSS");
        TYPE_CN.put("command_injection", "命令注入");
        TYPE_CN.put("other", "其他");
    }

    /**
     * 生成 HTML 文件到 ${tempDir}/reports/ 目录，返回生成文件的绝对路径；失败返回 null。
     */
    public static String renderToFile(List<VulnReport> reports) {
        try {
            Path baseDir = Paths.get(Const.tempDir).resolve("reports").normalize().toAbsolutePath();
            Files.createDirectories(baseDir);
            String ts = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
            String fname = "report-" + ts + ".html";
            if (!fname.matches("[a-zA-Z0-9.\\-]+")) {
                throw new RuntimeException("invalid file name produced");
            }
            Path target = baseDir.resolve(fname).normalize().toAbsolutePath();
            if (!target.startsWith(baseDir)) {
                throw new RuntimeException("path traversal detected");
            }
            String html = render(reports);
            Files.write(target, html.getBytes(StandardCharsets.UTF_8));
            logger.info("html report generated: {}", target);
            return target.toString();
        } catch (Throwable t) {
            logger.error("render html report error: {}", t.toString());
            return null;
        }
    }

    /**
     * 渲染完整 HTML 字符串。
     */
    public static String render(List<VulnReport> reports) {
        if (reports == null) {
            reports = Collections.emptyList();
        }
        // 统计
        int total = reports.size();
        int high = 0, mid = 0, low = 0;
        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (VulnReport r : reports) {
            int s = r.getScore();
            if (s >= 8) {
                high++;
            } else if (s >= 4) {
                mid++;
            } else {
                low++;
            }
            String t = r.getType() == null ? "other" : r.getType();
            Integer c = typeCount.get(t);
            typeCount.put(t, c == null ? 1 : c + 1);
        }

        String genTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date());

        StringBuilder sb = new StringBuilder(8192);
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"zh-CN\">\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<title>").append(escape(REPORT_TITLE)).append("</title>\n");
        sb.append("<style>\n").append(css()).append("\n</style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");

        // ===== Header =====
        sb.append("<header class=\"top\">\n");
        sb.append("  <div class=\"top-inner\">\n");
        sb.append("    <div class=\"brand\">\n");
        sb.append("      <div class=\"logo\">JA</div>\n");
        sb.append("      <div>\n");
        sb.append("        <h1>").append(escape(REPORT_TITLE)).append("</h1>\n");
        sb.append("        <div class=\"subtitle\">AI 漏洞扫描工作流 · 自动生成报告</div>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"gen-time\">生成时间：").append(escape(genTime)).append("</div>\n");
        sb.append("  </div>\n");
        sb.append("</header>\n");

        sb.append("<main class=\"container\">\n");

        // ===== 概览卡片 =====
        sb.append("<section class=\"cards\">\n");
        sb.append(statCard("报告总数", String.valueOf(total), "c-total"));
        sb.append(statCard("高危 (≥8)", String.valueOf(high), "c-high"));
        sb.append(statCard("中危 (4-7)", String.valueOf(mid), "c-mid"));
        sb.append(statCard("低危 (≤3)", String.valueOf(low), "c-low"));
        sb.append("</section>\n");

        // ===== 类型分布 =====
        if (!typeCount.isEmpty()) {
            int maxC = 0;
            for (Integer v : typeCount.values()) {
                if (v != null && v > maxC) {
                    maxC = v;
                }
            }
            sb.append("<section class=\"panel\">\n");
            sb.append("  <h2 class=\"panel-title\">漏洞类型分布</h2>\n");
            sb.append("  <div class=\"dist\">\n");
            for (Map.Entry<String, Integer> e : typeCount.entrySet()) {
                int v = e.getValue() == null ? 0 : e.getValue();
                int pct = maxC == 0 ? 0 : (int) Math.round(v * 100.0 / maxC);
                sb.append("    <div class=\"dist-row\">\n");
                sb.append("      <div class=\"dist-name\">").append(escape(typeName(e.getKey()))).append("</div>\n");
                sb.append("      <div class=\"dist-bar\"><div class=\"dist-fill\" style=\"width:")
                        .append(pct).append("%\"></div></div>\n");
                sb.append("      <div class=\"dist-val\">").append(v).append("</div>\n");
                sb.append("    </div>\n");
            }
            sb.append("  </div>\n");
            sb.append("</section>\n");
        }

        // ===== 工具栏（搜索 / 筛选） =====
        sb.append("<section class=\"toolbar\">\n");
        sb.append("  <input id=\"searchBox\" type=\"text\" placeholder=\"搜索类型 / 依据 / 类名 / 方法...\" oninput=\"applyFilter()\">\n");
        sb.append("  <div class=\"filters\">\n");
        sb.append("    <button class=\"chip active\" data-level=\"all\" onclick=\"setLevel(this,'all')\">全部</button>\n");
        sb.append("    <button class=\"chip\" data-level=\"high\" onclick=\"setLevel(this,'high')\">高危</button>\n");
        sb.append("    <button class=\"chip\" data-level=\"mid\" onclick=\"setLevel(this,'mid')\">中危</button>\n");
        sb.append("    <button class=\"chip\" data-level=\"low\" onclick=\"setLevel(this,'low')\">低危</button>\n");
        sb.append("  </div>\n");
        sb.append("</section>\n");

        // ===== 报告列表 =====
        sb.append("<section id=\"reportList\">\n");
        if (reports.isEmpty()) {
            sb.append("  <div class=\"empty\">暂无漏洞报告。运行 AI 漏洞扫描工作流后，结果将展示在此处。</div>\n");
        } else {
            // 已按时间倒序（来自 loadAll），保持顺序
            int idx = 0;
            for (VulnReport r : reports) {
                sb.append(renderReportItem(r, idx++));
            }
        }
        sb.append("</section>\n");

        sb.append("</main>\n");

        // ===== Footer =====
        sb.append("<footer class=\"foot\">\n");
        sb.append("  <div>Generated by <strong>jar-analyzer</strong> AI Workflow · ")
                .append(escape(genTime)).append("</div>\n");
        sb.append("  <div><a href=\"https://github.com/jar-analyzer/jar-analyzer\" target=\"_blank\" rel=\"noopener\">github.com/jar-analyzer/jar-analyzer</a></div>\n");
        sb.append("</footer>\n");

        sb.append("<script>\n").append(js()).append("\n</script>\n");
        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private static String statCard(String label, String value, String cls) {
        return "  <div class=\"card " + cls + "\">\n"
                + "    <div class=\"card-val\">" + escape(value) + "</div>\n"
                + "    <div class=\"card-label\">" + escape(label) + "</div>\n"
                + "  </div>\n";
    }

    private static String renderReportItem(VulnReport r, int idx) {
        int score = r.getScore();
        String level = score >= 8 ? "high" : (score >= 4 ? "mid" : "low");
        String levelText = score >= 8 ? "高危" : (score >= 4 ? "中危" : "低危");
        String type = r.getType() == null ? "other" : r.getType();
        String reason = r.getReason() == null ? "" : r.getReason();
        String title = r.getTitle() == null ? "" : r.getTitle().trim();
        String attack = r.getAttackVector() == null ? "" : r.getAttackVector();
        String poc = r.getPoc() == null ? "" : r.getPoc();
        // 兜底标题：当模型未返回 title 时退化使用类型名
        String displayTitle = title.isEmpty() ? typeName(type) : title;
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                .format(new Date(r.getTimestamp()));

        // 用于 JS 搜索的纯文本（小写）
        StringBuilder search = new StringBuilder();
        search.append(type).append(' ').append(typeName(type)).append(' ')
                .append(displayTitle).append(' ').append(reason).append(' ')
                .append(attack).append(' ').append(poc);
        if (r.getTrace() != null) {
            for (VulnTrace t : r.getTrace()) {
                if (t == null) {
                    continue;
                }
                search.append(' ').append(nv(t.getClazz()))
                        .append(' ').append(nv(t.getMethod()))
                        .append(' ').append(nv(t.getDesc()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("  <article class=\"report\" data-level=\"").append(level)
                .append("\" data-search=\"").append(escapeAttr(search.toString().toLowerCase(Locale.ROOT)))
                .append("\">\n");

        // header（可点击折叠）
        sb.append("    <div class=\"report-head\" onclick=\"toggleReport(this)\">\n");
        sb.append("      <span class=\"badge badge-").append(level).append("\">").append(levelText)
                .append(" · ").append(score).append("/10</span>\n");
        sb.append("      <span class=\"r-title\">").append(escape(displayTitle)).append("</span>\n");
        sb.append("      <span class=\"r-type-tag\">").append(escape(typeName(type))).append("</span>\n");
        sb.append("      <span class=\"r-type-raw\">").append(escape(type)).append("</span>\n");
        sb.append("      <span class=\"r-time\">").append(escape(timeStr)).append("</span>\n");
        sb.append("      <span class=\"toggle-ico\">▾</span>\n");
        sb.append("    </div>\n");

        // body
        sb.append("    <div class=\"report-body\">\n");

        // score 进度
        sb.append("      <div class=\"score-line\">\n");
        sb.append("        <span class=\"score-label\">风险评分</span>\n");
        sb.append("        <div class=\"score-bar\"><div class=\"score-fill sf-").append(level)
                .append("\" style=\"width:").append(Math.max(0, Math.min(100, score * 10)))
                .append("%\"></div></div>\n");
        sb.append("        <span class=\"score-num\">").append(score).append(" / 10</span>\n");
        sb.append("      </div>\n");

        // reason
        sb.append("      <div class=\"block\">\n");
        sb.append("        <div class=\"block-title\">判断依据</div>\n");
        sb.append("        <div class=\"reason\">").append(escapeMultiline(reason)).append("</div>\n");
        sb.append("      </div>\n");

        // 攻击方式
        if (!attack.isEmpty()) {
            sb.append("      <div class=\"block\">\n");
            sb.append("        <div class=\"block-title\">攻击方式</div>\n");
            sb.append("        <div class=\"attack\">").append(escapeMultiline(attack)).append("</div>\n");
            sb.append("      </div>\n");
        }

        // 推断 PoC（含 RAW HTTP，使用 <pre> 保留格式）
        if (!poc.isEmpty()) {
            sb.append("      <div class=\"block\">\n");
            sb.append("        <div class=\"block-title\">推断 PoC（含 RAW HTTP）</div>\n");
            sb.append("        <pre class=\"poc\"><code>").append(escape(poc)).append("</code></pre>\n");
            sb.append("      </div>\n");
        }

        // trace
        List<VulnTrace> trace = r.getTrace();
        sb.append("      <div class=\"block\">\n");
        sb.append("        <div class=\"block-title\">调用链 (").append(trace == null ? 0 : trace.size()).append(")</div>\n");
        if (trace == null || trace.isEmpty()) {
            sb.append("        <div class=\"trace-empty\">无调用链信息</div>\n");
        } else {
            sb.append("        <ol class=\"trace\">\n");
            for (VulnTrace t : trace) {
                if (t == null) {
                    continue;
                }
                sb.append("          <li>\n");
                sb.append("            <span class=\"t-clazz\">").append(escape(nv(t.getClazz()))).append("</span>\n");
                sb.append("            <span class=\"t-method\">").append(escape(nv(t.getMethod()))).append("</span>\n");
                sb.append("            <span class=\"t-desc\">").append(escape(nv(t.getDesc()))).append("</span>\n");
                sb.append("          </li>\n");
            }
            sb.append("        </ol>\n");
        }
        sb.append("      </div>\n");

        sb.append("    </div>\n");
        sb.append("  </article>\n");
        return sb.toString();
    }

    private static String typeName(String type) {
        if (type == null) {
            return "其他";
        }
        String cn = TYPE_CN.get(type);
        return cn == null ? type : cn;
    }

    private static String nv(String s) {
        return s == null ? "" : s;
    }

    // =============== HTML 转义 ===============

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    private static String escapeAttr(String s) {
        return escape(s);
    }

    /**
     * 多行文本转义并保留换行。
     */
    private static String escapeMultiline(String s) {
        return escape(s).replace("\r\n", "\n").replace("\n", "<br>");
    }

    // =============== 内嵌资源 ===============

    private static String css() {
        return "" +
                ":root{--bg:#f5f7fa;--panel:#ffffff;--text:#1f2933;--muted:#7b8794;" +
                "--border:#e4e7eb;--primary:#2563eb;--high:#dc2626;--mid:#d97706;--low:#16a34a;}" +
                "*{box-sizing:border-box;}" +
                "body{margin:0;font-family:'Segoe UI','Microsoft YaHei',Helvetica,Arial,sans-serif;" +
                "background:var(--bg);color:var(--text);line-height:1.6;}" +
                ".top{background:linear-gradient(135deg,#1e3a8a,#2563eb);color:#fff;padding:24px 0;" +
                "box-shadow:0 2px 12px rgba(0,0,0,.12);}" +
                ".top-inner{max-width:1080px;margin:0 auto;padding:0 24px;display:flex;" +
                "align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;}" +
                ".brand{display:flex;align-items:center;gap:16px;}" +
                ".logo{width:48px;height:48px;border-radius:12px;background:rgba(255,255,255,.18);" +
                "display:flex;align-items:center;justify-content:center;font-weight:700;font-size:20px;" +
                "letter-spacing:1px;}" +
                ".top h1{margin:0;font-size:22px;font-weight:700;}" +
                ".subtitle{font-size:13px;opacity:.85;margin-top:2px;}" +
                ".gen-time{font-size:13px;opacity:.9;}" +
                ".container{max-width:1080px;margin:0 auto;padding:24px;}" +
                ".cards{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:24px;}" +
                ".card{background:var(--panel);border:1px solid var(--border);border-radius:12px;" +
                "padding:18px 20px;box-shadow:0 1px 3px rgba(0,0,0,.04);}" +
                ".card-val{font-size:30px;font-weight:700;line-height:1.1;}" +
                ".card-label{font-size:13px;color:var(--muted);margin-top:6px;}" +
                ".c-total .card-val{color:var(--primary);}" +
                ".c-high .card-val{color:var(--high);}" +
                ".c-mid .card-val{color:var(--mid);}" +
                ".c-low .card-val{color:var(--low);}" +
                ".panel{background:var(--panel);border:1px solid var(--border);border-radius:12px;" +
                "padding:20px;margin-bottom:24px;box-shadow:0 1px 3px rgba(0,0,0,.04);}" +
                ".panel-title{margin:0 0 16px;font-size:16px;}" +
                ".dist-row{display:flex;align-items:center;gap:12px;margin-bottom:10px;}" +
                ".dist-name{width:160px;font-size:13px;color:var(--text);flex-shrink:0;}" +
                ".dist-bar{flex:1;height:14px;background:#eef1f5;border-radius:7px;overflow:hidden;}" +
                ".dist-fill{height:100%;background:linear-gradient(90deg,#3b82f6,#2563eb);border-radius:7px;}" +
                ".dist-val{width:36px;text-align:right;font-size:13px;font-weight:600;color:var(--muted);}" +
                ".toolbar{display:flex;align-items:center;justify-content:space-between;gap:16px;" +
                "margin-bottom:16px;flex-wrap:wrap;}" +
                "#searchBox{flex:1;min-width:220px;padding:10px 14px;border:1px solid var(--border);" +
                "border-radius:10px;font-size:14px;outline:none;background:#fff;}" +
                "#searchBox:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.12);}" +
                ".filters{display:flex;gap:8px;}" +
                ".chip{border:1px solid var(--border);background:#fff;border-radius:20px;padding:7px 16px;" +
                "font-size:13px;cursor:pointer;color:var(--text);transition:all .15s;}" +
                ".chip:hover{border-color:var(--primary);color:var(--primary);}" +
                ".chip.active{background:var(--primary);border-color:var(--primary);color:#fff;}" +
                ".report{background:var(--panel);border:1px solid var(--border);border-radius:12px;" +
                "margin-bottom:14px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.04);}" +
                ".report-head{display:flex;align-items:center;gap:14px;padding:14px 18px;cursor:pointer;" +
                "user-select:none;}" +
                ".report-head:hover{background:#fafbfc;}" +
                ".badge{font-size:12px;font-weight:700;color:#fff;padding:4px 10px;border-radius:6px;" +
                "white-space:nowrap;}" +
                ".badge-high{background:var(--high);}.badge-mid{background:var(--mid);}" +
                ".badge-low{background:var(--low);}" +
                ".r-title{font-size:15px;font-weight:600;color:var(--text);" +
                "white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:520px;}" +
                ".r-type-tag{font-size:12px;color:var(--primary);background:#eef4ff;border:1px solid #dbe6ff;" +
                "padding:2px 8px;border-radius:10px;white-space:nowrap;}" +
                ".r-type-raw{font-size:12px;color:var(--muted);font-family:Consolas,monospace;}" +
                ".r-time{margin-left:auto;font-size:12px;color:var(--muted);}" +
                ".toggle-ico{transition:transform .2s;color:var(--muted);font-size:14px;}" +
                ".report.collapsed .toggle-ico{transform:rotate(-90deg);}" +
                ".report.collapsed .report-body{display:none;}" +
                ".report-body{padding:6px 18px 18px;border-top:1px solid var(--border);}" +
                ".score-line{display:flex;align-items:center;gap:12px;margin:14px 0 18px;}" +
                ".score-label{font-size:13px;color:var(--muted);width:62px;flex-shrink:0;}" +
                ".score-bar{flex:1;height:10px;background:#eef1f5;border-radius:5px;overflow:hidden;}" +
                ".score-fill{height:100%;border-radius:5px;}" +
                ".sf-high{background:var(--high);}.sf-mid{background:var(--mid);}.sf-low{background:var(--low);}" +
                ".score-num{font-size:13px;font-weight:600;width:54px;text-align:right;}" +
                ".block{margin-bottom:16px;}" +
                ".block-title{font-size:13px;font-weight:700;color:var(--muted);margin-bottom:8px;" +
                "text-transform:uppercase;letter-spacing:.5px;}" +
                ".reason{background:#f8fafc;border:1px solid var(--border);border-radius:8px;padding:12px 14px;" +
                "font-size:14px;white-space:normal;word-break:break-word;}" +
                ".attack{background:#fff7ed;border:1px solid #fed7aa;border-radius:8px;padding:12px 14px;" +
                "font-size:14px;color:#7c2d12;white-space:normal;word-break:break-word;}" +
                ".poc{margin:0;background:#0f172a;color:#e2e8f0;border-radius:8px;padding:14px 16px;" +
                "font-family:Consolas,Menlo,'Courier New',monospace;font-size:13px;line-height:1.55;" +
                "white-space:pre-wrap;word-break:break-word;overflow-x:auto;border:1px solid #1e293b;}" +
                ".poc code{font-family:inherit;color:inherit;background:transparent;padding:0;}" +
                ".trace{margin:0;padding-left:0;list-style:none;counter-reset:step;}" +
                ".trace li{position:relative;padding:10px 12px 10px 40px;border:1px solid var(--border);" +
                "border-radius:8px;margin-bottom:8px;background:#fbfcfd;}" +
                ".trace li:before{counter-increment:step;content:counter(step);position:absolute;left:10px;" +
                "top:50%;transform:translateY(-50%);width:22px;height:22px;border-radius:50%;" +
                "background:var(--primary);color:#fff;font-size:12px;display:flex;align-items:center;" +
                "justify-content:center;font-weight:700;}" +
                ".t-clazz{font-family:Consolas,monospace;font-size:13px;font-weight:600;color:#1d4ed8;}" +
                ".t-method{font-family:Consolas,monospace;font-size:13px;color:var(--text);margin-left:6px;}" +
                ".t-desc{font-family:Consolas,monospace;font-size:12px;color:var(--muted);margin-left:6px;" +
                "word-break:break-all;}" +
                ".trace-empty,.empty{color:var(--muted);font-size:14px;padding:14px;text-align:center;}" +
                ".empty{background:var(--panel);border:1px dashed var(--border);border-radius:12px;padding:40px;}" +
                ".foot{max-width:1080px;margin:8px auto 32px;padding:16px 24px;display:flex;" +
                "justify-content:space-between;flex-wrap:wrap;gap:8px;font-size:12px;color:var(--muted);}" +
                ".foot a{color:var(--primary);text-decoration:none;}" +
                ".foot a:hover{text-decoration:underline;}" +
                "@media(max-width:760px){.cards{grid-template-columns:repeat(2,1fr);}" +
                ".dist-name{width:100px;}.r-time{display:none;}}";
    }

    private static String js() {
        return "" +
                "var curLevel='all';\n" +
                "function setLevel(btn,level){\n" +
                "  curLevel=level;\n" +
                "  var chips=document.querySelectorAll('.chip');\n" +
                "  for(var i=0;i<chips.length;i++){chips[i].classList.remove('active');}\n" +
                "  btn.classList.add('active');\n" +
                "  applyFilter();\n" +
                "}\n" +
                "function applyFilter(){\n" +
                "  var kw=(document.getElementById('searchBox').value||'').toLowerCase().trim();\n" +
                "  var items=document.querySelectorAll('.report');\n" +
                "  var shown=0;\n" +
                "  for(var i=0;i<items.length;i++){\n" +
                "    var el=items[i];\n" +
                "    var okLevel=(curLevel==='all')||(el.getAttribute('data-level')===curLevel);\n" +
                "    var okKw=(kw==='')||(el.getAttribute('data-search').indexOf(kw)>=0);\n" +
                "    var visible=okLevel&&okKw;\n" +
                "    el.style.display=visible?'':'none';\n" +
                "    if(visible){shown++;}\n" +
                "  }\n" +
                "  var empty=document.getElementById('noResult');\n" +
                "  if(shown===0){\n" +
                "    if(!empty){\n" +
                "      empty=document.createElement('div');\n" +
                "      empty.id='noResult';empty.className='empty';\n" +
                "      empty.textContent='没有匹配的报告';\n" +
                "      document.getElementById('reportList').appendChild(empty);\n" +
                "    }\n" +
                "  }else if(empty){empty.parentNode.removeChild(empty);}\n" +
                "}\n" +
                "function toggleReport(head){\n" +
                "  head.parentNode.classList.toggle('collapsed');\n" +
                "}\n";
    }
}
