/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.core.golden;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.core.CoreRunner;
import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.taint.TaintAnalyzer;
import me.n1ar4.jar.analyzer.taint.TaintEvent;
import me.n1ar4.jar.analyzer.taint.TaintResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 污点分析黄金测试：TaintAnalyzer 对 DFS 链路的数据流验证。
 * <p>
 * 目标 jar 为 test/taint-test 项目构建的 taint-test.jar（五条链路：
 * 直传 / 字符串拼接传播 / 接口透传 / sanitizer 净化 / 常量误报对照），
 * 由 test-golden-taint workflow 在 CI 中构建；jar 不存在时全部跳过。
 * SINK 与 GUI 默认值一致：java/lang/Runtime#exec(String)。
 */
public class TaintGoldenTest {
    private static final String JAR = "taint-test.jar";
    private static final String DB = "jar-analyzer.db";

    private static final String SINK_CLASS = "java/lang/Runtime";
    private static final String SINK_METHOD = "exec";
    private static final String SINK_DESC = "(Ljava/lang/String;)Ljava/lang/Process;";

    private static final String CONTROLLER_DESC = "(Ljava/lang/String;)Ljava/lang/String;";

    @BeforeAll
    @SuppressWarnings("all")
    static void setup() {
        Assumptions.assumeTrue(Files.exists(Paths.get(JAR)),
                JAR + " 不存在，跳过（应由 test-golden-taint workflow 构建）");
        try {
            // 关闭可能存在的数据库会话再删除重建：
            // 同一 JVM 顺序执行多个测试类时也保持安全
            DatabaseManager.closeForRebuild();
            Files.delete(Paths.get(DB));
            Files.deleteIfExists(Paths.get(DB + "-wal"));
            Files.deleteIfExists(Paths.get(DB + "-shm"));
        } catch (Exception ignored) {
        }
        DatabaseManager.reopen();

        AnalyzeEnv.isCli = true;
        CoreRunner.run(Paths.get(JAR), null, false, null);
    }

    private static String key(MethodReference.Handle h) {
        return h.getClassReference().getName() + "#" + h.getName() + "#" + h.getDesc();
    }

    /**
     * 从 SINK 反向推导指定 SOURCE 的链路并做污点分析
     */
    private static List<TaintResult> analyzeFrom(String srcClass, String srcMethod) {
        DFSEngine engine = new DFSEngine(null, true, false, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.setSource(srcClass, srcMethod, CONTROLLER_DESC);
        engine.doAnalyze();
        assertFalse(engine.getResults().isEmpty(),
                "应至少找到一条链: " + srcClass + "#" + srcMethod);
        return TaintAnalyzer.analyze(engine.getResults());
    }

    private static boolean anySuccess(List<TaintResult> results) {
        for (TaintResult r : results) {
            if (r.isSuccess()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEvent(List<TaintResult> results, TaintEvent.Type type) {
        for (TaintResult r : results) {
            for (TaintEvent e : r.getEvents()) {
                if (e.getType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从 SINK 反向查找所有 SOURCE：根节点应恰好是五个无调用者的入口
     */
    @Test
    void testDfsFindAllSources() {
        DFSEngine engine = new DFSEngine(null, true, true, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.doAnalyze();

        Set<String> sources = new HashSet<>();
        for (DFSResult r : engine.getResults()) {
            if (r.getSource() != null) {
                sources.add(key(r.getSource()));
            }
        }
        Set<String> expected = new HashSet<>();
        expected.add("me/n1ar4/taint/web/DirectController#direct#" + CONTROLLER_DESC);
        expected.add("me/n1ar4/taint/web/PropController#prop#" + CONTROLLER_DESC);
        expected.add("me/n1ar4/taint/web/IfaceController#iface#" + CONTROLLER_DESC);
        expected.add("me/n1ar4/taint/web/SafeController#safe#" + CONTROLLER_DESC);
        expected.add("me/n1ar4/taint/web/ConstController#constant#" + CONTROLLER_DESC);
        assertEquals(expected, sources, "所有可能的 SOURCE 点应恰好是五个入口");
    }

    /**
     * 全量链路的污点分析结果 golden：三条可控链命中、两条对照链不命中
     */
    @Test
    void testTaintAllChainsOutcome() {
        DFSEngine engine = new DFSEngine(null, true, true, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.doAnalyze();
        assertFalse(engine.getResults().isEmpty(), "findAllSources 应找到链路");

        List<TaintResult> results = TaintAnalyzer.analyze(engine.getResults());
        assertFalse(results.isEmpty(), "污点分析应返回结果");

        Map<String, Boolean> outcome = new HashMap<>();
        for (TaintResult r : results) {
            if (r.getDfsResult() == null || r.getDfsResult().getSource() == null) {
                continue;
            }
            String src = key(r.getDfsResult().getSource());
            // 同一 SOURCE 可能有多条链：任一条存活即视为命中
            outcome.merge(src, r.isSuccess(), Boolean::logicalOr);
        }

        Map<String, Boolean> expected = new HashMap<>();
        expected.put("me/n1ar4/taint/web/DirectController#direct#" + CONTROLLER_DESC, true);
        expected.put("me/n1ar4/taint/web/PropController#prop#" + CONTROLLER_DESC, true);
        expected.put("me/n1ar4/taint/web/IfaceController#iface#" + CONTROLLER_DESC, true);
        expected.put("me/n1ar4/taint/web/SafeController#safe#" + CONTROLLER_DESC, false);
        expected.put("me/n1ar4/taint/web/ConstController#constant#" + CONTROLLER_DESC, false);
        assertEquals(expected, outcome, "五条链路的污点命中情况应与 golden 完全一致");
    }

    /**
     * 链路 1：入口参数经中间方法直传 SINK，应命中且产生 REACH_NEXT 事件
     */
    @Test
    void testTaintDirectChainPass() {
        List<TaintResult> results =
                analyzeFrom("me/n1ar4/taint/web/DirectController", "direct");
        assertTrue(anySuccess(results), "直传链应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.REACH_NEXT),
                "直传链应产生 REACH_NEXT 事件");
    }

    /**
     * 链路 2："前缀" + cmd 经 StringBuilder append/toString 传播，
     * 应命中且产生 PROPAGATION_RULE_HIT 事件
     */
    @Test
    void testTaintPropagationChainPass() {
        List<TaintResult> results =
                analyzeFrom("me/n1ar4/taint/web/PropController", "prop");
        assertTrue(anySuccess(results), "字符串拼接链应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.PROPAGATION_RULE_HIT),
                "应命中 propagation.json 精细传播规则");
    }

    /**
     * 链路 3：污点经接口方法透传到实现类，应命中且产生 INTERFACE_PASSTHROUGH 事件
     */
    @Test
    void testTaintInterfaceChainPass() {
        List<TaintResult> results =
                analyzeFrom("me/n1ar4/taint/web/IfaceController", "iface");
        assertTrue(anySuccess(results), "接口透传链应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.INTERFACE_PASSTHROUGH),
                "接口链应产生 INTERFACE_PASSTHROUGH 事件");
    }

    /**
     * 链路 4：Pattern.quote 净化后传入 SINK，不应命中且产生 SANITIZER_HIT 事件
     */
    @Test
    void testTaintSanitizerChainFail() {
        List<TaintResult> results =
                analyzeFrom("me/n1ar4/taint/web/SafeController", "safe");
        assertFalse(anySuccess(results), "净化链不应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.SANITIZER_HIT),
                "应命中 sanitizer.json 清洗规则");
    }

    /**
     * 链路 5：SINK 收到常量、入口参数与 SINK 无数据流关系，不应命中（误报对照）
     */
    @Test
    void testTaintConstantChainFail() {
        List<TaintResult> results =
                analyzeFrom("me/n1ar4/taint/web/ConstController", "constant");
        assertFalse(anySuccess(results), "常量链不应命中（误报对照），实际: " + results);
    }
}
