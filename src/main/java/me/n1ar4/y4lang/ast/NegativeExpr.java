package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;

import java.util.List;

public class NegativeExpr extends ASTList {
    public NegativeExpr(List<ASTree> c) {
        super(c);
    }

    public ASTree operand() {
        return child(0);
    }

    @Override
    public String toString() {
        return "-" + operand();
    }

    @Override
    public Object eval(Environment env) {
        Object v = operand().eval(env);
        if (v instanceof Integer) {
            return -(Integer) v;
        } else {
            throw new Y4LangException("bad type for -", this);
        }
    }
}
