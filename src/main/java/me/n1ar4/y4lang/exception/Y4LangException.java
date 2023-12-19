package me.n1ar4.y4lang.exception;

import me.n1ar4.y4lang.ast.ASTree;

public class Y4LangException extends RuntimeException {
    public Y4LangException(String m) {
        super(m);
    }

    public Y4LangException(String m, ASTree t) {
        super(m + " " + t.location());
    }
}
