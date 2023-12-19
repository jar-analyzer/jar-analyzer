package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTLeaf;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;
import me.n1ar4.y4lang.token.Token;

import java.util.ArrayList;
import java.util.List;

public class Expr extends Element {
    protected Factory factory;
    protected Operators ops;
    protected Parser factor;

    protected Expr(Class<? extends ASTree> clazz, Parser exp, Operators map) {
        factory = Factory.getForASTList(clazz);
        ops = map;
        factor = exp;
    }

    @Override
    public void parse(Lexer lexer, List<ASTree> res) throws ParseException {
        ASTree right = factor.parse(lexer);
        Precedence pre;
        while ((pre = nextOperator(lexer)) != null) {
            right = doShift(lexer, right, pre.value);
        }
        res.add(right);
    }

    private ASTree doShift(Lexer lexer, ASTree left, int pre) throws ParseException {
        ArrayList<ASTree> list = new ArrayList<>();
        list.add(left);
        list.add(new ASTLeaf(lexer.read()));
        ASTree right = factor.parse(lexer);
        Precedence next;
        while ((next = nextOperator(lexer)) != null && rightIsExpr(pre, next)) {
            right = doShift(lexer, right, next.value);
        }
        list.add(right);
        return factory.make(list);
    }

    private Precedence nextOperator(Lexer lexer) throws ParseException {
        Token t = lexer.peek(0);
        if (t.isIdentifier()) {
            return ops.get(t.getText());
        } else {
            return null;
        }
    }

    private static boolean rightIsExpr(int pre, Precedence nextPre) {
        if (nextPre.leftAssoc) {
            return pre < nextPre.value;
        } else {
            return pre <= nextPre.value;
        }
    }

    @Override
    protected boolean match(Lexer lexer) throws ParseException {
        return factor.match(lexer);
    }
}