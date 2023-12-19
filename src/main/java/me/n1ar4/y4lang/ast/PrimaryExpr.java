package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;

import java.util.List;

public class PrimaryExpr extends ASTList {
    public PrimaryExpr(List<ASTree> c) {
        super(c);
    }

    public static ASTree create(List<ASTree> c) {
        return c.size() == 1 ? c.get(0) : new PrimaryExpr(c);
    }

    public ASTree operand() {
        return child(0);
    }

    public Postfix postfix(int nest) {
        return (Postfix) child(numChildren() - nest - 1);
    }

    public boolean hasPostfix(int nest) {
        return numChildren() - nest > 1;
    }

    @Override
    public Object eval(Environment env) {
        return evalSubExpr(env, 0);
    }

    public Object evalSubExpr(Environment env, int nest) {
        if (hasPostfix(nest)) {
            Object target = evalSubExpr(env, nest + 1);
            return (postfix(nest)).eval(env, target);
        } else {
            return operand().eval(env);
        }
    }
}
