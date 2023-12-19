package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;

import java.util.List;

public class ReturnStmt extends ASTList {
    public ReturnStmt(List<ASTree> list) {
        super(list);
    }

    @Override
    public Object eval(Environment env) {
        return child(0).eval(env);
    }
}
