package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.token.Token;

import java.util.HashSet;

public class IdToken extends AToken {
    HashSet<String> reserved;

    protected IdToken(Class<? extends ASTLeaf> type, HashSet<String> r) {
        super(type);
        reserved = r != null ? r : new HashSet<>();
    }

    @Override
    protected boolean test(Token t) {
        return t.isIdentifier() && !reserved.contains(t.getText());
    }
}