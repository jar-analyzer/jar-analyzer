package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.token.Token;

import java.util.List;

public class Skip extends Leaf {
    protected Skip(String[] t) {
        super(t);
    }

    @Override
    protected void find(List<ASTree> res, Token t) {
    }
}