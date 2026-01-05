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

public class MemberEntity {
    private int mid;
    private String memberName;
    private int modifiers;
    private String value;
    private String typeClassName;
    private String className;
    private String methodDesc;
    private String methodSignature;
    private Integer jarId;

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public String getTypeClassName() {
        return typeClassName;
    }

    public void setTypeClassName(String typeClassName) {
        this.typeClassName = typeClassName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public Integer getJarId() {
        return jarId;
    }

    public void setJarId(Integer jarId) {
        this.jarId = jarId;
    }

    @Override
    public String toString() {
        return "MemberEntity{" +
                "mid=" + mid +
                ", memberName='" + memberName + '\'' +
                ", modifiers=" + modifiers +
                ", value='" + value + '\'' +
                ", typeClassName='" + typeClassName + '\'' +
                ", className='" + className + '\'' +
                ", methodDesc='" + methodDesc + '\'' +
                ", methodSignature='" + methodSignature + '\'' +
                ", jarId=" + jarId +
                '}';
    }
}
