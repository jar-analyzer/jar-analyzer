package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.core.Threads;
import me.n1ar4.y4lang.env.Environment;

import java.util.List;
import java.util.UUID;

public class GoStmt extends ASTList {
    public GoStmt(List<ASTree> list) {
        super(list);
    }

    @Override
    public Object eval(Environment env) {
        String key = UUID.randomUUID().toString();
        Threads.add(key, new Thread(() -> child(0).eval(env)));
        Threads.start(key);
        return null;
    }
}
