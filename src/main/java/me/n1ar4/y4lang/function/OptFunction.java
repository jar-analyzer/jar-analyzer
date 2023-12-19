package me.n1ar4.y4lang.function;

import me.n1ar4.y4lang.ast.BlockStmt;
import me.n1ar4.y4lang.ast.ParameterList;
import me.n1ar4.y4lang.env.ArrayEnv;
import me.n1ar4.y4lang.env.Environment;

public class OptFunction extends Function {
    protected int size;

    public OptFunction(ParameterList parameters, BlockStmt body,
                       Environment env, int memorySize) {
        super(parameters, body, env);
        size = memorySize;
    }

    @Override
    public Environment makeEnv() {
        return new ArrayEnv(size, env);
    }
}
