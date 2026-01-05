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

public class DFSResultListEntity {
    private String dfsListUid;
    private int dfsListIndex;
    private String dfsClassName;
    private String dfsMethodName;
    private String dfsMethodDesc;

    public String getDfsListUid() {
        return dfsListUid;
    }

    public void setDfsListUid(String dfsListUid) {
        this.dfsListUid = dfsListUid;
    }

    public int getDfsListIndex() {
        return dfsListIndex;
    }

    public void setDfsListIndex(int dfsListIndex) {
        this.dfsListIndex = dfsListIndex;
    }

    public String getDfsClassName() {
        return dfsClassName;
    }

    public void setDfsClassName(String dfsClassName) {
        this.dfsClassName = dfsClassName;
    }

    public String getDfsMethodName() {
        return dfsMethodName;
    }

    public void setDfsMethodName(String dfsMethodName) {
        this.dfsMethodName = dfsMethodName;
    }

    public String getDfsMethodDesc() {
        return dfsMethodDesc;
    }

    public void setDfsMethodDesc(String dfsMethodDesc) {
        this.dfsMethodDesc = dfsMethodDesc;
    }

    @Override
    public String toString() {
        return "DFSResultListEntity{" +
                "dfsListUid='" + dfsListUid + '\'' +
                ", dfsListIndex='" + dfsListIndex + '\'' +
                ", dfsClassName='" + dfsClassName + '\'' +
                ", dfsMethodName='" + dfsMethodName + '\'' +
                ", dfsMethodDesc='" + dfsMethodDesc + '\'' +
                '}';
    }
}
