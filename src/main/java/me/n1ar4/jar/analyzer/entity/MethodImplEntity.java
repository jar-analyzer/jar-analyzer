/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

public class MethodImplEntity {
    private int implId;
    private String className;
    private String methodName;
    private String methodDesc;
    private String implClassName;

    public int getImplId() {
        return implId;
    }

    public void setImplId(int implId) {
        this.implId = implId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getImplClassName() {
        return implClassName;
    }

    public void setImplClassName(String implClassName) {
        this.implClassName = implClassName;
    }

    @Override
    public String toString() {
        return "MethodImplEntity{" +
                "implId=" + implId +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDesc='" + methodDesc + '\'' +
                ", implClassName='" + implClassName + '\'' +
                '}';
    }
}
