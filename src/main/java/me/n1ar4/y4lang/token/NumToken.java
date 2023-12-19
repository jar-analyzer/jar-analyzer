package me.n1ar4.y4lang.token;

public class NumToken extends Token {
    private final int value;

    public NumToken(int line, int v) {
        super(line);
        value = v;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public String getText() {
        return Integer.toString(value);
    }

    @Override
    public int getNumber() {
        return value;
    }
}
