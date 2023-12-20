package me.n1ar4.y4lang.function;

import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.Y4LangException;

import java.lang.reflect.Method;

public class NativeFunction {
    protected Method method;
    protected String name;
    protected int numParams;

    public NativeFunction(String n, Method m) {
        name = n;
        method = m;
        numParams = m.getParameterTypes().length;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<native:" + hashCode() + ">";
    }

    public int numOfParameters() {
        return numParams;
    }

    public Object invoke(Object[] args, ASTree tree) {
        try {
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new Y4LangException("bad native function call: " + name, tree);
        }
    }
}
