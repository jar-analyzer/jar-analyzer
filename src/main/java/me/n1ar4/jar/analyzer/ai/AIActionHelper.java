/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.parser.JarAnalyzerParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 业务动作工具集。
 * <p>
 * 把"反编译解释 / 调用链研判 / DIFF 总结 / SpEL 生成"等业务 prompt 集中在此，
 * 避免 prompt 字符串散落在 GUI 各处。所有方法都做了：
 * - 启用检查（未配置则提示用户去 AI 设置）
 * - 长度截断（防止 token 爆炸）
 * - 一键打开 AIChatDialog 预填 prompt（流式输出由对话窗口接管）
 */
public final class AIActionHelper {
    private static final Logger logger = LogManager.getLogger();

    /** 单段代码 prompt 的最大字符数（约对应 8~16K token，覆盖大多数反编译方法） */
    private static final int MAX_CODE_CHARS = 32000;
    /** DIFF 文本最大字符数（覆盖较大的 unified diff） */
    private static final int MAX_DIFF_CHARS = 64000;
    /** 调用链中每个节点附带反编译时的最大字符数（单节点） */
    private static final int MAX_PER_METHOD_CHARS = 12000;
    /** 调用链整体 prompt 最大字符数（10 节点 × 12K 仍可控） */
    private static final int MAX_CHAIN_TOTAL_CHARS = 96000;

    private AIActionHelper() {
    }

    /**
     * 在执行任何 AI 动作前调用：检查是否已启用配置；
     * 未启用时弹出引导，确认后自动打开 AI 设置面板。
     *
     * @return true 表示可继续；false 表示用户取消
     */
    public static boolean ensureReady(Component owner) {
        AIConfig cfg = AIConfigManager.load();
        if (cfg.isReady()) {
            return true;
        }
        Component parent = owner != null ? owner : (
                MainForm.getInstance() == null ? null : MainForm.getInstance().getMasterPanel());
        int r = JOptionPane.showConfirmDialog(parent,
                "尚未启用任何 AI 配置。\n是否打开 AI 设置面板？",
                "需要配置 AI",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            AISettingsDialog.open();
        }
        return false;
    }

    /**
     * 安全截断：保留前后两段，中间标注省略，便于模型理解上下文
     */
    static String trimText(String text, int max) {
        if (text == null) {
            return "";
        }
        if (text.length() <= max) {
            return text;
        }
        int half = (max - 32) / 2;
        return text.substring(0, half)
                + "\n\n... [此处省略 " + (text.length() - half * 2) + " 字符] ...\n\n"
                + text.substring(text.length() - half);
    }

    // ============================================================
    //  P0-1 反编译代码解释
    // ============================================================

    /**
     * 让 AI 解释一段反编译代码。
     *
     * @param code      要解释的代码片段（可为整段或选中段）
     * @param className 当前所属类（FQN，可为 null）
     */
    public static void explainCode(String code, String className) {
        explainCode(null, code, className);
    }

    /**
     * 带触发源组件的版本：弹窗 owner 会绑定到 anchor 所在窗口，
     * 避免在多子窗口场景下被反复盖到下层。
     */
    public static void explainCode(Component anchor, String code, String className) {
        if (!ensureReady(anchor)) {
            return;
        }
        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(anchor, "代码为空，无法解释");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("请逐段解释下面这段 Java 反编译代码，包括：\n");
        sb.append("1. 该方法/代码块的功能；\n");
        sb.append("2. 关键 API 调用与外部交互（IO/反射/反序列化/网络/SQL/命令执行 等）；\n");
        sb.append("3. 可能的安全风险或可疑模式；\n");
        sb.append("4. 如有外部输入污染路径，请指出 source 与潜在 sink。\n\n");
        if (className != null && !className.isEmpty()) {
            sb.append("当前类：").append(className).append("\n\n");
        }
        sb.append("```java\n").append(trimText(code, MAX_CODE_CHARS)).append("\n```");
        AIChatDialog.openWithPrompt(anchor, sb.toString());
    }

    // ============================================================
    //  P0-2 DFS 调用链研判（带反编译代码）
    // ============================================================

    /**
     * 调用链节点签名（className.methodName(desc)）拆分结果
     */
    private static class MethodSig {
        final String className;
        final String methodName;
        final String methodDesc;

        MethodSig(String className, String methodName, String methodDesc) {
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }
    }

    /**
     * 解析 "className.methodName(desc)" 格式的签名字符串
     */
    private static MethodSig parseSignature(String signature) {
        if (signature == null || signature.isEmpty()) {
            return null;
        }
        // 找到方法名与描述符分界处："类全名.方法名(描述符)"
        int paren = signature.indexOf('(');
        if (paren < 0) {
            // 兼容无描述符
            int lastDot = signature.lastIndexOf('.');
            if (lastDot < 0) {
                return null;
            }
            return new MethodSig(signature.substring(0, lastDot), signature.substring(lastDot + 1), "");
        }
        String beforeParen = signature.substring(0, paren);
        String desc = signature.substring(paren);
        int lastDot = beforeParen.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        return new MethodSig(beforeParen.substring(0, lastDot),
                beforeParen.substring(lastDot + 1), desc);
    }

    /**
     * 在临时目录中按多级路径查找 .class 文件（兼容 BOOT-INF / WEB-INF 路径）
     * 注意：DFS 链路传入的 className 一定是斜杠形式（com/foo/Bar），无需处理点号
     */
    private static Path locateClassFile(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        String tempPath = className.replace('/', File.separatorChar);
        String[] candidates = new String[]{
                String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath),
                String.format("%s%sBOOT-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath),
                String.format("%s%sWEB-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath)
        };
        for (String c : candidates) {
            Path p = Paths.get(c);
            if (Files.exists(p)) {
                return p;
            }
        }
        return null;
    }

    /**
     * 取出反编译后类源代码中目标方法的源代码片段。
     * 优先使用 JavaParser AST 精确提取；失败时回退到整段类代码（截断）。
     *
     * @return 方法源代码；若 JavaParser 解析失败但有反编译结果，返回整段反编译；都失败返回 null
     */
    private static String extractMethodSource(String classCode, MethodSig sig) {
        if (classCode == null || classCode.isEmpty() || sig == null) {
            return null;
        }
        // desc 为空时无法用 ASM 解析，跳过 AST 直接降级
        if (sig.methodDesc != null && !sig.methodDesc.isEmpty()) {
            try {
                CompilationUnit cu = JarAnalyzerParser.buildInstance(classCode);
                if ("<init>".equals(sig.methodName)) {
                    ConstructorDeclaration cd = JarAnalyzerParser.getConstructor(cu, sig.methodDesc);
                    if (cd != null) {
                        return cd.toString();
                    }
                } else if ("<clinit>".equals(sig.methodName)) {
                    return cu.findFirst(com.github.javaparser.ast.body.InitializerDeclaration.class)
                            .map(Object::toString).orElse(null);
                } else {
                    MethodDeclaration md = JarAnalyzerParser.getMethod(cu, sig.methodName, sig.methodDesc);
                    if (md != null) {
                        return md.toString();
                    }
                }
                // AST 解析成功但找不到目标方法（可能反编译丢失了某些方法）：降级到整段
            } catch (Throwable ex) {
                // JavaParser 解析失败（反编译产物可能不是合法 Java）
                logger.debug("javaparser failed, fallback to whole class: {}", ex.toString());
            }
        }
        // 降级：返回整段反编译类代码 + 提示
        return "// AST 提取失败，下面是整个类的反编译代码（请关注方法 "
                + sig.methodName + "）：\n" + classCode;
    }

    /**
     * 异步研判调用链：自动反编译每个节点，附上代码片段后发给 AI。
     * 全程在后台线程执行，UI 上显示进度对话框，避免卡 EDT。
     * <p>
     * 性能优化：同一个类只反编译一次，从同一份反编译结果中提取多个方法。
     *
     * @param title   链路标题
     * @param methods 链路节点签名列表（className.method(desc)）
     */
    public static void auditChain(String title, List<String> methods) {
        auditChain(null, title, methods);
    }

    /**
     * 带触发源组件的版本：进度框 / AI 对话框都会以 anchor 所在窗口为父，
     * 避免在调用链子窗口里点 AI 后弹窗被反复盖到下层。
     */
    public static void auditChain(Component anchor, String title, List<String> methods) {
        if (!ensureReady(anchor)) {
            return;
        }
        if (methods == null || methods.isEmpty()) {
            JOptionPane.showMessageDialog(anchor, "调用链为空");
            return;
        }
        // 拷贝一份，避免后台线程中被修改
        final List<String> snapshot = new ArrayList<>(methods);
        final String snapshotTitle = title;

        // 进度对话框：以 anchor 所在 Window 为父，保证浮在触发源之上
        final java.awt.Window ownerWin = (anchor == null) ? null
                : SwingUtilities.getWindowAncestor(anchor);
        final java.awt.Window progressOwner = ownerWin != null ? ownerWin
                : (MainForm.getInstance() == null ? null
                        : SwingUtilities.getWindowAncestor(MainForm.getInstance().getMasterPanel()));
        final JDialog progress = new JDialog(progressOwner, "正在反编译调用链…",
                Dialog.ModalityType.MODELESS);
        JLabel msg = new JLabel("准备中…");
        msg.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        progress.add(msg);
        progress.pack();
        progress.setLocationRelativeTo(progressOwner);
        progress.setVisible(true);
        progress.toFront();

        new Thread(() -> {
            try {
                // 同一个类的反编译结果只做一次
                java.util.Map<String, String> classCache = new java.util.HashMap<>();
                List<String> sources = new ArrayList<>(snapshot.size());
                for (int i = 0; i < snapshot.size(); i++) {
                    final int idx = i;
                    SwingUtilities.invokeLater(() -> msg.setText(
                            String.format("反编译节点 %d / %d …", idx + 1, snapshot.size())));
                    String sig = snapshot.get(i);
                    String src = decompileNodeSource(sig, classCache);
                    sources.add(src);
                }
                String prompt = buildChainPrompt(snapshotTitle, snapshot, sources);
                SwingUtilities.invokeLater(() -> {
                    progress.dispose();
                    AIChatDialog.openWithPrompt(anchor, prompt);
                });
            } catch (Throwable ex) {
                logger.error("auditChain prepare failed: {}", ex.toString());
                SwingUtilities.invokeLater(() -> {
                    progress.dispose();
                    JOptionPane.showMessageDialog(anchor,
                            "准备调用链上下文失败：" + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "ai-chain-prepare").start();
    }

    /**
     * 反编译某个调用链节点对应的方法源代码；找不到时返回 null
     * 通过 cache 避免对同一个类多次反编译
     */
    private static String decompileNodeSource(String signature, java.util.Map<String, String> cache) {
        MethodSig sig = parseSignature(signature);
        if (sig == null) {
            return null;
        }
        // JDK 内置类（如 java/lang/Runtime）通常不在临时目录，直接跳过
        if (sig.className.startsWith("java/") || sig.className.startsWith("javax/")
                || sig.className.startsWith("sun/") || sig.className.startsWith("jdk/")) {
            return null;
        }
        Path classFile = locateClassFile(sig.className);
        if (classFile == null) {
            return null;
        }
        try {
            String key = classFile.toAbsolutePath().toString();
            String classCode = cache.get(key);
            if (classCode == null) {
                classCode = DecompileEngine.decompile(classFile);
                cache.put(key, classCode == null ? "" : classCode);
            }
            if (classCode.isEmpty()) {
                return null;
            }
            return extractMethodSource(classCode, sig);
        } catch (Throwable ex) {
            logger.debug("decompile node failed: {}", ex.toString());
            return null;
        }
    }

    /**
     * 构造带反编译代码的链路 prompt
     */
    private static String buildChainPrompt(String title, List<String> methods, List<String> sources) {
        StringBuilder sb = new StringBuilder();
        sb.append("下面是一条 Java 调用链（DFS 静态分析得到），每个节点都附带了反编译后的源代码。请基于代码内容判断：\n");
        sb.append("1. 该链是否真实可达（关注每个节点中的条件分支、异常处理、null 检查、参数过滤）；\n");
        sb.append("2. 入口 source 是否可由外部用户直接或间接控制（HTTP 参数 / 反序列化输入 / 配置 等）；\n");
        sb.append("3. sink 处的危害性，可能被利用的方式；\n");
        sb.append("4. 若可利用，简要描述构造 PoC 的关键参数路径与前提条件；\n");
        sb.append("5. 给出明确结论：高/中/低风险，并说明判断依据。\n\n");
        if (title != null && !title.isEmpty()) {
            sb.append("链路标题：").append(title).append("\n\n");
        }
        sb.append("调用链概览（自顶向下）：\n```\n");
        for (int i = 0; i < methods.size(); i++) {
            sb.append(i == 0 ? "" : "  -> ").append(methods.get(i)).append('\n');
        }
        sb.append("```\n\n");
        sb.append("各节点反编译代码：\n");
        for (int i = 0; i < methods.size(); i++) {
            sb.append("\n### 节点 ").append(i + 1).append(": ").append(methods.get(i)).append('\n');
            String src = sources.get(i);
            if (src == null || src.isEmpty()) {
                // 通常是 JDK 内置类（java/javax/sun/jdk）或外部依赖，不在工作集中
                sb.append("> （JDK 或外部依赖类，仅根据签名分析）\n");
            } else {
                sb.append("```java\n").append(trimText(src, MAX_PER_METHOD_CHARS)).append("\n```\n");
            }
        }
        // 整体最大长度兜底
        return trimText(sb.toString(), MAX_CHAIN_TOTAL_CHARS);
    }

    // ============================================================
    //  P0-3 JAR DIFF 安全摘要
    // ============================================================

    /**
     * 让 AI 总结一次 JAR DIFF 的内容是否包含安全修复。
     *
     * @param leftLabel  左侧 JAR 标签（旧版本）
     * @param rightLabel 右侧 JAR 标签（新版本）
     * @param diffText   diff 报告文本（unified diff 或导出的完整记录）
     */
    public static void summarizeDiff(String leftLabel, String rightLabel, String diffText) {
        summarizeDiff(null, leftLabel, rightLabel, diffText);
    }

    /**
     * 带触发源组件的版本：弹窗 owner 会绑定到 anchor 所在窗口（通常是 JarDiffForm），
     * 避免 AI 对话框被反复盖到 diff 子窗口下层。
     */
    public static void summarizeDiff(Component anchor, String leftLabel,
                                     String rightLabel, String diffText) {
        if (!ensureReady(anchor)) {
            return;
        }
        if (diffText == null || diffText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(anchor, "DIFF 内容为空，请先选择条目或导出 DIFF 报告");
            return;
        }

        // 压缩策略：原始 diff 超过上限时，只保留 +/- 改动行（含 hunk 头），剔除 unchanged 上下文
        String body;
        boolean compressed = false;
        if (diffText.length() <= MAX_DIFF_CHARS) {
            body = diffText;
        } else {
            String compact = compressDiff(diffText);
            if (compact.length() <= MAX_DIFF_CHARS) {
                body = compact;
                compressed = true;
            } else {
                // 仍然太大：再做首尾截断
                body = trimText(compact, MAX_DIFF_CHARS);
                compressed = true;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("下面是两个 JAR 包的差异内容，请分析：\n");
        sb.append("1. 这次变更是否包含明显的安全修复（CVE 修复、输入校验、权限检查、危险 API 替换 等）？\n");
        sb.append("2. 若是安全修复，可能修复了什么类型的漏洞（RCE / 反序列化 / SQL 注入 / 越权 / 信息泄漏 等）？\n");
        sb.append("3. 列出最值得关注的 3-5 个变更，并解释判断依据；\n");
        sb.append("4. 是否存在引入新风险的可能。\n\n");
        if (leftLabel != null) {
            sb.append("旧版本：").append(leftLabel).append("\n");
        }
        if (rightLabel != null) {
            sb.append("新版本：").append(rightLabel).append("\n");
        }
        if (compressed) {
            sb.append("\n> 注意：原始 diff 过大，已压缩为仅保留 +/- 改动行。\n");
        }
        sb.append("\n```diff\n").append(body).append("\n```");
        AIChatDialog.openWithPrompt(anchor, sb.toString());
    }

    /**
     * 压缩 unified diff：仅保留 +/- 改动行 + hunk 头 (`@@`) + 文件头 (`---` `+++`)，
     * 丢弃 unchanged 上下文行（` ` 开头）。
     * 适用于巨大 diff 文件传给 AI 时减少 token 占用。
     */
    static String compressDiff(String diff) {
        if (diff == null || diff.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(diff.length() / 2);
        int kept = 0;
        int dropped = 0;
        for (String line : diff.split("\\r?\\n", -1)) {
            if (line.isEmpty()) {
                continue;
            }
            char c = line.charAt(0);
            // 保留：文件头、hunk 头、变更行
            if (c == '+' || c == '-' || c == '@'
                    || line.startsWith("===")
                    || line.startsWith("Index:")
                    || line.startsWith("diff ")) {
                out.append(line).append('\n');
                kept++;
            } else {
                // 上下文（空格开头）/ 其他装饰：跳过
                dropped++;
            }
        }
        if (dropped > 0) {
            out.append("\n# [省略 ").append(dropped).append(" 行未变更上下文，保留 ")
                    .append(kept).append(" 行改动]\n");
        }
        return out.toString();
    }
}
