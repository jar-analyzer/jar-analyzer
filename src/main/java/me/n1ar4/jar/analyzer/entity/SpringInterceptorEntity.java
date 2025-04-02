package me.n1ar4.jar.analyzer.entity;

public class SpringInterceptorEntity {
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "SpringInterceptorEntity{" +
                "className='" + className + '\'' +
                '}';
    }
}
