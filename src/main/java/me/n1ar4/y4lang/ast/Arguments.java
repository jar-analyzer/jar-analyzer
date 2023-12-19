package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.function.Function;
import me.n1ar4.y4lang.function.NativeFunction;

import java.util.List;

public class Arguments extends Postfix {
    public Arguments(List<ASTree> c) {
        super(c);
    }

    @Override
    public Object eval(Environment callerEnv, Object value) {
        if (!(value instanceof NativeFunction)) {
            if (!(value instanceof Function)) {
                throw new Y4LangException("bad function", this);
            }
            Function func = (Function) value;
            ParameterList params = func.parameters();
            if (size() != params.size()) {
                throw new Y4LangException("bad number of arguments", this);
            }
            Environment newEnv = func.makeEnv();
            int num = 0;
            for (ASTree a : this) {
                params.eval(newEnv, num++, a.eval(callerEnv));
            }
            return func.body().eval(newEnv);
        } else {
            NativeFunction func = (NativeFunction) value;
            int nativeParams = func.numOfParameters();
            if (size() != nativeParams) {
                throw new Y4LangException("bad number of arguments", this);
            }
            Object[] args = new Object[nativeParams];
            int num = 0;
            for (ASTree a : this) {
                args[num++] = a.eval(callerEnv);
            }
            return func.invoke(args, this);
        }
    }

    public int size() {
        return numChildren();
    }
}
