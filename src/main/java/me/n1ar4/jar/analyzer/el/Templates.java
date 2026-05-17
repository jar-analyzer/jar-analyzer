/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import java.util.LinkedHashMap;

/**
 * Built-in SPEL templates surfaced in the EL panel's dropdown.
 * <p>
 * Conventions:
 * <ul>
 *   <li>Keys are grouped with a leading sigil so the combo box shows
 *       cohesive sections: 「基础」「Web 入口」「命令执行」
 *       「反序列化 / 反射」「JNDI / SSRF」「文件 / IO」「加解密 / 凭据」
 *       「Spring 专题」「数据访问」「实用模式」.</li>
 *   <li>Every template starts with comments explaining intent +
 *       typical CVE references when applicable, then a #method chain.</li>
 *   <li>Conditions are AND'd by the engine, so each template aims to
 *       be tight enough to land &lt; 1000 hits on real-world jars.</li>
 *   <li>Only DSL methods declared in {@link MethodEL} are used.</li>
 * </ul>
 */
public class Templates {
    public static final LinkedHashMap<String, String> data = new LinkedHashMap<>();

    static {
        // ============================================================
        // 基础 / 入门
        // ============================================================
        data.put("【基础】默认模板（演示所有条件）",
                "// 演示所有可用条件 -- 实际使用时请删除不需要的行\n" +
                        "// 所有条件之间是 AND 关系\n" +
                        "#method\n" +
                        "        .startWith(\"set\")\n" +
                        "        .endWith(\"value\")\n" +
                        "        .nameContains(\"lookup\")\n" +
                        "        .nameNotContains(\"internal\")\n" +
                        "        .classNameContains(\"Context\")\n" +
                        "        .classNameNotContains(\"Abstract\")\n" +
                        "        .returnType(\"java.lang.Process\")\n" +
                        "        .paramTypeMap(0,\"java.lang.String\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .isStatic(false)\n" +
                        "        .isPublic(true)\n" +
                        "        .isSubClassOf(\"java.awt.Component\")\n" +
                        "        .isSuperClassOf(\"com.test.SomeClass\")\n" +
                        "        .hasClassAnno(\"Controller\")\n" +
                        "        .hasAnno(\"RequestMapping\")\n" +
                        "        .excludeAnno(\"Auth\")\n" +
                        "        .hasField(\"context\")\n" +
                        "        .containsInvoke(\"java.lang.Runtime\",\"exec\")\n" +
                        "        .excludeInvoke(\"java.lang.System\",\"exit\")\n" +
                        "        .nameRegex(\"get.*|set.*\")\n" +
                        "        .classNameRegex(\".*Controller.*\")");

        data.put("【基础】所有 public static 方法",
                "// 列出所有 public static 方法 -- 通常是工具类入口或 SPI 注册点\n" +
                        "#method\n" +
                        "        .isPublic(true)\n" +
                        "        .isStatic(true)\n" +
                        "        .nameNotContains(\"<init>\")\n" +
                        "        .nameNotContains(\"<clinit>\")");

        data.put("【基础】所有 main 方法",
                "// 定位所有 main 入口 -- 常用于发现可执行的 jar/class\n" +
                        "#method\n" +
                        "        .nameRegex(\"main\")\n" +
                        "        .isPublic(true)\n" +
                        "        .isStatic(true)\n" +
                        "        .returnType(\"void\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .paramTypeMap(0,\"java.lang.String[]\")");

        // ============================================================
        // Web 入口 / Controller
        // ============================================================
        data.put("【Web】Spring Controller 全部接口",
                "// 列出所有 @RequestMapping / @GetMapping / @PostMapping 等接口\n" +
                        "// 注意 nameRegex 里的 'Mapping' 子串可同时匹配 GetMapping/PostMapping/...\n" +
                        "#method\n" +
                        "        .hasAnno(\"Mapping\")\n" +
                        "        .hasClassAnno(\"Controller\")\n" +
                        "        .isPublic(true)");

        data.put("【Web】所有 GET 接口",
                "// 仅 @GetMapping 修饰的方法\n" +
                        "#method\n" +
                        "        .hasAnno(\"GetMapping\")\n" +
                        "        .isPublic(true)");

        data.put("【Web】所有 POST 接口",
                "// 仅 @PostMapping 修饰的方法\n" +
                        "#method\n" +
                        "        .hasAnno(\"PostMapping\")\n" +
                        "        .isPublic(true)");

        data.put("【Web】未鉴权接口（无 @PreAuthorize / @Auth）",
                "// 找出有 @RequestMapping 但缺少鉴权注解的接口 -- 越权审计起点\n" +
                        "// 同时排除常见鉴权注解；按需扩展 excludeAnno 列表\n" +
                        "#method\n" +
                        "        .hasAnno(\"Mapping\")\n" +
                        "        .excludeAnno(\"PreAuthorize\")\n" +
                        "        .excludeAnno(\"RequiresPermissions\")\n" +
                        "        .excludeAnno(\"Auth\")\n" +
                        "        .isPublic(true)");

        data.put("【Web】Servlet doGet/doPost",
                "// HttpServlet 的请求处理方法 -- 经典 Web 入口\n" +
                        "#method\n" +
                        "        .nameRegex(\"do(Get|Post|Put|Delete|Head|Options)\")\n" +
                        "        .isPublic(true)\n" +
                        "        .isSubClassOf(\"javax.servlet.http.HttpServlet\")");

        data.put("【Web】Filter doFilter",
                "// 自定义 Filter 实现 -- 关注其链路是否被绕过\n" +
                        "#method\n" +
                        "        .nameContains(\"doFilter\")\n" +
                        "        .isPublic(true)\n" +
                        "        .isSubClassOf(\"javax.servlet.Filter\")");

        // ============================================================
        // 命令执行
        // ============================================================
        data.put("【命令执行】调用 Runtime.exec",
                "// 任何调用 Runtime.exec 的方法 -- 命令注入审计起点\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.Runtime\",\"exec\")");

        data.put("【命令执行】调用 ProcessBuilder.start",
                "// ProcessBuilder 入口 -- exec 的现代替代品\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.ProcessBuilder\",\"start\")");

        data.put("【命令执行】调用 Runtime.exec 但排除 System.exit",
                "// 真正的命令执行而非进程退出语义；同时排除 SecurityManager\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.Runtime\",\"exec\")\n" +
                        "        .excludeInvoke(\"java.lang.System\",\"exit\")\n" +
                        "        .excludeInvoke(\"java.lang.System\",\"setSecurityManager\")");

        data.put("【命令执行】可疑 shell / cmd 调用",
                "// 通过方法体调用关系找疑似 shell / bash / cmd 拼接命令的入口\n" +
                        "// 注意：实际命令字符串需进一步在源码中确认\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.Runtime\",\"exec\")\n" +
                        "        .nameRegex(\"(?i).*(exec|shell|cmd|run|invoke).*\")");

        // ============================================================
        // 反序列化 / 反射
        // ============================================================
        data.put("【反序列化】readObject 实现（原生）",
                "// 自定义 readObject -- Java 原生反序列化 gadget 入口\n" +
                        "#method\n" +
                        "        .nameRegex(\"readObject\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .paramTypeMap(0,\"java.io.ObjectInputStream\")");

        data.put("【反序列化】readResolve 实现",
                "// readResolve 钩子 -- 反序列化后的替换对象，gadget 链常用\n" +
                        "#method\n" +
                        "        .nameRegex(\"readResolve\")\n" +
                        "        .paramsNum(0)");

        data.put("【反序列化】readExternal 实现",
                "// 实现 Externalizable 的反序列化入口\n" +
                        "#method\n" +
                        "        .nameRegex(\"readExternal\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .paramTypeMap(0,\"java.io.ObjectInput\")");

        data.put("【反序列化】Jackson 多态可疑入口",
                "// 含有 @JsonTypeInfo 或类似的多态反序列化设置 -- 反序列化 RCE 风险\n" +
                        "#method\n" +
                        "        .hasClassAnno(\"JsonTypeInfo\")");

        data.put("【反射】Method.invoke 调用点",
                "// 反射调用 -- 反序列化 gadget / 表达式注入常用通路\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.reflect.Method\",\"invoke\")");

        data.put("【反射】Class.forName 调用点",
                "// 动态类加载 -- 关注上游 className 是否可被外部控制\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.Class\",\"forName\")");

        data.put("【反射】ClassLoader.loadClass 调用点",
                "// 自定义 ClassLoader 加载 -- 字节码注入起点\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.ClassLoader\",\"loadClass\")");

        data.put("【反射】defineClass 自定义类加载",
                "// 通过 defineClass 直接将字节数组加载为类 -- 内存马常用\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.lang.ClassLoader\",\"defineClass\")");

        // ============================================================
        // JNDI / SSRF
        // ============================================================
        data.put("【JNDI】Context.lookup 调用点",
                "// JNDI 注入入口 -- Log4Shell / Fastjson 经典通路\n" +
                        "#method\n" +
                        "        .containsInvoke(\"javax.naming.Context\",\"lookup\")");

        data.put("【JNDI】InitialContext 创建",
                "// 任何手动创建 InitialContext 的方法\n" +
                        "#method\n" +
                        "        .containsInvoke(\"javax.naming.InitialContext\",\"<init>\")");

        data.put("【SSRF】URL / HttpURLConnection",
                "// URL 打开连接 -- SSRF 起点\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.net.URL\",\"openConnection\")");

        data.put("【SSRF】OkHttp / Apache HttpClient",
                "// 主流 HTTP 客户端调用 -- 重点排查参数来源\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i).*(fetch|request|get|post|send|invoke).*\")\n" +
                        "        .containsInvoke(\"okhttp3.OkHttpClient\",\"newCall\")");

        data.put("【SSRF】SocketFactory / Socket",
                "// 任意网络连接出口 -- 内网探测通常从此出发\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.net.Socket\",\"<init>\")");

        // ============================================================
        // 表达式 / 模板注入
        // ============================================================
        data.put("【表达式注入】SpEL Expression.getValue",
                "// SpEL 表达式动态求值 -- Spring SpEL 注入\n" +
                        "#method\n" +
                        "        .containsInvoke(\"org.springframework.expression.Expression\",\"getValue\")");

        data.put("【表达式注入】Ognl / MVEL",
                "// 主流表达式引擎调用点\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i).*(eval|parse|execute|getValue).*\")\n" +
                        "        .classNameRegex(\".*(Ognl|MVEL|JEXL|Aviator).*\")");

        data.put("【表达式注入】ScriptEngine.eval",
                "// JSR-223 脚本引擎 -- nashorn / js / groovy 通用入口\n" +
                        "#method\n" +
                        "        .containsInvoke(\"javax.script.ScriptEngine\",\"eval\")");

        data.put("【模板注入】FreeMarker / Velocity / Thymeleaf",
                "// 用户字符串作为模板内容 render -- SSTI 常见误用\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i).*(render|process|merge|evaluate).*\")\n" +
                        "        .classNameRegex(\".*(freemarker|velocity|thymeleaf).*\")");

        // ============================================================
        // 文件 / IO
        // ============================================================
        data.put("【文件】File.delete / Files.delete",
                "// 文件删除 -- 任意文件删除审计起点\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.io.File\",\"delete\")");

        data.put("【文件】FileInputStream / Files.read*",
                "// 任意文件读取 -- 配合外部输入参数判断 LFI\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.io.FileInputStream\",\"<init>\")");

        data.put("【文件】FileOutputStream / Files.write",
                "// 任意文件写入 -- 配合外部输入参数判断 LFW / 文件上传落地\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.io.FileOutputStream\",\"<init>\")");

        data.put("【文件】ZipEntry 解压（zip-slip 风险）",
                "// ZipFile.getEntry 调用 -- 检查路径是否做了 startsWith 边界校验\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.util.zip.ZipFile\",\"getEntry\")");

        // ============================================================
        // 加解密 / 凭据
        // ============================================================
        data.put("【加密】不安全的 MD5 / SHA-1 哈希",
                "// 用 MessageDigest 创建实例 -- 进一步看 algorithm 是否为 MD5/SHA-1\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.security.MessageDigest\",\"getInstance\")");

        data.put("【加密】Cipher.getInstance（含 ECB 风险）",
                "// 加密模式选择点 -- 重点关注是否使用 AES/ECB 等不安全模式\n" +
                        "#method\n" +
                        "        .containsInvoke(\"javax.crypto.Cipher\",\"getInstance\")");

        data.put("【加密】不安全随机数（java.util.Random）",
                "// 任何使用非密码学随机的方法 -- token / 验证码 / 密钥若用此则严重\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.util.Random\",\"<init>\")");

        data.put("【凭据】疑似硬编码密码 / 密钥",
                "// 名字带 password / secret / token 的字段所在类的相关方法\n" +
                        "// 配合代码查看是否硬编码字面量\n" +
                        "#method\n" +
                        "        .hasField(\"password\")");

        // ============================================================
        // Spring 专题
        // ============================================================
        data.put("【Spring】@Autowired 注入点",
                "// 字段或 setter 上有 @Autowired -- bean 装配关系审计\n" +
                        "#method\n" +
                        "        .hasAnno(\"Autowired\")");

        data.put("【Spring】@Value 配置注入点",
                "// 任何 @Value 注入 -- 关注是否暴露了应配置项到不可信代码路径\n" +
                        "#method\n" +
                        "        .hasAnno(\"Value\")");

        data.put("【Spring】@Bean 工厂方法",
                "// 所有 @Bean 工厂方法 -- 用于排查可疑的自定义 bean\n" +
                        "#method\n" +
                        "        .hasAnno(\"Bean\")\n" +
                        "        .hasClassAnno(\"Configuration\")");

        data.put("【Spring】@PostConstruct 初始化方法",
                "// bean 初始化生命周期 -- 危险逻辑常常埋在这里\n" +
                        "#method\n" +
                        "        .hasAnno(\"PostConstruct\")");

        data.put("【Spring】Spring Security 鉴权放行点",
                "// 配置 security 规则的方法\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i).*(configure|filterChain|authorize).*\")\n" +
                        "        .containsInvoke(\"org.springframework.security.config.annotation.web.builders.HttpSecurity\",\"authorizeRequests\")");

        // ============================================================
        // 数据访问
        // ============================================================
        data.put("【SQL】Statement.execute / executeQuery",
                "// 直接拼接 SQL -- SQL 注入排查起点\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.sql.Statement\",\"executeQuery\")");

        data.put("【SQL】PreparedStatement 创建",
                "// 参数化查询入口 -- 反向核对是否所有用户输入都走 PreparedStatement\n" +
                        "#method\n" +
                        "        .containsInvoke(\"java.sql.Connection\",\"prepareStatement\")");

        data.put("【SQL】MyBatis 动态 SQL @Select / @Update",
                "// 注解式 SQL -- 易出现 ${} 拼接\n" +
                        "#method\n" +
                        "        .hasAnno(\"Select\")");

        data.put("【NoSQL】MongoDB / Redis 调用",
                "// 主流 NoSQL 客户端 -- 审计点\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i).*(find|query|get|set|del).*\")\n" +
                        "        .classNameRegex(\".*(mongo|redis|jedis).*\")");

        // ============================================================
        // 实用模式 / Gadget 探测
        // ============================================================
        data.put("【实用】单 String 参数 public 构造方法",
                "// 历史参考 PGSQL Driver RCE (CVE-2022-21724)\n" +
                        "// 也可参考 Apache ActiveMQ RCE (CVE-2023-46604)\n" +
                        "#method\n" +
                        "        .nameContains(\"<init>\")\n" +
                        "        .paramTypeMap(0,\"java.lang.String\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .isStatic(false)\n" +
                        "        .isPublic(true)");

        data.put("【实用】Swing RCE 风格 set 方法",
                "// JDK CVE-2023-21939 模式：单 String setter + Component 子类\n" +
                        "// 注意：由于多层继承关系建议加入 rt.jar 环境\n" +
                        "#method\n" +
                        "        .startWith(\"set\")\n" +
                        "        .paramsNum(1)\n" +
                        "        .isStatic(false)\n" +
                        "        .isPublic(true)\n" +
                        "        .paramTypeMap(0,\"java.lang.String\")\n" +
                        "        .isSubClassOf(\"java.awt.Component\")");

        data.put("【实用】危险方法名正则汇总",
                "// 用正则一键扫敏感方法名 -- 快速摸底\n" +
                        "#method\n" +
                        "        .nameRegex(\"(?i)(exec|invoke|lookup|deserialize|decode|eval|render|forName|loadClass).*\")\n" +
                        "        .isPublic(true)");

        data.put("【实用】Getter / Setter Gadget 候选",
                "// 单参数 setter 或无参 getter -- ROME / Jackson gadget 链常用\n" +
                        "#method\n" +
                        "        .nameRegex(\"^(get|set|is)[A-Z].*\")\n" +
                        "        .isPublic(true)\n" +
                        "        .isStatic(false)");

        data.put("【实用】toString / hashCode / equals",
                "// 这些隐式调用点常被反序列化 gadget 利用作为触发器\n" +
                        "#method\n" +
                        "        .nameRegex(\"(toString|hashCode|equals|finalize|compareTo)\")\n" +
                        "        .isPublic(true)\n" +
                        "        .isStatic(false)");

        data.put("【实用】抽象类 / 接口的所有实现入口",
                "// 列出某接口/抽象类下的全部实现方法 -- 用于追踪多态调用链\n" +
                        "// 把 isSubClassOf 的目标改成你关心的接口/基类\n" +
                        "#method\n" +
                        "        .isSubClassOf(\"java.lang.Runnable\")\n" +
                        "        .nameContains(\"run\")\n" +
                        "        .paramsNum(0)");
    }
}
