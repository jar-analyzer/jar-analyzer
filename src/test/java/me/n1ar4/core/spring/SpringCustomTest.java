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
 * 目标 jar 为 test/test-springboot 项目（源码已入库）在 CI 中
 * 构建的 test-0.0.1-SNAPSHOT.jar，不再依赖外部下载
 */
public class SpringCustomTest {
    private static final String JAR = "test-0.0.1-SNAPSHOT.jar";
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
        try {
            // 连接数据库
            String url = "jdbc:sqlite:" + DB;
            Connection conn = DriverManager.getConnection(url);

            // spring_controller_table 表应包含 4 条数据
            PreparedStatement stmt1 = conn.prepareStatement("SELECT COUNT(*) FROM spring_controller_table");
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                int count = rs1.getInt(1);
                System.out.println("spring_controller_table 表数据条数: " + count);
                assertEquals(4, count, "spring_controller_table 表应该包含 4 条数据");
            }
            rs1.close();
            stmt1.close();

            // class_table 表应有 76 条数据
            PreparedStatement stmt3 = conn.prepareStatement("SELECT COUNT(*) FROM class_table");
            ResultSet rs3 = stmt3.executeQuery();
            if (rs3.next()) {
                int classCount = rs3.getInt(1);
                System.out.println("class_table 表数据条数: " + classCount);
                assertEquals(76, classCount, "class_table 表应该有 76 条数据");
            }
            rs3.close();
            stmt3.close();

            // method_call_table 表应有 1755 条数据
            PreparedStatement stmt4 = conn.prepareStatement("SELECT COUNT(*) FROM method_call_table");
            ResultSet rs4 = stmt4.executeQuery();
            if (rs4.next()) {
                int methodCallCount = rs4.getInt(1);
                System.out.println("method_call_table 表数据条数: " + methodCallCount);
                assertEquals(1755, methodCallCount, "method_call_table 表应该有 1755 条数据");
            }
            rs4.close();
            stmt4.close();

            // 关闭数据库连接
            conn.close();

            System.out.println("所有数据库验证通过！");

        } catch (Exception e) {
            e.printStackTrace();
            fail("测试执行失败: " + e.getMessage());
        }
    }
}
