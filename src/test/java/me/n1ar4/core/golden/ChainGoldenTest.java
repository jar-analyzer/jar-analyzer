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
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.ELSearchMapper;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.el.ELQueryPlanner;
import me.n1ar4.jar.analyzer.el.MethodEL;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 核心资产黄金测试：DFSEngine（调用链推导）与 ELQueryPlanner（表达式下推）。
 * <p>
 * 目标 jar 为 test/springboot-chain 项目构建的 chain-test.jar，
 * 由 test-golden workflow 在 CI 中构建；jar 不存在时全部跳过。
 */
public class ChainGoldenTest {
    private static final String JAR = "chain-test.jar";
    private static final String DB = "jar-analyzer.db";

    private static final String SINK_CLASS = "me/n1ar4/chain/util/CmdUtil";
    private static final String SINK_METHOD = "exec";
    private static final String SINK_DESC = "(Ljava/lang/String;)V";

    @BeforeAll
    @SuppressWarnings("all")
    static void setup() {
        Assumptions.assumeTrue(Files.exists(Paths.get(JAR)),
                JAR + " 不存在，跳过（应由 test-golden-chain workflow 构建）");
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

    private static List<Set<String>> chainKeySets(DFSEngine engine) {
        List<Set<String>> chains = new ArrayList<>();
        for (DFSResult r : engine.getResults()) {
            Set<String> chain = new HashSet<>();
            for (MethodReference.Handle h : r.getMethodList()) {
                chain.add(key(h));
            }
            chains.add(chain);
        }
        return chains;
    }

    /**
     * 链路 A（无接口）：DirectController.exec -> CmdService.handle -> CmdUtil.exec
     * 从 SINK 反向推导应得到恰好这 3 个节点的一条链
     */
    @Test
    void testDfsExactChainFromSink() {
        DFSEngine engine = new DFSEngine(null, true, false, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.setSource("me/n1ar4/chain/web/DirectController", "exec",
                "(Ljava/lang/String;)Ljava/lang/String;");
        engine.doAnalyze();

        List<DFSResult> results = engine.getResults();
        assertFalse(results.isEmpty(), "应至少找到一条链");

        Set<String> expected = new HashSet<>();
        expected.add("me/n1ar4/chain/util/CmdUtil#exec#(Ljava/lang/String;)V");
        expected.add("me/n1ar4/chain/service/CmdService#handle#(Ljava/lang/String;)Ljava/lang/String;");
        expected.add("me/n1ar4/chain/web/DirectController#exec#(Ljava/lang/String;)Ljava/lang/String;");

        boolean found = false;
        for (Set<String> chain : chainKeySets(engine)) {
            if (chain.size() == 3 && chain.equals(expected)) {
                found = true;
            }
        }
        assertTrue(found, "应存在恰好 3 节点的链路 A，实际: " + chainKeySets(engine));
    }

    /**
     * 链路 B（接口链）：IfaceController.exec 经 GadgetService 接口
     * 调用 GadgetServiceImpl 最终到达 CmdUtil.exec，验证 interface -> impl 桥接
     */
    @Test
    void testDfsInterfaceBridgeChain() {
        DFSEngine engine = new DFSEngine(null, true, false, 8);
        engine.setSink(SINK_CLASS, SINK_METHOD, SINK_DESC);
        engine.setSource("me/n1ar4/chain/web/IfaceController", "exec",
                "(Ljava/lang/String;)Ljava/lang/String;");
        engine.doAnalyze();

        List<Set<String>> chains = chainKeySets(engine);
        assertFalse(chains.isEmpty(), "接口链应可以被推导");

        String sourceKey = "me/n1ar4/chain/web/IfaceController#exec#(Ljava/lang/String;)Ljava/lang/String;";
        String sinkKey = SINK_CLASS + "#" + SINK_METHOD + "#" + SINK_DESC;
        boolean found = false;
        for (Set<String> chain : chains) {
            if (chain.contains(sourceKey) && chain.contains(sinkKey) && chain.size() >= 2) {
                found = true;
            }
        }
        assertTrue(found, "应存在连接 IfaceController 与 CmdUtil 的链路，实际: " + chains);
    }

    /**
     * 从 SINK 反向查找所有 SOURCE：根节点应恰好是两个无调用者的 Controller 入口
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
        expected.add("me/n1ar4/chain/web/DirectController#exec#(Ljava/lang/String;)Ljava/lang/String;");
        expected.add("me/n1ar4/chain/web/IfaceController#exec#(Ljava/lang/String;)Ljava/lang/String;");
        assertEquals(expected, sources, "所有可能的 SOURCE 点应恰好是两个 Controller 入口");
    }

    /**
     * EL 表达式应正确下推到 SQL 参数（className/method/isStatic），
     * 且 selectCandidates 端到端返回唯一目标方法
     */
    @Test
    @SuppressWarnings("all")
    void testElPlannerAndCandidates() {
        MethodEL c = new MethodEL();
        c.setNameContains("exec");
        c.setClassNameContains("chain/util");
        c.setStatic(true);

        try (SqlSession session = SqlSessionFactoryUtil.sqlSessionFactory.openSession(true)) {
            ELQueryPlanner.Plan plan = ELQueryPlanner.plan(c, session, null);
            assertFalse(plan.impossible, "可行表达式不应被判定为 impossible");
            assertEquals("exec", plan.sqlParams.get("methodNameContains"));
            assertEquals("chain/util", plan.sqlParams.get("classNameContains"));
            assertEquals(1, plan.sqlParams.get("isStatic"));

            ELSearchMapper mapper = session.getMapper(ELSearchMapper.class);
            List<MethodResult> candidates = mapper.selectCandidates(plan.sqlParams);
            assertEquals(1, candidates.size(), "应恰好命中 CmdUtil.exec");
            MethodResult m = candidates.get(0);
            assertEquals(SINK_CLASS, m.getClassName());
            assertEquals(SINK_METHOD, m.getMethodName());
            assertEquals(SINK_DESC, m.getMethodDesc());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Spring 入口发现：两个 @RestController 及其 @GetMapping 路径
     */
    @Test
    void testSpringControllerDiscovery() {
        CoreEngine engine = MainForm.getEngine();
        assertNotNull(engine, "CoreRunner 结束后 engine 应可用");
        ArrayList<ClassResult> controllers = engine.getAllSpringC();
        Set<String> names = controllers.stream()
                .map(ClassResult::getClassName).collect(Collectors.toSet());
        assertTrue(names.contains("me/n1ar4/chain/web/DirectController"),
                "应发现 DirectController: " + names);
        assertTrue(names.contains("me/n1ar4/chain/web/IfaceController"),
                "应发现 IfaceController: " + names);
        List<MethodResult> ms = engine.getSpringM("me/n1ar4/chain/web/DirectController");
        assertTrue(ms.stream().anyMatch(m -> "/direct/exec".equals(m.getActualPath())),
                "应发现 /direct/exec 映射: " + ms);
    }
}
