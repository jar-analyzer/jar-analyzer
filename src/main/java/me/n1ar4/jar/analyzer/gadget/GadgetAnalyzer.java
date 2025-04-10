/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
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

    public GadgetAnalyzer(String dir) {
        this.dir = dir;
    }

    public List<GadgetInfo> process() {
        logger.info("start gadget analyzer");
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
            logger.info("processing rule : " + rule.getJarsName());
            List<String> jarsName = rule.getJarsName();
            boolean[] successArray = new boolean[jarsName.size()];
            for (int i = 0; i < successArray.length; i++) {
                String jarName = jarsName.get(i);
                for (String exiFileName : finalFiles) {
                    if (jarName.equals(exiFileName)) {
                        successArray[i] = true;
                        break;
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
        return result;
    }
}
