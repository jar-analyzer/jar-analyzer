/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.shell.analyzer.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("all")
public class ClassObj implements Comparable<ClassObj> {
    private String className;

    private String type;

    public ClassObj(String name, String type) {
        this.className = name;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return getClassName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassObj classObj = (ClassObj) o;
        return Objects.equals(className, classObj.className) && Objects.equals(type, classObj.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(className);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }

    @Override
    public int compareTo(@NotNull ClassObj o) {
        return this.className.compareTo(o.className);
    }
}
