package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.env.Symbols;

import java.util.Iterator;

public abstract class ASTree implements Iterable<ASTree> {
    public abstract ASTree child(int i);

    public abstract int numChildren();

    public abstract Iterator<ASTree> children();

    public abstract String location();

    @Override
    public Iterator<ASTree> iterator() {
        return children();
    }

    public abstract Object eval(Environment env);

    public void lookup(Symbols sym) {}
}