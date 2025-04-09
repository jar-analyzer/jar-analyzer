/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.exporter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonExporter implements Exporter {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine engine;
    private String fileName;

    public JsonExporter() {
        this.engine = MainForm.getEngine();
    }

    @Override
    public boolean doExport() {
        ArrayList<ClassResult> servlets = this.engine.getAllServlets();
        ArrayList<ClassResult> filters = this.engine.getAllFilters();
        ArrayList<ClassResult> listeners = this.engine.getAllListeners();
        ArrayList<ClassResult> interceptors = this.engine.getAllSpringI();
        ArrayList<ClassResult> controllers = this.engine.getAllSpringC();

        Map<String, Object> exportData = new HashMap<>();
        exportData.put("servlets", servlets);
        exportData.put("filters", filters);
        exportData.put("listeners", listeners);
        exportData.put("interceptors", interceptors);

        ArrayList<Map<String, Object>> controllersList = new ArrayList<>();
        for (ClassResult cr : controllers) {
            Map<String, Object> controllerMap = new HashMap<>();
            controllerMap.put("className", cr.getClassName());
            controllerMap.put("methods", this.engine.getSpringM(cr.getClassName()));
            controllersList.add(controllerMap);
        }
        exportData.put("controllers", controllersList);

        this.fileName = String.format("jar-analyzer-%d.json", System.currentTimeMillis());
        try {
            String jsonString = JSON.toJSONString(exportData, JSONWriter.Feature.PrettyFormat);
            Files.write(Paths.get(fileName), jsonString.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception ex) {
            logger.error("export error : " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
}
