package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.env.Location;
import me.n1ar4.y4lang.env.Symbols;
import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.token.Token;

public class Name extends ASTLeaf {
    private static final int UNKNOWN = -1;
    private int nest;
    private int index;

    public String name() {
        return token().getText();
    }

    public Name(Token t) {
        super(t);
        index = UNKNOWN;
    }

    @Override
    public void lookup(Symbols sym) {
        Location loc = sym.get(name());
        if (loc == null) {
            throw new Y4LangException("undefined name: " + name(), this);
        } else {
            nest = loc.nest;
            index = loc.index;
        }
    }

    public void lookupForAssign(Symbols sym) {
        Location loc = sym.put(name());
        nest = loc.nest;
        index = loc.index;
    }

    @Override
    public Object eval(Environment env) {
        if (index == UNKNOWN) {
            return env.get(name());
        } else {
            return env.get(nest, index);
        }
    }

    public void evalForAssign(Environment env, Object value) {
        if (index == UNKNOWN) {
            env.put(name(), value);
        } else {
            env.put(nest, index, value);
        }
    }
}
