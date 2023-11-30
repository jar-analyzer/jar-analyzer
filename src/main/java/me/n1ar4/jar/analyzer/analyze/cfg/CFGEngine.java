package me.n1ar4.jar.analyzer.analyze.cfg;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CFGEngine {
    private static final Logger logger = LogManager.getLogger();
    private final CoreEngine coreEngine;

    public CFGEngine() {
        this.coreEngine = MainForm.getEngine();
    }

    public String doAnalyze(String className,
                            String methodName,
                            String methodDesc) {
        StringBuilder globalBuilder = new StringBuilder();
        try {
            String absPath = this.coreEngine.getAbsPath(className);
            Path absPathPath = Paths.get(absPath);
            ControlFlowGraphEngine.start(Files.newInputStream(absPathPath),
                    methodName, methodDesc, globalBuilder);
        } catch (Exception e) {
            logger.error("discovery error: {}", e.toString());
        }
        return globalBuilder.toString();
    }
}
