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

public class MethodCallEntity {
    private int mcId;
    private String callerClassName;
    private String callerMethodName;
    private String callerMethodDesc;
    private String calleeClassName;
    private String calleeMethodName;
    private String calleeMethodDesc;
    private Integer callerJarId;
    private Integer calleeJarId;
    private Integer opCode;

    public int getMcId() {
        return mcId;
    }

    public void setMcId(int mcId) {
        this.mcId = mcId;
    }

    public String getCallerClassName() {
        return callerClassName;
    }

    public void setCallerClassName(String callerClassName) {
        this.callerClassName = callerClassName;
    }

    public String getCallerMethodName() {
        return callerMethodName;
    }

    public void setCallerMethodName(String callerMethodName) {
        this.callerMethodName = callerMethodName;
    }

    public String getCallerMethodDesc() {
        return callerMethodDesc;
    }

    public void setCallerMethodDesc(String callerMethodDesc) {
        this.callerMethodDesc = callerMethodDesc;
    }

    public String getCalleeClassName() {
        return calleeClassName;
    }

    public void setCalleeClassName(String calleeClassName) {
        this.calleeClassName = calleeClassName;
    }

    public String getCalleeMethodName() {
        return calleeMethodName;
    }

    public void setCalleeMethodName(String calleeMethodName) {
        this.calleeMethodName = calleeMethodName;
    }

    public String getCalleeMethodDesc() {
        return calleeMethodDesc;
    }

    public void setCalleeMethodDesc(String calleeMethodDesc) {
        this.calleeMethodDesc = calleeMethodDesc;
    }

    public Integer getCallerJarId() {
        return callerJarId;
    }

    public void setCallerJarId(Integer callerJarId) {
        this.callerJarId = callerJarId;
    }

    public Integer getCalleeJarId() {
        return calleeJarId;
    }

    public void setCalleeJarId(Integer calleeJarId) {
        this.calleeJarId = calleeJarId;
    }

    public void setOpCode(Integer opCode) {
        this.opCode = opCode;
    }

    public Integer getOpCode() {
        return opCode;
    }

    @Override
    public String toString() {
        return "MethodCallEntity{" +
                "mcId=" + mcId +
                ", callerClassName='" + callerClassName + '\'' +
                ", callerMethodName='" + callerMethodName + '\'' +
                ", callerMethodDesc='" + callerMethodDesc + '\'' +
                ", calleeClassName='" + calleeClassName + '\'' +
                ", calleeMethodName='" + calleeMethodName + '\'' +
                ", calleeMethodDesc='" + calleeMethodDesc + '\'' +
                ", callerJarId=" + callerJarId +
                ", calleeJarId=" + calleeJarId +
                ", opCode=" + opCode +
                '}';
    }
}
