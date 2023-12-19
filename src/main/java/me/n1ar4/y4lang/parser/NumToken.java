package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.token.Token;

public class NumToken extends AToken {
    protected NumToken(Class<? extends ASTLeaf> type) {
        super(type);
    }

    @Override
    protected boolean test(Token t) {
        return t.isNumber();
    }
}