package me.n1ar4.jar.analyzer.analyze.frame;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleStackFrameEngine {
    private static final Logger logger = LogManager.getLogger();

    public String doAnalyze(String absPath,
                            String methodName,
                            String methodDesc) {
        StringBuilder globalBuilder = new StringBuilder();
        try {
            Path absPathPath = Paths.get(absPath);
            SimpleFrameEngine.start(Files.newInputStream(absPathPath),
                    methodName, methodDesc, globalBuilder);
        } catch (Exception e) {
            logger.error("discovery error: {}", e.toString());
        }
        return globalBuilder.toString();
    }
}
