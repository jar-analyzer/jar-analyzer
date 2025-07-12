/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * `https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE`
 */

package me.n1ar4.jar.analyzer.dfs;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFSEngine {
    private static final Logger logger = LogManager.getLogger();

    private String sinkClass;
    private String sinkMethod;
    private String sinkDesc;
    private String sourceClass;
    private String sourceMethod;
    private String sourceDesc;

    private final CoreEngine engine;
    private final JTextArea resultArea;

    private final boolean fromSink;
    private final boolean searchNullSource;
    private final int depth;

    private int chainCount = 0;

    private void update(String msg) {
        this.resultArea.append(msg + "\n");
        this.resultArea.setCaretPosition(this.resultArea.getDocument().getLength());
    }

    public DFSEngine(
            JTextArea resultArea,
            boolean fromSink,
            boolean searchNullSource,
            int depth) {
        this.engine = MainForm.getEngine();
        this.resultArea = resultArea;
        this.fromSink = fromSink;
        this.searchNullSource = searchNullSource;
        this.depth = depth;
    }

    public void setSink(
            String sinkClass,
            String sinkMethod,
            String sinkDesc) {
        this.sinkClass = sinkClass;
        this.sinkMethod = sinkMethod;
        this.sinkDesc = sinkDesc;
    }

    public void setSource(
            String sourceClass,
            String sourceMethod,
            String sourceDesc) {
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.sourceDesc = sourceDesc;
    }

    public void doAnalyze() {
        if (this.fromSink) {
            if (this.sinkClass == null || this.sinkClass.trim().isEmpty()) {
                update("错误：SINK 分析模式不允许 SINK 为空");
                return;
            }
            if (!this.searchNullSource) {
                if (this.sourceClass == null || this.sourceClass.trim().isEmpty()) {
                    update("错误：SINK 分析模式 - 精确搜索 - 不允许 SOURCE 为空");
                    return;
                }
            }
        } else {
            if (this.searchNullSource) {
                update("错误：SOURCE 分析模式不允许选择 空 SOURCE 分析");
                return;
            }
            if (this.sourceClass == null || this.sourceClass.trim().isEmpty()) {
                update("错误：SOURCE 分析模式不允许 SOURCE 为空");
                return;
            }
        }
        logger.info("start chains dfs analyze");
        update("分析最大深度：" + depth);

        // 检查是否为查找所有 SOURCE 的模式
        boolean findAllSources = this.fromSink && this.searchNullSource;

        logger.info("find all sources from sink : " + findAllSources);

        if (findAllSources) {
            update("SINK: " + sinkClass + "." + sinkMethod);
            update("SOURCE: [查找所有可能的SOURCE点]");
        } else {
            update("SOURCE: " + sourceClass + "." + sourceMethod);
            update("SINK: " + sinkClass + "." + sinkMethod);
        }
        update("===========================================");

        chainCount = 0;

        if (this.fromSink) {
            if (findAllSources) {
                update("从 SINK 开始反向分析 查找所有可能的 SOURCE 点");
            } else {
                update("从 SINK 开始反向分析");
            }
            MethodResult startMethod = new MethodResult(sinkClass, sinkMethod, sinkDesc);
            List<MethodResult> path = new ArrayList<>();
            path.add(startMethod);
            Set<String> visited = new HashSet<>();

            if (findAllSources) {
                dfsFromSinkFindAllSources(startMethod, path, visited, 0);
            } else {
                dfsFromSink(startMethod, path, visited, 0);
            }
        } else {
            update("从 SOURCE 开始正向分析");
            MethodResult startMethod = new MethodResult(sourceClass, sourceMethod, sourceDesc);
            List<MethodResult> path = new ArrayList<>();
            path.add(startMethod);
            Set<String> visited = new HashSet<>();

            dfsFromSource(startMethod, path, visited, 0);
        }

        update("===========================================");
        if (findAllSources) {
            update("总共找到 " + chainCount + " 个可能的 SOURCE 点");
        } else {
            update("总共找到 " + chainCount + " 条可能的调用链");
        }
    }

    private void dfsFromSink(
            MethodResult currentMethod,
            List<MethodResult> path,
            Set<String> visited,
            int currentDepth) {
        if (currentDepth >= depth) {
            return;
        }

        String methodKey = getMethodKey(currentMethod);

        if (isTargetMethod(currentMethod, sourceClass, sourceMethod, sourceDesc)) {
            chainCount++;
            outputChain(path, true);
            return;
        }

        if (visited.contains(methodKey)) {
            return;
        }

        visited.add(methodKey);

        ArrayList<MethodResult> callerMethods = engine.getCallers(
                currentMethod.getClassName(),
                currentMethod.getMethodName(),
                currentMethod.getMethodDesc());

        for (MethodResult caller : callerMethods) {
            path.add(caller);
            dfsFromSink(caller, path, visited, currentDepth + 1);
            path.remove(path.size() - 1);
        }

        visited.remove(methodKey);
    }

    private void dfsFromSinkFindAllSources(
            MethodResult currentMethod,
            List<MethodResult> path,
            Set<String> visited,
            int currentDepth) {
        if (currentDepth >= depth) {
            return;
        }

        String methodKey = getMethodKey(currentMethod);

        if (visited.contains(methodKey)) {
            return;
        }

        visited.add(methodKey);

        ArrayList<MethodResult> callerMethods = engine.getCallers(
                currentMethod.getClassName(),
                currentMethod.getMethodName(),
                currentMethod.getMethodDesc());

        if (callerMethods.isEmpty()) {
            chainCount++;
            outputSourceChain(path, currentMethod);
        } else {
            for (MethodResult caller : callerMethods) {
                path.add(caller);
                dfsFromSinkFindAllSources(caller, path, visited, currentDepth + 1);
                path.remove(path.size() - 1);
            }
        }

        visited.remove(methodKey);
    }

    private void dfsFromSource(
            MethodResult currentMethod,
            List<MethodResult> path,
            Set<String> visited,
            int currentDepth) {
        if (currentDepth >= depth) {
            return;
        }

        String methodKey = getMethodKey(currentMethod);

        if (isTargetMethod(currentMethod, sinkClass, sinkMethod, sinkDesc)) {
            chainCount++;
            outputChain(path, false);
            return;
        }

        if (visited.contains(methodKey)) {
            return;
        }

        visited.add(methodKey);

        ArrayList<MethodResult> calleeMethods = engine.getCallee(
                currentMethod.getClassName(),
                currentMethod.getMethodName(),
                currentMethod.getMethodDesc());

        for (MethodResult callee : calleeMethods) {
            path.add(callee);
            dfsFromSource(callee, path, visited, currentDepth + 1);
            path.remove(path.size() - 1);
        }

        visited.remove(methodKey);
    }

    private String getMethodKey(MethodResult method) {
        return method.getClassName() + "." + method.getMethodName() + "." + method.getMethodDesc();
    }

    private boolean isTargetMethod(MethodResult method, String targetClass, String targetMethod, String targetDesc) {
        if (targetClass == null || targetMethod == null || targetDesc == null) {
            return false;
        }
        return method.getClassName().equals(targetClass) &&
                method.getMethodName().equals(targetMethod) &&
                method.getMethodDesc().equals(targetDesc);
    }

    private void outputChain(List<MethodResult> path, boolean isReverse) {
        update("\n发现调用链 #" + chainCount + ":");
        update("链长度: " + path.size());
        update("调用路径:");

        if (isReverse) {
            // 反向搜索时，路径需要反转输出
            for (int i = path.size() - 1; i >= 0; i--) {
                MethodResult method = path.get(i);
                String arrow = (i == 0) ? "" : " -> ";
                update(arrow + formatMethod(method));
            }
        } else {
            // 正向搜索时，直接输出
            for (int i = 0; i < path.size(); i++) {
                MethodResult method = path.get(i);
                String arrow = (i == 0) ? "" : " -> ";
                update(arrow + formatMethod(method));
            }
        }
        update("");
    }

    private void outputSourceChain(List<MethodResult> path, MethodResult sourceMethod) {
        update("\n发现 SOURCE 点 #" + chainCount + ":");
        update("SOURCE: " + formatMethod(sourceMethod));
        update("到达 SINK 的调用链长度: " + path.size());
        update("调用路径:");

        // 反向搜索时，路径需要反转输出（从SOURCE到SINK）
        for (int i = path.size() - 1; i >= 0; i--) {
            MethodResult method = path.get(i);
            String arrow = (i == path.size() - 1) ? "" : " -> ";
            update(arrow + formatMethod(method));
        }
        update("");
    }

    private String formatMethod(MethodResult method) {
        return method.getClassName() + "." + method.getMethodName() + method.getMethodDesc();
    }
}