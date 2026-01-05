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

public class AnnoEntity {
    private int annoId;
    private String annoName;
    private String methodName;
    private String className;
    private Integer jarId;
    private Integer visible;
    private Integer parameter;

    public int getAnnoId() {
        return annoId;
    }

    public void setAnnoId(int annoId) {
        this.annoId = annoId;
    }

    public String getAnnoName() {
        return annoName;
    }

    public void setAnnoName(String annoName) {
        this.annoName = annoName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setJarId(Integer jarId) {
        this.jarId = jarId;
    }

    public Integer getJarId() {
        return jarId;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public void setParameter(Integer parameter) {
        this.parameter = parameter;
    }

    public Integer getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return "AnnoEntity{" +
                "annoId=" + annoId +
                ", annoName='" + annoName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", className='" + className + '\'' +
                ", jarId=" + jarId +
                ", visible=" + visible +
                ", parameter=" + parameter +
                '}';
    }
}
