package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;

import java.util.List;

public class ArrayLiteral extends ASTList {
    public ArrayLiteral(List<ASTree> list) {
        super(list);
    }

    public int size() {
        return numChildren();
    }

    @Override
    public Object eval(Environment env) {
        int s = numChildren();
        Object[] res = new Object[s];
        int i = 0;
        for (ASTree t : this) {
            res[i++] = t.eval(env);
        }
        return res;
    }
}
