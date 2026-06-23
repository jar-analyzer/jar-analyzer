/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.exporter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultJsonExporter implements Exporter {
    private static final Logger logger = LogManager.getLogger();
    private final List<MethodResult> results;
    private String fileName;

    public SearchResultJsonExporter(List<MethodResult> results) {
        this.results = results;
    }

    @Override
    public boolean doExport() {
        if (results == null || results.isEmpty()) {
            logger.warn("no search results to export");
            return false;
        }

        ArrayList<Map<String, String>> exportData = new ArrayList<>();
        for (MethodResult result : results) {
            Map<String, String> item = new HashMap<>();
            item.put("class", result.getClassName());
            item.put("method", result.getMethodName());
            item.put("desc", result.getMethodDesc());
            exportData.add(item);
        }

        this.fileName = String.format("search-results-%d.json", System.currentTimeMillis());
        try {
            String jsonString = JSON.toJSONString(exportData, JSONWriter.Feature.PrettyFormat);
            Files.write(Paths.get(fileName), jsonString.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception ex) {
            logger.error("export search results to JSON failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
}
