package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;

import java.util.List;

public abstract class Element {
    protected abstract void parse(Lexer lexer, List<ASTree> res) throws ParseException;

    protected abstract boolean match(Lexer lexer) throws ParseException;
}