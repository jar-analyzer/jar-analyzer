/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gadget;

import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GadgetAnalyzer {
    private static final Logger logger = LogManager.getLogger();
    private final String dir;
    private final boolean enableNative;
    private final boolean enableHessian;
    private final boolean enableFastjson;
    private final boolean enableJdbc;

    public GadgetAnalyzer(String dir, boolean enableN, boolean enableH, boolean enableF, boolean enableJ) {
        this.dir = dir;
        this.enableNative = enableN;
        this.enableHessian = enableH;
        this.enableFastjson = enableF;
        this.enableJdbc = enableJ;
    }

    public List<GadgetInfo> process() {
        logger.info("start gadget analyzer");
        logger.info("n -> {} h -> {} f -> {} j -> {}",
                this.enableNative, this.enableHessian, this.enableFastjson, this.enableJdbc);
        List<String> files = DirUtil.GetFiles(this.dir);
        if (files == null || files.isEmpty()) {
            logger.warn("no files found");
            return null;
        }
        List<Path> exiFiles = new ArrayList<>();
        for (String file : files) {
            Path tmp = Paths.get(file);
            if (Files.exists(tmp)) {
                exiFiles.add(tmp);
            }
        }
        List<String> finalFiles = new ArrayList<>();
        for (Path exiFile : exiFiles) {
            String filename = exiFile.toFile().getName();
            if (!filename.endsWith(".jar")) {
                continue;
            }
            finalFiles.add(filename);
        }
        List<GadgetInfo> result = new ArrayList<>();
        // 匹配分析
        for (GadgetInfo rule : GadgetRule.rules) {
            String ruleType = rule.getType();
            if (ruleType.equals(GadgetInfo.NATIVE_TYPE)) {
                if (!this.enableNative) {
                    continue;
                }
            }
            if (ruleType.equals(GadgetInfo.HESSIAN_TYPE)) {
                if (!this.enableHessian) {
                    continue;
                }
            }
            if (ruleType.equals(GadgetInfo.FASTJSON_TYPE)) {
                if (!this.enableFastjson) {
                    continue;
                }
            }
            if (ruleType.equals(GadgetInfo.JDBC_TYPE)) {
                if (!this.enableJdbc) {
                    continue;
                }
            }
            logger.info("processing rule : " + rule.getJarsName());
            List<String> jarsName = rule.getJarsName();
            boolean[] successArray = new boolean[jarsName.size()];
            for (int i = 0; i < successArray.length; i++) {
                String jarName = jarsName.get(i);
                if (jarName.contains("!")) {
                    String temp = jarName.split("!")[0];
                    String whiteList = jarName.split("!")[1].split("\\.jar")[0];
                    for (String exiFileName : finalFiles) {
                        if (exiFileName.startsWith(temp)) {
                            String ver = exiFileName.split(temp)[1].split("\\.jar")[0];
                            if (!ver.equals(whiteList)) {
                                successArray[i] = true;
                                break;
                            }
                        }
                    }
                } else {
                    if (!jarName.contains("*")) {
                        for (String exiFileName : finalFiles) {
                            if (jarName.equals(exiFileName)) {
                                successArray[i] = true;
                                break;
                            }
                        }
                    } else {
                        String regex = jarName.replace("*", ".*");
                        for (String fileName : finalFiles) {
                            if (fileName.matches(regex)) {
                                successArray[i] = true;
                                break;
                            }
                        }
                    }
                }
            }
            boolean success = true;
            for (boolean b : successArray) {
                if (!b) {
                    success = false;
                    break;
                }
            }
            if (success) {
                result.add(rule);
            }
        }
        // 补充输出
        if (this.enableNative) {

        }
        if (this.enableHessian) {

        }
        if (this.enableFastjson) {

        }
        if (this.enableJdbc) {

        }
        return result;
    }
}
