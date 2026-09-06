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
 * 传播/净化规则黄金测试：验证 propagation.json 与 sanitizer.json
 * 在真实字节码上的行为。
 * <p>
 * 目标 jar 为 test/rule-test 项目构建的 rule-test.jar（十三条链路：
 * 七条净化链应断链 / 五条传播链应命中 / 一条常量对照不命中），
 * 由 test-golden-rule workflow 在 CI 中构建；jar 不存在时全部跳过。
 * SINK 与 GUI 默认值一致：java/lang/Runtime#exec(String)。
 */
public class RuleGoldenTest {
    private static final String JAR = "rule-test.jar";
    private static final String DB = "jar-analyzer.db";

    private static final String SINK_CLASS = "java/lang/Runtime";
    private static final String SINK_METHOD = "exec";
    private static final String SINK_DESC = "(Ljava/lang/String;)Ljava/lang/Process;";

    private static final String CONTROLLER_DESC = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final String PKG = "me/n1ar4/rule/web/";

    @BeforeAll
    @SuppressWarnings("all")
    static void setup() {
        Assumptions.assumeTrue(Files.exists(Paths.get(JAR)),
                JAR + " 不存在，跳过（应由 test-golden-rule workflow 构建）");
        try {
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

    private static String key(String cls, String method) {
        return PKG + cls + "#" + method + "#" + CONTROLLER_DESC;
    }

    private static List<TaintResult> analyzeFrom(String srcClass, String srcMethod) {
        DFSEngine engine = new DFSEngine(null, true, false, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.setSource(PKG + srcClass, srcMethod, CONTROLLER_DESC);
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
     * 断言净化链：不应命中且产生 SANITIZER_HIT
     */
    private static void assertSanitized(String cls, String method) {
        List<TaintResult> results = analyzeFrom(cls, method);
        assertFalse(anySuccess(results), cls + " 净化链不应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.SANITIZER_HIT),
                cls + " 应命中 sanitizer.json 清洗规则");
    }

    /**
     * 断言传播链：应命中且产生 PROPAGATION_RULE_HIT
     */
    private static void assertPropagated(String cls, String method) {
        List<TaintResult> results = analyzeFrom(cls, method);
        assertTrue(anySuccess(results), cls + " 传播链应命中，实际: " + results);
        assertTrue(hasEvent(results, TaintEvent.Type.PROPAGATION_RULE_HIT),
                cls + " 应命中 propagation.json 精细传播规则");
    }

    /**
     * 从 SINK 反向查找所有 SOURCE：根节点应恰好是十三个无调用者的入口
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
        expected.add(key("SanEncoderController", "sanEncoder"));
        expected.add(key("SanJsoupController", "sanJsoup"));
        expected.add(key("SanNumericController", "sanNumeric"));
        expected.add(key("SanLang3Controller", "sanLang3"));
        expected.add(key("SanPathController", "sanPath"));
        expected.add(key("SanUrlController", "sanUrl"));
        expected.add(key("SanDigestController", "sanDigest"));
        expected.add(key("PropConcatController", "propConcat"));
        expected.add(key("PropValueOfController", "propValueOf"));
        expected.add(key("PropReplaceController", "propReplace"));
        expected.add(key("PropBufferController", "propBuffer"));
        expected.add(key("PropTrimController", "propTrim"));
        expected.add(key("ConstController", "constant"));
        assertEquals(expected, sources, "所有可能的 SOURCE 点应恰好是十三个入口");
    }

    /**
     * 全量链路 golden：五条传播链命中、七条净化链与一条对照链不命中
     */
    @Test
    void testRuleGoldenOutcome() {
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
            outcome.merge(src, r.isSuccess(), Boolean::logicalOr);
        }

        Map<String, Boolean> expected = new HashMap<>();
        // 传播链：应命中
        expected.put(key("PropConcatController", "propConcat"), true);
        expected.put(key("PropValueOfController", "propValueOf"), true);
        expected.put(key("PropReplaceController", "propReplace"), true);
        expected.put(key("PropBufferController", "propBuffer"), true);
        expected.put(key("PropTrimController", "propTrim"), true);
        // 净化链：不应命中
        expected.put(key("SanEncoderController", "sanEncoder"), false);
        expected.put(key("SanJsoupController", "sanJsoup"), false);
        expected.put(key("SanNumericController", "sanNumeric"), false);
        expected.put(key("SanLang3Controller", "sanLang3"), false);
        expected.put(key("SanPathController", "sanPath"), false);
        expected.put(key("SanUrlController", "sanUrl"), false);
        expected.put(key("SanDigestController", "sanDigest"), false);
        // 常量对照：不应命中
        expected.put(key("ConstController", "constant"), false);
        assertEquals(expected, outcome, "十三条链路的污点命中情况应与 golden 完全一致");
    }

    // -------- 净化链（应断链 + SANITIZER_HIT） --------

    @Test
    void testSanEncoder() {
        assertSanitized("SanEncoderController", "sanEncoder");
    }

    @Test
    void testSanJsoup() {
        assertSanitized("SanJsoupController", "sanJsoup");
    }

    @Test
    void testSanNumeric() {
        // parseInt 净化缺失时此链会被通用传播误报（历史高频误报场景）
        assertSanitized("SanNumericController", "sanNumeric");
    }

    @Test
    void testSanLang3() {
        assertSanitized("SanLang3Controller", "sanLang3");
    }

    @Test
    void testSanPath() {
        assertSanitized("SanPathController", "sanPath");
    }

    @Test
    void testSanUrl() {
        assertSanitized("SanUrlController", "sanUrl");
    }

    @Test
    void testSanDigest() {
        assertSanitized("SanDigestController", "sanDigest");
    }

    // -------- 传播链（应命中 + PROPAGATION_RULE_HIT） --------

    @Test
    void testPropConcat() {
        assertPropagated("PropConcatController", "propConcat");
    }

    @Test
    void testPropValueOf() {
        assertPropagated("PropValueOfController", "propValueOf");
    }

    @Test
    void testPropReplace() {
        assertPropagated("PropReplaceController", "propReplace");
    }

    @Test
    void testPropBuffer() {
        assertPropagated("PropBufferController", "propBuffer");
    }

    @Test
    void testPropTrim() {
        assertPropagated("PropTrimController", "propTrim");
    }

    // -------- 常量对照（不应命中且无净化事件） --------

    @Test
    void testConstantChainFail() {
        List<TaintResult> results = analyzeFrom("ConstController", "constant");
        assertFalse(anySuccess(results), "常量链不应命中（误报对照），实际: " + results);
        assertFalse(hasEvent(results, TaintEvent.Type.SANITIZER_HIT),
                "常量链不应出现净化事件");
    }
}
