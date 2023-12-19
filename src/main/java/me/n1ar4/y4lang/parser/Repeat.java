package me.n1ar4.y4lang.parser;

import me.n1ar4.y4lang.ast.ASTList;
import me.n1ar4.y4lang.ast.ASTree;
import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.lexer.Lexer;

import java.util.List;

public class Repeat extends Element {
    protected Parser parser;
    protected boolean onlyOnce;

    protected Repeat(Parser p, boolean once) {
        parser = p;
        onlyOnce = once;
    }

    @Override
    protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
        while (parser.match(lexer)) {
            ASTree t = parser.parse(lexer);
            if (t.getClass() != ASTList.class || t.numChildren() > 0) {
                res.add(t);
            }
            if (onlyOnce) {
                break;
            }
        }
    }

    @Override
    protected boolean match(Lexer lexer) throws ParseException {
        return parser.match(lexer);
    }
}