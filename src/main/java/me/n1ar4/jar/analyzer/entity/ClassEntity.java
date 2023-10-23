package me.n1ar4.jar.analyzer.entity;

public class ClassEntity {
    private int cid;
    private String jarName;
    private String className;
    private String superClassName;
    private boolean isInterface;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    @Override
    public String toString() {
        return "ClassEntity{" +
                "cid=" + cid +
                ", jarName='" + jarName + '\'' +
                ", className='" + className + '\'' +
                ", superClassName='" + superClassName + '\'' +
                ", isInterface=" + isInterface +
                '}';
    }
}
