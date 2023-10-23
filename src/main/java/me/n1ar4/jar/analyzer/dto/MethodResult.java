package me.n1ar4.jar.analyzer.dto;

import me.n1ar4.jar.analyzer.utils.ASMUtil;

public class MethodResult {
    private String className;
    private String jarName;
    private String methodName;
    private String methodDesc;
    private int isStaticInt;
    private int accessInt;

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
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

    public int getIsStaticInt() {
        return isStaticInt;
    }

    public void setIsStaticInt(int isStaticInt) {
        this.isStaticInt = isStaticInt;
    }

    public int getAccessInt() {
        return accessInt;
    }

    public void setAccessInt(int accessInt) {
        this.accessInt = accessInt;
    }

    @Override
    public String toString() {
        return "<html>" + ASMUtil.convertMethodDesc(
                getMethodName(), getMethodDesc()) + "</html>";
    }
}
