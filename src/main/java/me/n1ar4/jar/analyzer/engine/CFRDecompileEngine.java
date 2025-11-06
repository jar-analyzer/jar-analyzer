/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * CFR Decompile Engine
 */
public class CFRDecompileEngine {
    public static final String INFO = "<html>" +
            "<b>CFR</b> - Another Java decompiler" +
            "</html>";
    private static final Logger logger = LogManager.getLogger();
    private static final String CFR_PREFIX = "//\n" +
            "// Jar Analyzer by 4ra1n\n" +
            "// (powered by CFR decompiler)\n" +
            "//\n";
    private static final LRUCache lruCache = new LRUCache(30);

    /**
     * 使用CFR反编译指定的class文件
     *
     * @param classFilePath class文件的绝对路径
     * @return 反编译后的Java源代码
     */
    public static String decompile(String classFilePath) {
        if (classFilePath == null || classFilePath.trim().isEmpty()) {
            logger.warn("class file path is null or empty");
            return null;
        }

        // 检查缓存
        String key = "cfr-" + classFilePath;
        String cached = lruCache.get(key);
        if (cached != null) {
            logger.debug("get from cache: " + classFilePath);
            return cached;
        }

        try {
            Path classPath = Paths.get(classFilePath);
            if (!Files.exists(classPath)) {
                logger.warn("class file not exists: " + classFilePath);
                return null;
            }

            // CFR反编译选项
            Map<String, String> options = new HashMap<>();
            options.put("showversion", "false");
            options.put("hidelongstrings", "false");
            options.put("hideutf", "false");
            options.put("innerclasses", "true");
            options.put("skipbatchinnerclasses", "false");

            // 创建输出收集器
            StringBuilder decompiledCode = new StringBuilder();
            OutputSinkFactory outputSinkFactory = new OutputSinkFactory() {
                @Override
                public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
                    if (sinkType == SinkType.JAVA && available.contains(SinkClass.DECOMPILED)) {
                        return Collections.singletonList(SinkClass.DECOMPILED);
                    }
                    return Collections.singletonList(SinkClass.STRING);
                }

                @Override
                public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
                    if (sinkType == SinkType.JAVA) {
                        if (sinkClass == SinkClass.DECOMPILED) {
                            return (T obj) -> {
                                SinkReturns.Decompiled decompiled = (SinkReturns.Decompiled) obj;
                                decompiledCode.append(decompiled.getJava());
                            };
                        } else if (sinkClass == SinkClass.STRING) {
                            return (T obj) -> decompiledCode.append(obj.toString());
                        }
                    }
                    return (T obj) -> {
                    };
                }
            };

            // 执行CFR反编译
            CfrDriver driver = new CfrDriver.Builder()
                    .withOptions(options)
                    .withOutputSink(outputSinkFactory)
                    .build();

            List<String> toAnalyse = Collections.singletonList(classFilePath);
            driver.analyse(toAnalyse);

            String result = decompiledCode.toString();
            if (!result.trim().isEmpty()) {
                // 添加前缀
                result = CFR_PREFIX + result;
                // 保存到缓存
                lruCache.put(key, result);
                logger.debug("cfr decompile success: " + classFilePath);
                return result;
            } else {
                logger.warn("cfr decompile result is empty: " + classFilePath);
                return null;
            }
        } catch (Exception ex) {
            logger.warn("cfr decompile fail: " + ex.getMessage());
            return null;
        }
    }

    /**
     * 检查CFR是否可用
     *
     * @return true if CFR is available
     */
    public static boolean isAvailable() {
        try {
            Class.forName("org.benf.cfr.reader.api.CfrDriver");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}