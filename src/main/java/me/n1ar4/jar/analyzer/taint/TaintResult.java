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

import me.n1ar4.jar.analyzer.dfs.DFSResult;

public class TaintResult {
    private DFSResult dfsResult;
    private String taintText;

    public DFSResult getDfsResult() {
        return dfsResult;
    }

    public void setDfsResult(DFSResult dfsResult) {
        this.dfsResult = dfsResult;
    }

    public String getTaintText() {
        return taintText;
    }

    public void setTaintText(String taintText) {
        this.taintText = taintText;
    }

    @Override
    public String toString() {
        return "TaintResult{" +
                "dfsResult=" + dfsResult +
                ", taintText='" + taintText + '\'' +
                '}';
    }
}
