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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class TxtExporter implements Exporter {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine engine;
    private String fileName;

    public TxtExporter() {
        this.engine = MainForm.getEngine();
    }

    @Override
    public boolean doExport() {
        if (this.engine == null) {
            return false;
        }
        ArrayList<ClassResult> crl = this.engine.getAllServlets();
        ArrayList<ClassResult> cfl = this.engine.getAllFilters();
        ArrayList<ClassResult> cll = this.engine.getAllListeners();
        ArrayList<ClassResult> cil = this.engine.getAllSpringI();
        ArrayList<ClassResult> csl = this.engine.getAllSpringC();
        this.fileName = String.format("jar-analyzer-%d.txt", System.currentTimeMillis());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
            writer.write("############# SERVLETS #############");
            writer.newLine();
            for (ClassResult cr : crl) {
                String className = cr.getClassName();
                writer.write(className);
                writer.newLine();
            }
            writer.write("####################################");
            writer.newLine();
            writer.write("############# FILTERS #############");
            writer.newLine();
            for (ClassResult cr : cfl) {
                String className = cr.getClassName();
                writer.write(className);
                writer.newLine();
            }
            writer.write("####################################");
            writer.newLine();
            writer.write("############# LISTENERS #############");
            writer.newLine();
            for (ClassResult cr : cll) {
                String className = cr.getClassName();
                writer.write(className);
                writer.newLine();
            }
            writer.write("####################################");
            writer.newLine();
            writer.write("############# INTERCEPTORS #############");
            writer.newLine();
            for (ClassResult cr : cil) {
                String className = cr.getClassName();
                writer.write(className);
                writer.newLine();
            }
            writer.write("####################################");
            writer.newLine();
            writer.write("############# CONTROLLERS #############");
            writer.newLine();
            for (ClassResult cr : csl) {
                String className = cr.getClassName();
                ArrayList<MethodResult> mrl = this.engine.getSpringM(className);
                writer.write(className);
                writer.newLine();
                for (MethodResult m : mrl) {
                    String methodName = m.getMethodName();
                    writer.write(methodName);
                    writer.write("\t");
                    writer.write(m.getRestfulType());
                    writer.write("\t");
                    writer.write(m.getPath());
                    writer.newLine();
                }
                writer.newLine();
            }
            writer.write("####################################");
            writer.newLine();
            writer.flush();
            writer.close();
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
