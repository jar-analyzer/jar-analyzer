package me.n1ar4.jar.analyzer.frame;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StackFrameEngine {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine coreEngine;

    public StackFrameEngine() {
        this.coreEngine = MainForm.getEngine();
    }

    public String doAnalyze(String className,
                            String methodName,
                            String methodDesc) {
        StringBuilder globalBuilder = new StringBuilder();
        try {
            String absPath = this.coreEngine.getAbsPath(className);
            Path absPathPath = Paths.get(absPath);
            FrameEngine.start(Files.newInputStream(absPathPath),
                    methodName, methodDesc, globalBuilder);
        } catch (Exception e) {
            logger.error("discovery error: {}", e.toString());
        }
        return globalBuilder.toString();
    }
}
