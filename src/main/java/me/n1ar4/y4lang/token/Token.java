package me.n1ar4.y4lang.token;

import me.n1ar4.y4lang.exception.Y4LangException;

public abstract class Token {
    public static final Token EOF = new Token(-1) {
    };
    public static final String EOL = "\\n";
    private final int lineNumber;

    protected Token(int line) {
        lineNumber = line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public int getNumber() {
        throw new Y4LangException("not number token");
    }

    public String getText() {
        return "";
    }
}
