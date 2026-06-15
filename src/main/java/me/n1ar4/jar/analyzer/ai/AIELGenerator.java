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

import me.n1ar4.jar.analyzer.el.Templates;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EL（SpEL）表达式 AI 生成器。
 * <p>
 * 把 jar-analyzer 的 EL DSL 规则与全部内置模板示例喂给模型，
 * 让模型只输出一个 SpEL 表达式（用 &lt;EL&gt;...&lt;/EL&gt; 包裹）。
 * <p>
 * 复用全局 {@link AIConfigManager#getActive()} 配置，
 * 不引入额外配置项 / API Key。
 */
public final class AIELGenerator {
    private static final Logger logger = LogManager.getLogger();

    /**
     * 单次生成的硬上限，避免极端情况下耗光 token
     */
    private static final int MAX_USER_INPUT = 4000;

    /**
     * 抽取标签：&lt;EL&gt;...&lt;/EL&gt;（DOTALL）
     */
    private static final Pattern TAG_RE =
            Pattern.compile("<\\s*EL\\s*>(.*?)<\\s*/\\s*EL\\s*>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 备用：```spel / ```java / ``` 代码块
     */
    private static final Pattern FENCE_RE =
            Pattern.compile("```(?:spel|java|el)?\\s*\\n([\\s\\S]*?)```",
                    Pattern.CASE_INSENSITIVE);

    private AIELGenerator() {
    }

    /**
     * 生成结果（成功或失败）
     */
    public static final class Result {
        public final boolean ok;
        public final String expression;   // 提取到的纯表达式（可直接 setText）
        public final String rawResponse;  // 模型原始回复（调试 / 显示）
        public final String error;        // 失败原因

        private Result(boolean ok, String expression, String raw, String error) {
            this.ok = ok;
            this.expression = expression;
            this.rawResponse = raw;
            this.error = error;
        }

        static Result success(String expr, String raw) {
            return new Result(true, expr, raw, null);
        }

        static Result failure(String err, String raw) {
            return new Result(false, null, raw, err);
        }
    }

    /**
     * 在调用线程同步生成（建议放在后台 Thread 中调用，UI 线程仅做交互）。
     *
     * @param userIntent 用户用自然语言描述需求
     * @return 结构化结果，永不抛出
     */
    public static Result generate(String userIntent) {
        if (userIntent == null || userIntent.trim().isEmpty()) {
            return Result.failure("请输入需求描述", null);
        }
        AIConfig cfg = AIConfigManager.load();
        if (!cfg.isReady()) {
            return Result.failure("尚未启用 AI 配置，请先在 AI 设置中完成配置", null);
        }
        // 安全：截断超长输入，避免向模型发送巨量数据
        String trimmedIntent = userIntent.length() > MAX_USER_INPUT
                ? userIntent.substring(0, MAX_USER_INPUT)
                : userIntent;

        String systemPrompt = buildSystemPrompt();
        List<LLMClient.ChatMessage> msgs =
                LLMClient.singleTurn(systemPrompt, buildUserPrompt(trimmedIntent));

        try {
            LLMClient client = new LLMClient(cfg);
            String raw = client.chat(msgs);
            String expr = extractExpression(raw);
            if (expr == null || expr.isEmpty()) {
                return Result.failure("未能从模型回复中解析出有效的 SpEL 表达式", raw);
            }
            String validated = validateAndClean(expr);
            if (validated == null) {
                return Result.failure(
                        "模型输出的表达式不符合 #method 链式 DSL 规范", raw);
            }
            return Result.success(validated, raw);
        } catch (Throwable ex) {
            logger.error("AI EL generate error: {}", ex.toString());
            return Result.failure("调用 AI 失败：" + ex.getMessage(), null);
        }
    }

    // ====================================================================
    //  Prompt 构造
    // ====================================================================

    /**
     * 构造 system prompt：把 EL DSL 的语法规则 + 全部内置模板示例
     * 一次性灌给模型，让它能自洽地生成正确表达式。
     */
    private static String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("你是 jar-analyzer 内置 SpEL 表达式生成器。\n")
                .append("用户会用自然语言描述他想在已索引的 Java 字节码中搜索什么样的方法，")
                .append("你需要输出一条符合 jar-analyzer EL DSL 规范的 SpEL 表达式。\n\n");

        sb.append("==== 严格输出格式（务必遵守） ====\n")
                .append("1. 只输出一个 <EL>...</EL> 代码块，里面是表达式本身；\n")
                .append("2. <EL> 之外可以写 1~3 行简短中文说明，但不要再有第二个 <EL> 块；\n")
                .append("3. 不要使用 ```code``` 代码块，不要带行号；\n")
                .append("4. 表达式必须以 #method 开头，并使用链式调用 .xxx(...)。\n\n");

        sb.append("==== EL DSL 规则（所有条件之间是 AND） ====\n")
                .append("- 表达式以 #method 开头\n")
                .append("- 所有 .xxx(...) 之间是 AND 关系\n")
                .append("- 字符串参数必须用双引号\n")
                .append("- 类名使用点号分隔的 FQN（例如 java.lang.Runtime）\n")
                .append("- // 单行注释合法，可用于解释意图\n\n");

        sb.append("==== 可用 DSL 方法签名 ====\n");
        for (String line : DSL_SIGNATURES) {
            sb.append("- ").append(line).append('\n');
        }
        sb.append('\n');

        sb.append("==== 内置模板（学习风格 / 命名约定，不要照搬） ====\n");
        // 直接把内置模板灌给模型，作为 few-shot 示例
        int n = 0;
        for (Map.Entry<String, String> e : Templates.data.entrySet()) {
            sb.append("# ").append(e.getKey()).append('\n')
                    .append(e.getValue()).append("\n\n");
            // 防止 prompt 过长：模板很多，但单个都很短，全量 ~20K 字符可控
            n++;
            if (n >= 60) {
                break;
            }
        }

        sb.append("==== 生成准则 ====\n")
                .append("1. 只使用上面列出的 DSL 方法，不要发明 .foo() / .bar()。\n")
                .append("2. 字符串里不要包含换行 / 反引号 / 控制字符。\n")
                .append("3. 条件不要过松（避免命中过多无关方法），优先组合 2~5 个条件。\n")
                .append("4. 涉及具体类时使用真实存在的标准库 / 框架类全名。\n")
                .append("5. 若用户描述含糊，按最常见的安全审计意图生成。\n")
                .append("6. 不要在 <EL>...</EL> 内做出对外调用 / 拉远程资源 / I/O，只输出表达式。\n");

        return sb.toString();
    }

    /**
     * DSL 方法签名清单（与 MethodEL 保持同步）
     */
    private static final String[] DSL_SIGNATURES = new String[]{
            "nameContains(String)",
            "nameNotContains(String)",
            "startWith(String)",
            "endWith(String)",
            "classNameContains(String)",
            "classNameNotContains(String)",
            "returnType(String fqn)",
            "paramTypeMap(int index, String fqn)",
            "paramsNum(int)",
            "isStatic(boolean)",
            "isPublic(boolean)",
            "isSubClassOf(String fqn)",
            "isSuperClassOf(String fqn)",
            "hasAnno(String annoSimpleName)",
            "excludeAnno(String annoSimpleName)",
            "hasClassAnno(String annoSimpleName)",
            "hasField(String fieldName)",
            "containsInvoke(String calleeClassFqn, String calleeMethodName)",
            "excludeInvoke(String calleeClassFqn, String calleeMethodName)",
            "nameRegex(String javaRegex)",
            "classNameRegex(String javaRegex)",
    };

    private static String buildUserPrompt(String intent) {
        return "用户的需求：\n\"\"\"\n" + intent + "\n\"\"\"\n\n请按要求输出表达式：";
    }

    // ====================================================================
    //  响应解析
    // ====================================================================

    /**
     * 从模型回复中提取表达式。优先匹配 &lt;EL&gt; 标签；其次匹配 ```code``` 块；
     * 都没有时，把回复中以 #method 开头一直到结束的部分截出来。
     */
    static String extractExpression(String raw) {
        if (raw == null) {
            return null;
        }
        // 1) <EL>...</EL>
        Matcher m1 = TAG_RE.matcher(raw);
        if (m1.find()) {
            return m1.group(1).trim();
        }
        // 2) fenced code block
        Matcher m2 = FENCE_RE.matcher(raw);
        if (m2.find()) {
            return m2.group(1).trim();
        }
        // 3) 兜底：扫到 #method 开头
        int idx = raw.indexOf("#method");
        if (idx >= 0) {
            return raw.substring(idx).trim();
        }
        return null;
    }

    /**
     * 校验并清理表达式：必须以 #method 开头，去掉模型可能附带的多余说明 /
     * 反引号 / 行号前缀；只保留以 // 注释、以 . 开头的链式调用 / 首行 #method。
     */
    static String validateAndClean(String expr) {
        if (expr == null) {
            return null;
        }
        // 去掉无关的 ``` 标记
        expr = expr.replace("```", "").trim();
        if (expr.isEmpty()) {
            return null;
        }
        // 必须能找到 #method
        int hash = expr.indexOf("#method");
        if (hash < 0) {
            return null;
        }
        if (hash > 0) {
            // 把 #method 之前的解释文本砍掉
            expr = expr.substring(hash);
        }
        // 截断尾部超出表达式范围的额外解释段（连续 2 个空行视为段落分隔）
        int blank = expr.indexOf("\n\n\n");
        if (blank > 0) {
            expr = expr.substring(0, blank);
        }
        // 黑名单：不允许出现明显有害的 token，避免提示注入间接生成奇怪 EL
        // SpEL 表达式本身在 ELForm 里走的是 StandardEvaluationContext + 仅 #method 变量，
        // 即便包含恶意 SpEL 也不会真正执行（求值结果不是 MethodEL 会被拒绝）；
        // 这里只做一层"看起来不像合法 DSL 就拒绝"的弱校验。
        List<String> kept = new ArrayList<>();
        boolean firstNonComment = true;
        for (String line : expr.split("\\r?\\n", -1)) {
            String t = line.trim();
            if (t.isEmpty()) {
                kept.add(line);
                continue;
            }
            if (t.startsWith("//")) {
                kept.add(line);
                continue;
            }
            if (firstNonComment) {
                if (!t.startsWith("#method")) {
                    return null;
                }
                kept.add(line);
                firstNonComment = false;
                continue;
            }
            // 后续行必须是 .xxx(...) 链式调用
            if (!t.startsWith(".")) {
                // 模型输出了多余说明，遇到第一行非链式调用就停
                break;
            }
            kept.add(line);
        }
        if (firstNonComment) {
            // 没有任何 #method 行
            return null;
        }
        StringBuilder out = new StringBuilder();
        for (String s : kept) {
            out.append(s).append('\n');
        }
        return out.toString().trim();
    }
}
