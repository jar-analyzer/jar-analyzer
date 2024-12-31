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

public class ClassResult {
    private String jarName;
    private String className;
    private String superClassName;
    private int isInterfaceInt;

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

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public int getIsInterfaceInt() {
        return isInterfaceInt;
    }

    public void setIsInterfaceInt(int isInterfaceInt) {
        this.isInterfaceInt = isInterfaceInt;
    }

    @Override
    public String toString() {
        return "ClassResult{" +
                "jarName='" + jarName + '\'' +
                ", className='" + className + '\'' +
                ", superClassName='" + superClassName + '\'' +
                ", isInterfaceInt=" + isInterfaceInt +
                '}';
    }
}
