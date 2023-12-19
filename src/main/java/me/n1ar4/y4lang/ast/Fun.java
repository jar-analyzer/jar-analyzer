package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.env.Symbols;
import me.n1ar4.y4lang.function.OptFunction;

import java.util.List;

public class Fun extends ASTList {
    private int size = -1;

    public Fun(List<ASTree> c) {
        super(c);
    }

    public ParameterList parameters() {
        return (ParameterList) child(0);
    }

    public BlockStmt body() {
        return (BlockStmt) child(1);
    }

    @Override
    public String toString() {
        return "(fun " + parameters() + " " + body() + ")";
    }

    @Override
    public void lookup(Symbols sym) {
        size = lookup(sym, parameters(), body());
    }

    @Override
    public Object eval(Environment env) {
        return new OptFunction(parameters(), body(), env, size);
    }

    public static int lookup(Symbols sym, ParameterList params, BlockStmt body) {
        Symbols newSym = new Symbols(sym);
        params.lookup(newSym);
        body.lookup(newSym);
        return newSym.size();
    }
}
