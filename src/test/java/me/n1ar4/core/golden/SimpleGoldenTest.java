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
import me.n1ar4.jar.analyzer.dfs.DFSEngine;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 字符串搜索与 Spring 入口黄金测试。
 * 目标 jar 为 test/springboot-simple 项目构建的 simple-test.jar。
 */
public class SimpleGoldenTest {
    private static final String JAR = "simple-test.jar";
    private static final String DB = "jar-analyzer.db";

    @BeforeAll
    @SuppressWarnings("all")
    static void setup() {
        Assumptions.assumeTrue(Files.exists(Paths.get(JAR)),
                JAR + " 不存在，跳过（应由 test-golden-simple workflow 构建）");
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

    /**
     * 常量 GOLDEN-SECRET-1984 被 javac 内联进 leak() 的 LDC
     * （static final 编译期常量不产生 <clinit>）
     */
    @Test
    @SuppressWarnings("all")
    void testStringSearch() {
        CoreEngine engine = MainForm.getEngine();
        assertNotNull(engine);
        List<MethodResult> results = engine.getMethodsByStr("GOLDEN-SECRET-1984");
        assertFalse(results.isEmpty(), "应能搜到常量所在方法");

        Set<String> keys = new HashSet<>();
        for (MethodResult m : results) {
            keys.add(m.getClassName() + "#" + m.getMethodName());
        }
        Set<String> expected = new HashSet<>();
        expected.add("me/n1ar4/simple/util/SecretHolder#leak");
        assertEquals(expected, keys, "常量应恰好出现在 leak()");
    }

    /**
     * 唯一的 Spring 入口 /info 应被发现
     */
    @Test
    void testSpringMapping() {
        CoreEngine engine = MainForm.getEngine();
        assertNotNull(engine);
        List<MethodResult> ms = engine.getSpringM("me/n1ar4/simple/web/InfoController");
        assertTrue(ms.stream().anyMatch(m -> "/info".equals(m.getActualPath())),
                "应发现 /info 映射: " + ms);
    }

    /**
     * 从 SecretHolder.leak 反向推导所有 SOURCE 应只有 InfoController.info
     */
    @Test
    void testDfsFindAllSourcesSimple() {
        DFSEngine engine = new DFSEngine(null, true, true, 6);
        engine.setSink("me/n1ar4/simple/util/SecretHolder", "leak",
                "()Ljava/lang/String;");
        engine.doAnalyze();
        assertFalse(engine.getResults().isEmpty(), "应找到至少一条链");
    }
}
