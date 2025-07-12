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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MethodResult {
    private String className;
    private String jarName;
    private String methodName;
    private String methodDesc;
    private String restfulType;

    private int isStaticInt;
    private int accessInt;
    @JSONField(serialize = false)
    private Path classPath;
    // for spring
    @JSONField(serialize = false)
    private String path;

    @JSONField
    private String actualPath;

    @JSONField(serialize = false)
    private int lineNumber;

    @JSONField(serialize = false)
    private int jarId;

    // for string search
    @JSONField(serialize = false)
    private String strValue;

    public MethodResult() {
    }

    public MethodResult(String className, String methodName, String methodDesc) {
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

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

    public String getRestfulType() {
        return restfulType;
    }

    public String getActualPath() {
        if (this.path == null) {
            return "/";
        }
        String tmp = this.path.trim();
        if (tmp.isEmpty()) {
            return "/";
        }
        if (!tmp.startsWith("/")) {
            tmp = "/" + tmp;
        }
        return tmp;
    }

    public void setRestfulType(String restfulType) {
        this.restfulType = restfulType;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
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

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getJarId() {
        return jarId;
    }

    public void setJarId(int jarId) {
        this.jarId = jarId;
    }

    @Override
    public String toString() {
        return "<html>" + ASMUtil.convertMethodDesc(
                getMethodName(), getMethodDesc()) + "</html>";
    }

    public String getCopyString() {
        Map<String, String> data = new HashMap<>();
        data.put("className", getClassName());
        data.put("methodName", getMethodName());
        data.put("methodDesc", getMethodDesc());
        return JSON.toJSONString(data);
    }
}
