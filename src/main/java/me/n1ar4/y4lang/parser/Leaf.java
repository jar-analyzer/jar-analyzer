package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;
import me.n1ar4.y4lang.token.Token;

import java.util.List;

public class Leaf extends Element {
    protected String[] tokens;

    protected Leaf(String[] pat) {
        tokens = pat;
    }

    @Override
    protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
        Token t = lexer.read();
        if (t.isIdentifier()) {
            for (String token : tokens) {
                if (token.equals(t.getText())) {
                    find(res, t);
                    return;
                }
            }
        }
        if (tokens.length > 0) {
            throw new ParseException(tokens[0] + " expected.", t);
        } else {
            throw new ParseException(t);
        }
    }

    protected void find(List<ASTree> res, Token t) {
        res.add(new ASTLeaf(t));
    }

    @Override
    protected boolean match(Lexer lexer) throws ParseException {
        Token t = lexer.peek(0);
        if (t.isIdentifier()) {
            for (String token : tokens) {
                if (token.equals(t.getText())) {
                    return true;
                }
            }
        }
        return false;
    }
}