package me.n1ar4.jar.analyzer.analyze.frame;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleStackFrameEngine {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine coreEngine;

    public SimpleStackFrameEngine() {
        this.coreEngine = MainForm.getEngine();
    }

    public String doAnalyze(String className,
                            String methodName,
                            String methodDesc) {
        StringBuilder globalBuilder = new StringBuilder();
        try {
            String absPath = this.coreEngine.getAbsPath(className);
            Path absPathPath = Paths.get(absPath);
            SimpleFrameEngine.start(Files.newInputStream(absPathPath),
                    methodName, methodDesc, globalBuilder);
        } catch (Exception e) {
            logger.error("discovery error: {}", e.toString());
        }
        return globalBuilder.toString();
    }
}
