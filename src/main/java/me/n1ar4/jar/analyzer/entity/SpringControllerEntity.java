package me.n1ar4.jar.analyzer.entity;

public class SpringControllerEntity {
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "SpringControllerEntity{" +
                "className='" + className + '\'' +
                '}';
    }
}
