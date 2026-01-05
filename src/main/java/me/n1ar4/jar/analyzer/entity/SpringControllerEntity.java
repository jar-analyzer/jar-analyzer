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

public class SpringControllerEntity {
    private String className;
    private Integer jarId;

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

    @Override
    public String toString() {
        return "SpringControllerEntity{" +
                "className='" + className + '\'' +
                '}';
    }
}
