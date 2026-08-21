/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.core.spring;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.core.CoreRunner;
import me.n1ar4.jar.analyzer.core.DatabaseManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 目标 jar 为 test/test-springboot-java8|java17|java25 项目（源码已入库）
 * 在 CI 中构建的 custom-test.jar。三个变体分别对应 Java8/SB2、
 * Java17/SB3、Java25/SB4，依赖不同导致类和方法调用数不同，
 * 期望值通过 -Dcustom.test.* 系统属性注入（默认为 java8/SB2 基准值）
 */
public class SpringCustomTest {
    private static final String JAR = System.getProperty("custom.test.jar", "custom-test.jar");
    private static final int EXPECTED_CONTROLLERS = Integer.getInteger("custom.test.controllers", 4);
    private static final int EXPECTED_CLASSES = Integer.getInteger("custom.test.classes", 76);
    private static final int EXPECTED_CALLS = Integer.getInteger("custom.test.calls", 1755);
    private static final String DB = "jar-analyzer.db";

    @BeforeAll
    @SuppressWarnings("all")
    static void setup() {
        Assumptions.assumeTrue(Files.exists(Paths.get(JAR)),
                JAR + " 不存在，跳过（应由 test-spring-custom workflow 构建）");
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

    @Test
    @SuppressWarnings("all")
    public void testRun() {
        // 先收集全部计数再统一断言，失败时一次输出所有差异
        int controllerCount = -1;
        int classCount = -1;
        int methodCallCount = -1;
        try {
            // 连接数据库
            String url = "jdbc:sqlite:" + DB;
            Connection conn = DriverManager.getConnection(url);

            // spring_controller_table 表应包含 EXPECTED_CONTROLLERS 条数据
            PreparedStatement stmt1 = conn.prepareStatement("SELECT COUNT(*) FROM spring_controller_table");
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                controllerCount = rs1.getInt(1);
                System.out.println("spring_controller_table 表数据条数: " + controllerCount);
            }
            rs1.close();
            stmt1.close();

            // class_table 表应有 EXPECTED_CLASSES 条数据
            PreparedStatement stmt3 = conn.prepareStatement("SELECT COUNT(*) FROM class_table");
            ResultSet rs3 = stmt3.executeQuery();
            if (rs3.next()) {
                classCount = rs3.getInt(1);
                System.out.println("class_table 表数据条数: " + classCount);
            }
            rs3.close();
            stmt3.close();

            // method_call_table 表应有 EXPECTED_CALLS 条数据
            PreparedStatement stmt4 = conn.prepareStatement("SELECT COUNT(*) FROM method_call_table");
            ResultSet rs4 = stmt4.executeQuery();
            if (rs4.next()) {
                methodCallCount = rs4.getInt(1);
                System.out.println("method_call_table 表数据条数: " + methodCallCount);
            }
            rs4.close();
            stmt4.close();

            // 关闭数据库连接
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试执行失败: " + e.getMessage());
            return;
        }

        assertEquals(EXPECTED_CONTROLLERS, controllerCount, "spring_controller_table 表应该包含 " + EXPECTED_CONTROLLERS + " 条数据");
        assertEquals(EXPECTED_CLASSES, classCount, "class_table 表应该有 " + EXPECTED_CLASSES + " 条数据");
        assertEquals(EXPECTED_CALLS, methodCallCount, "method_call_table 表应该有 " + EXPECTED_CALLS + " 条数据");

        System.out.println("所有数据库验证通过！");
    }
}
