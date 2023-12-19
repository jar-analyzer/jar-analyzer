package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;
import me.n1ar4.y4lang.token.Token;

import java.util.ArrayList;
import java.util.Iterator;

public class ASTLeaf extends ASTree {
    private static final ArrayList<ASTree> empty = new ArrayList<>();
    protected Token token;

    public ASTLeaf(Token t) {
        token = t;
    }

    @Override
    public ASTree child(int i) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int numChildren() {
        return 0;
    }

    @Override
    public Iterator<ASTree> children() {
        return empty.iterator();
    }

    @Override
    public String toString() {
        return token.getText();
    }

    @Override
    public String location() {
        return "at line " + token.getLineNumber();
    }

    @Override
    public Object eval(Environment env) {
        throw new Y4LangException("cannot eval: " + toString(), this);
    }

    public Token token() {
        return token;
    }
}