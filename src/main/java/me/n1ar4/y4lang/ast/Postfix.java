package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;

import java.util.List;

public abstract class Postfix extends ASTList {
    public Postfix(List<ASTree> c) {
        super(c);
    }

    public abstract Object eval(Environment env, Object value);
}
