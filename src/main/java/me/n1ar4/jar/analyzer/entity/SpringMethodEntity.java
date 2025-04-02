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

public class SpringMethodEntity {
    private String className;
    private String methodName;
    private String methodDesc;
    private String path;
    private String restfulType = "Unknown";

    public String getRestfulType() {
        return restfulType;
    }

    public void setRestfulType(String restfulType) {
        this.restfulType = restfulType;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "SpringMethodEntity{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDesc='" + methodDesc + '\'' +
                ", path='" + path + '\'' +
                ", restfulType='" + restfulType + '\'' +
                '}';
    }
}
