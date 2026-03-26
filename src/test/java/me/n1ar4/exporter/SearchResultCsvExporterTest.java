/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.exporter;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.exporter.SearchResultCsvExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchResultCsvExporterTest {

    private static MethodResult make(String className, String methodName, String desc, int line) {
        MethodResult r = new MethodResult();
        r.setClassName(className);
        r.setMethodName(methodName);
        r.setMethodDesc(desc);
        r.setLineNumber(line);
        return r;
    }

    @Test
    void export_normalResults_shouldCreateCsv(@TempDir Path tempDir) throws Exception {
        List<MethodResult> results = new ArrayList<>();
        results.add(make("com/example/Test", "doSomething", "(Ljava/lang/String;)V", 42));
        results.add(make("com/example/Other", "run", "()V", 10));

        // 切换到临时目录执行导出
        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            SearchResultCsvExporter exporter = new SearchResultCsvExporter(results);
            boolean success = exporter.doExport();
            assertTrue(success, "导出应成功");
            assertNotNull(exporter.getFileName(), "文件名不应为 null");

            File csvFile = new File(exporter.getFileName());
            assertTrue(csvFile.exists(), "CSV 文件应存在");

            List<String> lines = Files.readAllLines(csvFile.toPath());
            assertEquals(3, lines.size(), "应有 1 行表头 + 2 行数据");
            assertEquals("ClassName,MethodName,MethodDesc,LineNumber", lines.get(0));
            assertTrue(lines.get(1).contains("com/example/Test"));
            assertTrue(lines.get(1).contains("doSomething"));
            assertTrue(lines.get(2).contains("com/example/Other"));

            // 清理
            csvFile.delete();
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void export_emptyResults_shouldReturnFalse() {
        List<MethodResult> results = new ArrayList<>();
        SearchResultCsvExporter exporter = new SearchResultCsvExporter(results);
        boolean success = exporter.doExport();
        assertFalse(success, "空结果不应导出");
    }

    @Test
    void export_nullResults_shouldReturnFalse() {
        SearchResultCsvExporter exporter = new SearchResultCsvExporter(null);
        boolean success = exporter.doExport();
        assertFalse(success, "null 结果不应导出");
    }
}
