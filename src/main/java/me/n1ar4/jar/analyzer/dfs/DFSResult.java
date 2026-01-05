/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.dfs;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;

import java.util.List;

public class DFSResult {
    private List<MethodReference.Handle> methodList;
    private int depth;
    private MethodReference.Handle source;
    private MethodReference.Handle sink;
    private int mode;

    public static final int FROM_SOURCE_TO_SINK = 1;
    public static final int FROM_SINK_TO_SOURCE = 2;
    public static final int FROM_SOURCE_TO_ALL = 3;

    public List<MethodReference.Handle> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<MethodReference.Handle> methodList) {
        this.methodList = methodList;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public MethodReference.Handle getSource() {
        return source;
    }

    public void setSource(MethodReference.Handle source) {
        this.source = source;
    }

    public MethodReference.Handle getSink() {
        return sink;
    }

    public void setSink(MethodReference.Handle sink) {
        this.sink = sink;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "DFSResult{" +
                "methodList=" + methodList +
                ", depth=" + depth +
                ", source=" + source +
                ", sink=" + sink +
                ", mode=" + mode +
                '}';
    }
}
