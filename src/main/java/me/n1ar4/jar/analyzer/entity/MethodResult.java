package me.n1ar4.jar.analyzer.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import java.nio.file.Path;

public class MethodResult {
    private String className;
    private String jarName;
    private String methodName;
    private String methodDesc;
    private int isStaticInt;
    private int accessInt;
    @JSONField(serialize = false)
    private Path classPath;
    // for spring
    @JSONField(serialize = false)
    private String path;

    public String getPath() {
        this.path = this.path.trim();
        if (this.path.isEmpty()) {
            return "path: none";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "path: " + path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getClassPath() {
        return classPath;
    }

    public void setClassPath(Path classPath) {
        this.classPath = classPath;
    }

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
