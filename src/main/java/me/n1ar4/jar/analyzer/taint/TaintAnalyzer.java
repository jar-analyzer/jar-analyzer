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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaintAnalyzer {
    private static final Logger logger = LogManager.getLogger();

    public static final Integer TAINT_FAIL = -1;

    public static void analyze(List<DFSResult> resultList) {
        CoreEngine engine = MainForm.getEngine();
        for (DFSResult result : resultList) {
            System.out.println("#####################################################");
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
                    logger.info("taint analyze finish");
                    if (pass.get() != TAINT_FAIL) {
                        logger.info("taint analyze pass");
                    }
                    break;
                }

                MethodReference.Handle m = methodList.get(i);
                MethodReference.Handle next = methodList.get(i + 1);

                String classOrigin = m.getClassReference().getName();
                classOrigin = classOrigin.replace(".", "/");
                String absPath = engine.getAbsPath(classOrigin);

                if (absPath == null || absPath.trim().isEmpty()) {
                    logger.warn("class not found: {}", m.getClassReference().getName());
                    break;
                }
                byte[] clsBytes;
                try {
                    clsBytes = Files.readAllBytes(Paths.get(absPath));
                } catch (Exception ex) {
                    logger.error("read file error: {}", ex.toString());
                    return;
                }

                String desc = m.getDesc();
                Type[] argumentTypes = Type.getArgumentTypes(desc);
                int paramCount = argumentTypes.length;

                logger.info("method: {} params count: {}", m.getName(), paramCount);

                if (pass.get() ==TAINT_FAIL) {
                    // 第一次开始
                    logger.info("start taint analyze no pass info");
                    // 遍历所有 source 的参数
                    // 认为所有参数都可能是 source
                    for (int k = 0; k < paramCount; k++) {
                        try {
                            logger.info("try method {} params: {}", m.getName(), k);
                            TaintClassVisitor tcv = new TaintClassVisitor(k, m, next, pass);
                            ClassReader cr = new ClassReader(clsBytes);
                            cr.accept(tcv, Const.AnalyzeASMOptions);
                            pass = tcv.getPass();
                            logger.info("pass return: {}", pass.get());
                            // 无法抵达第二个 chain 认为有问题
                            if (pass.get() != TAINT_FAIL) {
                                break;
                            }
                        } catch (Exception e) {
                            logger.error("discovery error: {}", e.toString());
                        }
                    }
                } else {
                    // 第二个 chain 开始
                    // 只要顺利 即可继续分析
                    try {
                        TaintClassVisitor tcv = new TaintClassVisitor(pass.get(), m, next, pass);
                        ClassReader cr = new ClassReader(clsBytes);
                        cr.accept(tcv, Const.AnalyzeASMOptions);
                        pass = tcv.getPass();
                        logger.info("pass return: {}", pass.get());
                    } catch (Exception e) {
                        logger.error("discovery error: {}", e.toString());
                    }
                    if (pass.get() ==TAINT_FAIL) {
                        break;
                    }
                }
            }
        }
    }
}
