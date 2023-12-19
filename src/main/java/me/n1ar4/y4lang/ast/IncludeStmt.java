package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.lib.LibManager;

import java.util.List;

public class IncludeStmt extends ASTList{
    public IncludeStmt(List<ASTree> list) {
        super(list);
    }

    @Override
    public Object eval(Environment env) {
        String includeName = (String) child(0).eval(env);
        LibManager.addLib(includeName,env);
        return null;
    }
}
