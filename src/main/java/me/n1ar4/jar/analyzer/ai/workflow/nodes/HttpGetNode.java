/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.nodes;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagNode;
import me.n1ar4.jar.analyzer.ai.workflow.core.NodeResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * HTTP GET 节点。
 * <p>
 * 模板支持： <pre>
 *   {@code https://${jar-analyzer-api}/api/get_methods_by_class?class={{className}} }
 * </pre>
 * 其中 <code>${name}</code> 取自 {@link DagContext#constant(String)}，<code>{{field}}</code> 取自上游输入对象。
 * <p>
 */
public final class HttpGetNode extends DagNode {

    private static final Logger logger = LogManager.getLogger();

    /**
     * 全局共享 OkHttpClient（短连接、禁用重定向）。
     */
    private static final OkHttpClient SHARED_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    private static final Pattern CONST_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_\\-.]+)}");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)}}");

    /**
     * URL 模板。
     */
    private final String urlTemplate;

    /**
     * 当上游传入是 List 时，是否对每个元素并发拉取（默认 true）。
     */
    private final boolean fanOut;

    /**
     * 允许的 host 白名单（可选）。当为 null 或空时，仅允许 loopback / 私网。
     */
    private final Set<String> hostAllowList;

    public HttpGetNode(String id, String name, String urlTemplate) {
        this(id, name, urlTemplate, true, null);
    }

    public HttpGetNode(String id, String name, String urlTemplate,
                       boolean fanOut, Set<String> hostAllowList) {
        super(id, name);
        this.urlTemplate = urlTemplate;
        this.fanOut = fanOut;
        this.hostAllowList = hostAllowList;
    }

    @Override
    public int requiredInputs() {
        return 0; // 入度可以为 0（trigger 后直连），也可以为 1（来自 Loop）
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) throws Exception {
        Object inputData = (inputs == null || inputs.isEmpty()) ? null : inputs.get(0).getData();

        // 输入是 List：fan-out
        if (inputData instanceof List && fanOut) {
            List<Object> items = (List<Object>) inputData;
            // 对每个 item 单独构造 url 并请求
            // 注意：对外网批量请求要警惕 SSRF，因此每条 url 都会过 assertSafeHost
            java.util.List<Object> outputs = new java.util.ArrayList<>(items.size());
            for (Object item : items) {
                Map<String, Object> bag = toMap(item);
                String url = render(urlTemplate, ctx.getConstants(), bag);
                Object obj = doGet(url);
                if (obj instanceof List) {
                    // 对 fan-out 节点，子调用结果是数组：合并成一个大数组（与 n8n 行为一致）
                    outputs.addAll((List<Object>) obj);
                } else if (obj != null) {
                    outputs.add(obj);
                }
            }
            return NodeResult.ok(outputs);
        }

        // 单输入或 Map：直接渲染 + 请求
        Map<String, Object> bag = toMap(inputData);
        String url = render(urlTemplate, ctx.getConstants(), bag);
        Object obj = doGet(url);
        return NodeResult.ok(obj);
    }

    private static Map<String, Object> toMap(Object o) {
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) o;
            return m;
        }
        return new LinkedHashMap<>();
    }

    /**
     * 模板渲染：${const}（来自 ctx.constants）+ {{field}}（来自上游 item）。
     * 占位的取值都做 URL 编码。
     */
    static String render(String template, Map<String, String> constants, Map<String, Object> bag) {
        if (template == null) {
            return "";
        }
        String s = template;
        java.util.regex.Matcher m1 = CONST_PATTERN.matcher(s);
        StringBuffer sb1 = new StringBuffer();
        while (m1.find()) {
            String key = m1.group(1);
            String v = constants == null ? "" : constants.getOrDefault(key, "");
            m1.appendReplacement(sb1, java.util.regex.Matcher.quoteReplacement(v));
        }
        m1.appendTail(sb1);
        s = sb1.toString();

        java.util.regex.Matcher m2 = FIELD_PATTERN.matcher(s);
        StringBuffer sb2 = new StringBuffer();
        while (m2.find()) {
            String key = m2.group(1);
            Object o = bag == null ? null : bag.get(key);
            String v = o == null ? "" : urlEncode(String.valueOf(o));
            m2.appendReplacement(sb2, java.util.regex.Matcher.quoteReplacement(v));
        }
        m2.appendTail(sb2);
        return sb2.toString();
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * 真正的 HTTP GET。结果若为 JSON 则解析为对象，否则返回字符串。
     */
    private Object doGet(String url) throws Exception {
        if (url == null || url.isEmpty()) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Throwable t) {
            throw new IllegalArgumentException("invalid url");
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("missing scheme");
        }
        scheme = scheme.toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("scheme not allowed: " + scheme);
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("missing host");
        }
        assertSafeHost(host);

        Request req = new Request.Builder().url(url).get().build();
        try (Response resp = SHARED_CLIENT.newCall(req).execute()) {
            ResponseBody body = resp.body();
            String text = body == null ? "" : body.string();
            if (!resp.isSuccessful()) {
                logger.warn("http get {} -> {} body={}", url, resp.code(),
                        text.length() > 200 ? text.substring(0, 200) : text);
                throw new RuntimeException("http " + resp.code());
            }
            return tryParseJson(text);
        }
    }

    /**
     * 校验 host：
     * - 在 hostAllowList 内 -> 通过
     * - 否则解析所有 IP，要求每个 IP 都是 loopback 或 site-local（私网）
     * - 拒绝多播 / any-local / 链路本地
     */
    private void assertSafeHost(String host) throws Exception {
        if (hostAllowList != null && hostAllowList.contains(host.toLowerCase())) {
            return;
        }
        InetAddress[] addrs;
        try {
            addrs = InetAddress.getAllByName(host);
        } catch (Throwable t) {
            throw new IllegalArgumentException("dns resolve failed: " + host);
        }
        for (InetAddress ia : addrs) {
            if (ia.isMulticastAddress() || ia.isAnyLocalAddress() || ia.isLinkLocalAddress()) {
                throw new IllegalArgumentException("blocked address: " + ia.getHostAddress());
            }
            if (!(ia.isLoopbackAddress() || ia.isSiteLocalAddress())) {
                throw new IllegalArgumentException(
                        "non-private address blocked (set host allow list to permit): "
                                + ia.getHostAddress());
            }
        }
    }

    private static Object tryParseJson(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        char first = text.charAt(0);
        if (first != '{' && first != '[') {
            return text;
        }
        try {
            return JSON.parse(text);
        } catch (Throwable t) {
            return text;
        }
    }

    public static Set<String> defaultLocalAllowList() {
        Set<String> s = new HashSet<>();
        s.add("127.0.0.1");
        s.add("localhost");
        s.add("::1");
        return s;
    }

    @SuppressWarnings("unused")
    private static String safeUtf8(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }
}
