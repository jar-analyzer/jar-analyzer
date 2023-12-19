package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;

import java.util.List;

import static me.n1ar4.y4lang.env.Environment.FALSE;

public class WhileStmt extends ASTList {
    public WhileStmt(List<ASTree> c) {
        super(c);
    }

    public ASTree condition() {
        return child(0);
    }

    public ASTree body() {
        return child(1);
    }

    @Override
    public String toString() {
        return "(while " + condition() + " " + body() + ")";
    }

    @Override
    public Object eval(Environment env) {
        Object result = 0;
        for (; ; ) {
            Object c = condition().eval(env);
            if (c instanceof Integer && (Integer) c == FALSE)
                return result;
            else
                result = body().eval(env);
        }
    }
}
