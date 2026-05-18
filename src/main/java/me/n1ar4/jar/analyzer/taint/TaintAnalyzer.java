/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
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
import me.n1ar4.jar.analyzer.utils.StackMapFrameHandler;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TaintAnalyzer {
    private static final Logger logger = LogManager.getLogger();

    /**
     * 历史保留：标记本字段不变以避免外部依赖断裂。
     */
    public static final Integer TAINT_FAIL = -1;
    public static final String TAINT = "TAINT";

    @SuppressWarnings("all")
    public static List<TaintResult> analyze(List<DFSResult> resultList) {
        List<TaintResult> taintResult = new ArrayList<>();

        InputStream sin = TaintAnalyzer.class.getClassLoader().getResourceAsStream("sanitizer.json");
        SanitizerRule rule = SanitizerRule.loadJSON(sin);
        logger.info("taint analysis loaded sanitizer rules count: {}", rule.getRules().size());

        InputStream pin = TaintAnalyzer.class.getClassLoader().getResourceAsStream("propagation.json");
        PropagationRuleSet propagation = PropagationRuleSet.loadJSON(pin);
        logger.info("taint analysis loaded propagation rules count: {}", propagation.getRules().size());

        CoreEngine engine = MainForm.getEngine();
        for (DFSResult result : resultList) {
            boolean thisChainSuccess = false;
            TaintEventSink sink = new TaintEventSink();
            sink.emit(TaintEvent.of(TaintEvent.Type.CHAIN_START, "开始分析新调用链"));

            List<MethodReference.Handle> methodList = result.getMethodList();

            // 跨方法污点载荷。
            // 旧实现是 AtomicInteger pass = -1，现升级为 TaintTransfer：
            //   - 支持同时污染多个 callee 的 locals 槽（含 this）
            //   - 携带返回值是否带污点的标记
            TaintTransfer transfer = new TaintTransfer();

            // 遍历 chains
            for (int i = 0; i < methodList.size(); i++) {
                // 不分析最后一个 chain（通常是 jdk sink，类不在工程内）
                if (i == methodList.size() - 1) {
                    if (transfer.hasTaint()) {
                        thisChainSuccess = true;
                        sink.emit(TaintEvent.of(TaintEvent.Type.CHAIN_PASS, "该链污点分析结果：通过"));
                    } else {
                        sink.emit(TaintEvent.of(TaintEvent.Type.CHAIN_FAIL, "该链污点分析结果：未通过"));
                    }
                    break;
                }

                MethodReference.Handle m = methodList.get(i);
                MethodReference.Handle next = methodList.get(i + 1);

                String classOrigin = m.getClassReference().getName();
                classOrigin = classOrigin.replace(".", "/");
                String absPath = engine.getAbsPath(classOrigin);

                if (absPath == null || absPath.trim().isEmpty()) {
                    sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, i,
                            m.getClassReference().getName(), m.getName(), m.getDesc(),
                            "类文件未找到，链路中断"));
                    break;
                }
                byte[] clsBytes;
                try {
                    clsBytes = Files.readAllBytes(Paths.get(absPath));
                } catch (Exception ex) {
                    sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, i,
                            m.getClassReference().getName(), m.getName(), m.getDesc(),
                            "读取类文件失败：" + ex));
                    return new ArrayList<>();
                }

                String desc = m.getDesc();
                Type[] argumentTypes = Type.getArgumentTypes(desc);
                int paramCount = argumentTypes.length;

                if (i == 0) {
                    // 链首：穷举每个形参作为 source 进行尝试。
                    sink.emit(TaintEvent.atMethod(TaintEvent.Type.INFO, 0,
                            m.getClassReference().getName(), m.getName(), m.getDesc(),
                            "链首方法，参数数量 " + paramCount + "（穷举每个参数作为 source）"));

                    boolean reached = false;
                    for (int paramSeq = 0; paramSeq < paramCount && !reached; paramSeq++) {
                        // 先尝试"非 static 视角"（locals[0]=this，所以 arg = paramSeq+1）
                        // 再尝试"static 视角"（arg = paramSeq）
                        for (int viewStart = 1; viewStart >= 0 && !reached; viewStart--) {
                            TaintTransfer entry = new TaintTransfer();
                            entry.markLocal(viewStart + paramSeq);
                            TaintTransfer exit = new TaintTransfer();

                            sink.emit(TaintEvent.atMethod(TaintEvent.Type.SOURCE_TRY, 0,
                                    m.getClassReference().getName(), m.getName(), m.getDesc(),
                                    "尝试 source：第 " + paramSeq + " 个参数（locals 索引 " + (viewStart + paramSeq) + "）"));

                            try {
                                TaintClassVisitor tcv = new TaintClassVisitor(entry, m, next, exit, rule, propagation, sink, 0);
                                ClassReader cr = new ClassReader(clsBytes);
                                cr.accept(tcv, Const.AnalyzeASMOptions);
                                exit = tcv.getExit();
                            } catch (IndexOutOfBoundsException e) {
                                TaintClassVisitor tcv = new TaintClassVisitor(entry, m, next, exit, rule, propagation, sink, 0);
                                if (StackMapFrameHandler.handleParseException(clsBytes, tcv,
                                        absPath + "!" + m.getClassReference().getName(),
                                        logger, "taint analysis chain start", e)) {
                                    exit = tcv.getExit();
                                } else {
                                    sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, 0,
                                            m.getClassReference().getName(), m.getName(), m.getDesc(),
                                            "链首分析异常：" + e));
                                }
                            } catch (Exception e) {
                                sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, 0,
                                        m.getClassReference().getName(), m.getName(), m.getDesc(),
                                        "链首分析异常：" + e));
                            }

                            if (exit != null && exit.hasTaint()) {
                                transfer = exit;
                                reached = true;
                                sink.emit(TaintEvent.atMethod(TaintEvent.Type.INFO, 0,
                                        m.getClassReference().getName(), m.getName(), m.getDesc(),
                                        "链首到达下一跳：" + transfer));
                            }
                        }
                    }

                    if (!reached) {
                        sink.emit(TaintEvent.atMethod(TaintEvent.Type.CHAIN_FAIL, 0,
                                m.getClassReference().getName(), m.getName(), m.getDesc(),
                                "链首无任何参数可达下一跳，结束分析"));
                        break;
                    }
                } else {
                    // 链中 / 链尾前一跳
                    TaintTransfer entry = transfer;
                    TaintTransfer exit = new TaintTransfer();
                    try {
                        TaintClassVisitor tcv = new TaintClassVisitor(entry, m, next, exit, rule, propagation, sink, i);
                        ClassReader cr = new ClassReader(clsBytes);
                        cr.accept(tcv, Const.AnalyzeASMOptions);
                        exit = tcv.getExit();
                    } catch (IndexOutOfBoundsException e) {
                        TaintClassVisitor tcv = new TaintClassVisitor(entry, m, next, exit, rule, propagation, sink, i);
                        if (StackMapFrameHandler.handleParseException(clsBytes, tcv,
                                absPath + "!" + m.getClassReference().getName(),
                                logger, "taint analysis chain middle", e)) {
                            exit = tcv.getExit();
                        } else {
                            sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, i,
                                    m.getClassReference().getName(), m.getName(), m.getDesc(),
                                    "链中分析异常：" + e));
                        }
                    } catch (Exception e) {
                        sink.emit(TaintEvent.atMethod(TaintEvent.Type.WARN, i,
                                m.getClassReference().getName(), m.getName(), m.getDesc(),
                                "链中分析异常：" + e));
                    }

                    if (exit == null || !exit.hasTaint()) {
                        sink.emit(TaintEvent.atMethod(TaintEvent.Type.CHAIN_FAIL, i,
                                m.getClassReference().getName(), m.getName(), m.getDesc(),
                                "本步未将污点传播至下一跳，链路中断"));
                        break;
                    }
                    transfer = exit;
                }
            }

            TaintResult r = new TaintResult();
            r.setDfsResult(result);
            r.setSuccess(thisChainSuccess);
            r.setTaintText(sink.getText());
            r.setEvents(sink.getEvents());
            r.setBadge(sink.shortBadge());
            taintResult.add(r);
        }

        return taintResult;
    }
}
