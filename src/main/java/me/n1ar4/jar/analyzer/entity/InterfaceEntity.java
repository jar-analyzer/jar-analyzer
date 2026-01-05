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

public class InterfaceEntity {
    private int iid;
    private String interfaceName;
    private String className;
    private Integer jarId;

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
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

    @Override
    public String toString() {
        return "InterfaceEntity{" +
                "iid=" + iid +
                ", interfaceName='" + interfaceName + '\'' +
                ", className='" + className + '\'' +
                ", jarId='" + jarId + '\'' +
                '}';
    }
}
