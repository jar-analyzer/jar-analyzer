/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

public class DFSResultEntity {
    private String sourceClassName;
    private String sourceMethodName;
    private String sourceMethodDesc;
    private String sinkClassName;
    private String sinkMethodName;
    private String sinkMethodDesc;
    private int dfsDepth;
    private int dfsMode;
    private String dfsListUid;

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getSourceMethodDesc() {
        return sourceMethodDesc;
    }

    public void setSourceMethodDesc(String sourceMethodDesc) {
        this.sourceMethodDesc = sourceMethodDesc;
    }

    public String getSinkClassName() {
        return sinkClassName;
    }

    public void setSinkClassName(String sinkClassName) {
        this.sinkClassName = sinkClassName;
    }

    public String getSinkMethodName() {
        return sinkMethodName;
    }

    public void setSinkMethodName(String sinkMethodName) {
        this.sinkMethodName = sinkMethodName;
    }

    public String getSinkMethodDesc() {
        return sinkMethodDesc;
    }

    public void setSinkMethodDesc(String sinkMethodDesc) {
        this.sinkMethodDesc = sinkMethodDesc;
    }

    public int getDfsDepth() {
        return dfsDepth;
    }

    public void setDfsDepth(int dfsDepth) {
        this.dfsDepth = dfsDepth;
    }

    public int getDfsMode() {
        return dfsMode;
    }

    public void setDfsMode(int dfsMode) {
        this.dfsMode = dfsMode;
    }

    public String getDfsListUid() {
        return dfsListUid;
    }

    public void setDfsListUid(String dfsListUid) {
        this.dfsListUid = dfsListUid;
    }

    @Override
    public String toString() {
        return "DFSResultEntity{" +
                "sourceClassName='" + sourceClassName + '\'' +
                ", sourceMethodName='" + sourceMethodName + '\'' +
                ", sourceMethodDesc='" + sourceMethodDesc + '\'' +
                ", sinkClassName='" + sinkClassName + '\'' +
                ", sinkMethodName='" + sinkMethodName + '\'' +
                ", sinkMethodDesc='" + sinkMethodDesc + '\'' +
                ", dfsDepth=" + dfsDepth +
                ", dfsMode=" + dfsMode +
                ", dfsListUid='" + dfsListUid + '\'' +
                '}';
    }
}
