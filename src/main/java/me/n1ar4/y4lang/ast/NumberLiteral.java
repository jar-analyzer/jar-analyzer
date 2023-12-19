package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.token.Token;

public class NumberLiteral extends ASTLeaf {
    public NumberLiteral(Token t) {
        super(t);
    }

    public int value() {
        return token().getNumber();
    }

    @Override
    public Object eval(Environment env) {
        return this.value();
    }
}
