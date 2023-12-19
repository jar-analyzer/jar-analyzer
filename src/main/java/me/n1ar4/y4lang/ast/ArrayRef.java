package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;

import java.util.List;

public class ArrayRef extends Postfix {
    public ArrayRef(List<ASTree> c) {
        super(c);
    }

    public ASTree index() {
        return child(0);
    }

    @Override
    public String toString() {
        return "[" + index() + "]";
    }

    @Override
    public Object eval(Environment env, Object value) {
        if (value instanceof Object[]) {
            Object index = index().eval(env);
            if (index instanceof Integer) {
                return ((Object[]) value)[(Integer) index];
            }
        }
        throw new Y4LangException("bad array access", this);
    }
}
