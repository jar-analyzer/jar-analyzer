package me.n1ar4.jar.analyzer.el;

import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.Type;

public class ResObj {
    private final String className;
    private final MethodReference.Handle method;

    public ResObj(MethodReference.Handle m, String className) {
        this.className = className;
        this.method = m;
    }

    public String getClassName() {
        return this.className;
    }

    public MethodReference.Handle getMethod() {
        return this.method;
    }

    private int getNumFromDesc() {
        Type methodType = Type.getMethodType(this.method.getDesc());
        return methodType.getArgumentTypes().length;
    }

    @Override
    public String toString() {
        String outputFormat = "%s %s (params:%d)";
        return String.format(outputFormat,
                className,
                method.getName(),
                getNumFromDesc());
    }
}
