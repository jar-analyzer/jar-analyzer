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

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.util.ArrayList;

public class CsvExporter implements Exporter {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine engine;
    private String fileName;

    public CsvExporter() {
        this.engine = MainForm.getEngine();
    }

    @Override
    public boolean doExport() {
        this.fileName = String.format("jar-analyzer-%d.csv", System.currentTimeMillis());
        try (FileWriter out = new FileWriter(fileName);
             CSVPrinter printer = new CSVPrinter(out,
                     CSVFormat.DEFAULT.builder().setHeader("Type", "ClassName",
                             "MethodName", "RestfulType", "Path").get())) {
            for (ClassResult cr : this.engine.getAllServlets()) {
                printer.printRecord("Servlet", cr.getClassName(), "", "", "");
            }
            for (ClassResult cr : this.engine.getAllFilters()) {
                printer.printRecord("Filter", cr.getClassName(), "", "", "");
            }
            for (ClassResult cr : this.engine.getAllListeners()) {
                printer.printRecord("Listener", cr.getClassName(), "", "", "");
            }
            for (ClassResult cr : this.engine.getAllSpringI()) {
                printer.printRecord("Interceptor", cr.getClassName(), "", "", "");
            }
            for (ClassResult cr : this.engine.getAllSpringC()) {
                String className = cr.getClassName();
                ArrayList<MethodResult> methods = this.engine.getSpringM(className);
                if (methods.isEmpty()) {
                    printer.printRecord("Controller", className, "", "", "");
                } else {
                    for (MethodResult method : methods) {
                        printer.printRecord("Controller", className,
                                method.getMethodName(),
                                method.getRestfulType(),
                                method.getActualPath());
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            logger.error("export error : " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }
}
