/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaintAnalyzer {
    private static final Logger logger = LogManager.getLogger();

    public static final Integer TAINT_FAIL = -1;
    public static final String TAINT = "TAINT";

    @SuppressWarnings("all")
    public static List<TaintResult> analyze(List<DFSResult> resultList) {
        List<TaintResult> taintResult = new ArrayList<>();

        InputStream sin = TaintAnalyzer.class.getClassLoader().getResourceAsStream("sanitizer.json");
        SanitizerRule rule = SanitizerRule.loadJSON(sin);
        logger.info("污点分析加载 sanitizer 规则数量：{}", rule.getRules().size());

        CoreEngine engine = MainForm.getEngine();
        for (DFSResult result : resultList) {
            boolean thisChainSuccess = false;
            StringBuilder text = new StringBuilder();
            System.out.println("####################### 污点分析进行中 #######################");
            text.append("####################### 污点分析进行中 #######################");
            text.append("\n");
            List<MethodReference.Handle> methodList = result.getMethodList();

            // 上一个方法调用 污点传递到第几个参数
            // ！！关键！！
            // 方法之间 数据流/污点传播 完全靠该字段实现
            AtomicInteger pass = new AtomicInteger(TAINT_FAIL);

            // 遍历 chains
            for (int i = 0; i < methodList.size(); i++) {
                // 不分析最后一个 chain
                // 因为最后一个一般是 jdk 的 sink
                // 但是用户很可能不加载 jdk 的东西
                // 如果只要上一个可以到达最后一个
                // 即可认为污点分析成功
                if (i == methodList.size() - 1) {
                    logger.info("污点分析执行结束");
                    text.append("污点分析执行结束");
                    text.append("\n");
                    if (pass.get() != TAINT_FAIL) {
                        thisChainSuccess = true;
                        logger.info("该链污点分析结果：通过");
                        text.append("该链污点分析结果：通过");
                        text.append("\n");
                    }
                    break;
                }

                MethodReference.Handle m = methodList.get(i);
                MethodReference.Handle next = methodList.get(i + 1);

                String classOrigin = m.getClassReference().getName();
                classOrigin = classOrigin.replace(".", "/");
                String absPath = engine.getAbsPath(classOrigin);

                if (absPath == null || absPath.trim().isEmpty()) {
                    logger.warn("污点分析找不到类: {}", m.getClassReference().getName());
                    break;
                }
                byte[] clsBytes;
                try {
                    clsBytes = Files.readAllBytes(Paths.get(absPath));
                } catch (Exception ex) {
                    logger.error("污点分析读文件错误: {}", ex.toString());
                    return new ArrayList<>();
                }

                String desc = m.getDesc();
                Type[] argumentTypes = Type.getArgumentTypes(desc);
                int paramCount = argumentTypes.length;

                logger.info("方法: {} 参数数量: {}", m.getName(), paramCount);
                text.append(String.format("方法: %s 参数数量: %d", m.getName(), paramCount));
                text.append("\n");

                if (pass.get() == TAINT_FAIL) {
                    // 第一次开始
                    logger.info("开始污点分析 - 链开始 - 无数据流");
                    text.append("开始污点分析 - 链开始 - 无数据流");
                    text.append("\n");
                    // 遍历所有 source 的参数
                    // 认为所有参数都可能是 source
                    for (int k = 0; k < paramCount; k++) {
                        try {
                            logger.info("开始分析方法 {} 第 {} 个参数", m.getName(), k);
                            text.append(String.format("开始分析方法 %s 第 %d 个参数", m.getName(), k));
                            text.append("\n");
                            TaintClassVisitor tcv = new TaintClassVisitor(k, m, next, pass, rule,text);
                            ClassReader cr = new ClassReader(clsBytes);
                            cr.accept(tcv, Const.AnalyzeASMOptions);
                            pass = tcv.getPass();
                            logger.info("数据流结果 - 传播到第 {} 个参数", pass.get());
                            text.append(String.format("数据流结果 - 传播到第 %d 个参数", pass.get()));
                            text.append("\n");
                            // 无法抵达第二个 chain 认为有问题
                            if (pass.get() != TAINT_FAIL) {
                                break;
                            }
                        } catch (Exception e) {
                            logger.error("污点分析 - 链开始 - 错误: {}", e.toString());
                        }
                    }
                } else {
                    // 第二个 chain 开始
                    // 只要顺利 即可继续分析
                    try {
                        TaintClassVisitor tcv = new TaintClassVisitor(pass.get(), m, next, pass, rule, text);
                        ClassReader cr = new ClassReader(clsBytes);
                        cr.accept(tcv, Const.AnalyzeASMOptions);
                        pass = tcv.getPass();
                        logger.info("数据流结果 - 传播到第 {} 个参数", pass.get());
                        text.append(String.format("数据流结果 - 传播到第 %d 个参数", pass.get()));
                        text.append("\n");
                    } catch (Exception e) {
                        logger.error("污点分析 - 链中 - 错误: {}", e.toString());
                    }
                    if (pass.get() == TAINT_FAIL) {
                        break;
                    }
                }
            }

            if (thisChainSuccess) {
                TaintResult r = new TaintResult();
                r.setDfsResult(result);
                r.setTaintText(text.toString());
                taintResult.add(r);
            }
        }

        return taintResult;
    }
}
