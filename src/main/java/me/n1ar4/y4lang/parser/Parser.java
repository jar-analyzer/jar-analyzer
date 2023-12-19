package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Parser {
    protected List<Element> elements;
    protected Factory factory;

    public Parser(Class<? extends ASTree> clazz) {
        reset(clazz);
    }

    protected Parser(Parser p) {
        elements = p.elements;
        factory = p.factory;
    }

    public ASTree parse(Lexer lexer) throws ParseException {
        ArrayList<ASTree> results = new ArrayList<>();
        for (Element e : elements) {
            e.parse(lexer, results);
        }
        return factory.make(results);
    }

    protected boolean match(Lexer lexer) throws ParseException {
        if (elements.size() == 0) {
            return true;
        } else {
            Element e = elements.get(0);
            return e.match(lexer);
        }
    }

    public static Parser rule() {
        return rule(null);
    }

    public static Parser rule(Class<? extends ASTree> clazz) {
        return new Parser(clazz);
    }

    public Parser reset() {
        elements = new ArrayList<>();
        return this;
    }

    public Parser reset(Class<? extends ASTree> clazz) {
        elements = new ArrayList<>();
        factory = Factory.getForASTList(clazz);
        return this;
    }

    public Parser number() {
        return number(null);
    }

    public Parser number(Class<? extends ASTLeaf> clazz) {
        elements.add(new NumToken(clazz));
        return this;
    }

    public Parser identifier(HashSet<String> reserved) {
        return identifier(null, reserved);
    }

    public Parser identifier(Class<? extends ASTLeaf> clazz, HashSet<String> reserved) {
        elements.add(new IdToken(clazz, reserved));
        return this;
    }

    public Parser string() {
        return string(null);
    }

    public Parser string(Class<? extends ASTLeaf> clazz) {
        elements.add(new StrToken(clazz));
        return this;
    }

    public Parser token(String... pat) {
        elements.add(new Leaf(pat));
        return this;
    }

    public Parser sep(String... pat) {
        elements.add(new Skip(pat));
        return this;
    }

    public Parser ast(Parser p) {
        elements.add(new Tree(p));
        return this;
    }

    public Parser or(Parser... p) {
        elements.add(new OrTree(p));
        return this;
    }

    public Parser maybe(Parser p) {
        Parser p2 = new Parser(p);
        p2.reset();
        elements.add(new OrTree(new Parser[]{p, p2}));
        return this;
    }

    public Parser option(Parser p) {
        elements.add(new Repeat(p, true));
        return this;
    }

    public Parser repeat(Parser p) {
        elements.add(new Repeat(p, false));
        return this;
    }

    public Parser expression(Parser subExpr, Operators operators) {
        elements.add(new Expr(null, subExpr, operators));
        return this;
    }

    public Parser expression(Class<? extends ASTree> clazz, Parser subExpr, Operators operators) {
        elements.add(new Expr(clazz, subExpr, operators));
        return this;
    }

    public Parser insertChoice(Parser p) {
        Element e = elements.get(0);
        if (e instanceof OrTree) {
            ((OrTree) e).insert(p);
        } else {
            Parser otherwise = new Parser(this);
            reset(null);
            or(p, otherwise);
        }
        return this;
    }
}