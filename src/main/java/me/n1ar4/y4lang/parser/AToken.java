package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;
import me.n1ar4.y4lang.token.Token;

import java.util.List;

public abstract class AToken extends Element {
    protected Factory factory;

    protected AToken(Class<? extends ASTLeaf> type) {
        if (type == null)
            type = ASTLeaf.class;
        factory = Factory.get(type, Token.class);
    }

    @Override
    protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
        Token t = lexer.read();
        if (test(t)) {
            ASTree leaf = factory.make(t);
            res.add(leaf);
        } else
            throw new ParseException(t);
    }

    @Override
    protected boolean match(Lexer lexer) throws ParseException {
        return test(lexer.peek(0));
    }

    protected abstract boolean test(Token t);
}