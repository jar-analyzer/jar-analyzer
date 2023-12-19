package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;

import java.util.List;

public class Tree extends Element {
    protected Parser parser;

    protected Tree(Parser p) {
        parser = p;
    }

    @Override
    protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
        res.add(parser.parse(lexer));
    }

    @Override
    protected boolean match(Lexer lexer) throws ParseException {
        return parser.match(lexer);
    }
}