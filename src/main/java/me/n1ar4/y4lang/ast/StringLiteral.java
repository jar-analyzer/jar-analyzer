package me.n1ar4.y4lang.ast;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.token.Token;

public class StringLiteral extends ASTLeaf {
    public StringLiteral(Token t) {
        super(t);
    }

    public String value() {
        return token().getText();
    }

    @Override
    public Object eval(Environment env) {
        return this.value();
    }
}
