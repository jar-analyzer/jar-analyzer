package me.n1ar4.shell.analyzer.model;

@SuppressWarnings("all")
public class ClassObj {
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
}
