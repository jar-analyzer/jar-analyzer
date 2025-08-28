/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.dfs;

import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.ChainsResultPanel;
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
    private final Object resultArea; // 可以是JTextArea或ChainsResultPanel

    private final boolean fromSink;
    private final boolean searchNullSource;
    private final int depth;

    private int chainCount = 0;
    private int sourceCount = 0;

    // 新增：结果收集列表
    private final List<DFSResult> results = new ArrayList<>();

    /**
     * 更新结果显示区域
     */
    private void update(String msg) {
        if (resultArea instanceof JTextArea) {
            JTextArea textArea = (JTextArea) resultArea;
            textArea.append(msg + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } else if (resultArea instanceof ChainsResultPanel) {
            ChainsResultPanel panel = (ChainsResultPanel) resultArea;
            panel.append(msg);
        }
    }

    /**
     * 添加调用链到结果面板
     */
    private void addChain(String chainId, String title, List<String> methods) {
        if (resultArea instanceof ChainsResultPanel) {
            ChainsResultPanel panel = (ChainsResultPanel) resultArea;
            panel.addChain(chainId, title, methods);
        } else {
            // 对于JTextArea，保持原有的输出方式
            update(title);
            for (int i = 0; i < methods.size(); i++) {
                String method = methods.get(i);
                String arrow = (i == 0) ? "" : " -> ";
                update(arrow + method);
            }
            update("");
        }
    }

    public DFSEngine(
            Object resultArea,
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
        sourceCount = 0;

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
            update("总共找到 " + sourceCount + " 个可能的 SOURCE 点");
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

    /**
     * 将 MethodResult 转换为 MethodReference.Handle
     */
    private MethodReference.Handle convertToHandle(MethodResult method) {
        ClassReference.Handle classHandle = new ClassReference.Handle(method.getClassName());
        return new MethodReference.Handle(classHandle, method.getMethodName(), method.getMethodDesc());
    }

    /**
     * 将 MethodResult 路径转换为 MethodReference.Handle 列表
     */
    private List<MethodReference.Handle> convertPathToHandles(List<MethodResult> path, boolean isReverse) {
        List<MethodReference.Handle> handles = new ArrayList<>();

        if (isReverse) {
            // 反向搜索时，路径需要反转
            for (int i = path.size() - 1; i >= 0; i--) {
                handles.add(convertToHandle(path.get(i)));
            }
        } else {
            // 正向搜索时，直接转换
            for (MethodResult method : path) {
                handles.add(convertToHandle(method));
            }
        }

        return handles;
    }

    private void outputChain(List<MethodResult> path, boolean isReverse) {
        chainCount++;
        String chainId = "chain_" + chainCount;
        String title = "调用链 #" + chainCount + " (长度: " + path.size() + ")";

        List<String> methods = new ArrayList<>();

        if (isReverse) {
            // 反向搜索时，路径需要反转输出
            for (int i = path.size() - 1; i >= 0; i--) {
                MethodResult method = path.get(i);
                methods.add(formatMethod(method));
            }
        } else {
            // 正向搜索时，直接输出
            for (MethodResult method : path) {
                methods.add(formatMethod(method));
            }
        }

        addChain(chainId, title, methods);

        // 新增：保存结果到 DFSResult
        DFSResult result = new DFSResult();
        result.setMethodList(convertPathToHandles(path, isReverse));
        result.setDepth(path.size());

        // 设置模式
        if (fromSink) {
            if (searchNullSource) {
                result.setMode(DFSResult.FROM_SOURCE_TO_ALL);
            } else {
                result.setMode(DFSResult.FROM_SINK_TO_SOURCE);
            }
        } else {
            result.setMode(DFSResult.FROM_SOURCE_TO_SINK);
        }

        // 设置 source 和 sink
        if (!path.isEmpty()) {
            if (isReverse) {
                // 反向搜索：路径的最后一个是source，第一个是sink
                result.setSource(convertToHandle(path.get(path.size() - 1)));
                result.setSink(convertToHandle(path.get(0)));
            } else {
                // 正向搜索：路径的第一个是source，最后一个是sink
                result.setSource(convertToHandle(path.get(0)));
                result.setSink(convertToHandle(path.get(path.size() - 1)));
            }
        }

        results.add(result);
    }

    private void outputSourceChain(List<MethodResult> path, MethodResult sourceMethod) {
        sourceCount++;
        String chainId = "source_" + sourceCount;
        String title = " (调用链长度: " + path.size() + ") #" + sourceCount + ": " + formatMethod(sourceMethod);

        List<String> methods = new ArrayList<>();

        // 反向搜索时，路径需要反转输出（从SOURCE到SINK）
        for (int i = path.size() - 1; i >= 0; i--) {
            MethodResult method = path.get(i);
            methods.add(formatMethod(method));
        }

        addChain(chainId, title, methods);

        // 新增：保存结果到 DFSResult
        DFSResult result = new DFSResult();
        result.setMethodList(convertPathToHandles(path, true)); // 反向输出
        result.setDepth(path.size());
        result.setMode(DFSResult.FROM_SOURCE_TO_ALL);

        // 设置 source 和 sink
        if (!path.isEmpty()) {
            result.setSource(convertToHandle(sourceMethod));
            result.setSink(convertToHandle(path.get(0))); // sink 是路径的起点
        }

        results.add(result);
    }

    private String formatMethod(MethodResult method) {
        return method.getClassName() + "." + method.getMethodName() + method.getMethodDesc();
    }

    /**
     * 获取所有分析结果
     *
     * @return DFSResult 列表
     */
    public List<DFSResult> getResults() {
        return new ArrayList<>(results);
    }

    /**
     * 清空结果列表
     */
    public void clearResults() {
        results.clear();
        chainCount = 0;
        sourceCount = 0;
    }

    /**
     * 获取结果数量
     *
     * @return 找到的调用链数量
     */
    public int getResultCount() {
        return results.size();
    }
}