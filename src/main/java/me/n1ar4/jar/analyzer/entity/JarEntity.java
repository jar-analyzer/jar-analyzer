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

public class JarEntity {
    private int jid;
    private String jarName;
    private String jarAbsPath;

    public int getJid() {
        return jid;
    }

    public void setJid(int jid) {
        this.jid = jid;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getJarAbsPath() {
        return jarAbsPath;
    }

    public void setJarAbsPath(String jarAbsPath) {
        this.jarAbsPath = jarAbsPath;
    }

    @Override
    public String toString() {
        return "JarEntity{" +
                "jid=" + jid +
                ", jarName='" + jarName + '\'' +
                ", jarAbsPath='" + jarAbsPath + '\'' +
                '}';
    }
}
