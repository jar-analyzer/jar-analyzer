package me.n1ar4.jar.analyzer.entity;

public class MemberEntity {
    private int mid;
    private String memberName;
    private int modifiers;
    private String typeClassName;
    private String className;

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
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

    @Override
    public String toString() {
        return "MemberEntity{" +
                "mid=" + mid +
                ", memberName='" + memberName + '\'' +
                ", modifiers=" + modifiers +
                ", typeClassName='" + typeClassName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
