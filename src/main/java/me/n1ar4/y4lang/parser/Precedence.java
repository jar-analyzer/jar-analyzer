package me.n1ar4.y4lang.parser;

public class Precedence {
    int value;
    boolean leftAssoc;

    public Precedence(int v, boolean a) {
        value = v;
        leftAssoc = a;
    }
}