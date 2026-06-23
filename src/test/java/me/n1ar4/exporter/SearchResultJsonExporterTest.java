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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.exporter.SearchResultJsonExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchResultJsonExporterTest {

    private static MethodResult make(String className, String methodName, String desc) {
        MethodResult r = new MethodResult();
        r.setClassName(className);
        r.setMethodName(methodName);
        r.setMethodDesc(desc);
        r.setLineNumber(42);
        return r;
    }

    @Test
    void export_normalResults_shouldCreateJsonWithMethodIdentity(@TempDir Path tempDir) throws Exception {
        List<MethodResult> results = new ArrayList<>();
        results.add(make("com/example/Test", "doSomething", "(Ljava/lang/String;)V"));
        results.add(make("com/example/Other", "run", "()V"));

        String oldDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            SearchResultJsonExporter exporter = new SearchResultJsonExporter(results);
            boolean success = exporter.doExport();
            assertTrue(success, "export should succeed");
            assertNotNull(exporter.getFileName(), "file name should not be null");

            File jsonFile = new File(exporter.getFileName());
            assertTrue(jsonFile.exists(), "JSON file should exist");

            String json = new String(Files.readAllBytes(jsonFile.toPath()), "UTF-8");
            JSONArray array = JSON.parseArray(json);
            assertEquals(2, array.size());

            JSONObject first = array.getJSONObject(0);
            assertEquals("com/example/Test", first.getString("class"));
            assertEquals("doSomething", first.getString("method"));
            assertEquals("(Ljava/lang/String;)V", first.getString("desc"));
            assertFalse(first.containsKey("className"));
            assertFalse(first.containsKey("methodName"));
            assertFalse(first.containsKey("methodDesc"));
            assertFalse(first.containsKey("lineNumber"));

            jsonFile.delete();
        } finally {
            System.setProperty("user.dir", oldDir);
        }
    }

    @Test
    void export_emptyResults_shouldReturnFalse() {
        SearchResultJsonExporter exporter = new SearchResultJsonExporter(new ArrayList<>());
        assertFalse(exporter.doExport(), "empty results should not export");
    }

    @Test
    void export_nullResults_shouldReturnFalse() {
        SearchResultJsonExporter exporter = new SearchResultJsonExporter(null);
        assertFalse(exporter.doExport(), "null results should not export");
    }
}
