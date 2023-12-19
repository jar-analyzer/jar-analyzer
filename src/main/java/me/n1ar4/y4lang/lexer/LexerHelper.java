package me.n1ar4.y4lang.lexer;

public class LexerHelper {
    public static boolean isLetter(int c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == ':';
    }

    public static boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    public static boolean isSpace(int c) {
        return c == ' ' || c == '\t';
    }

    public static boolean isCR(int c) {
        return c == '\r';
    }

    public static boolean isLF(int c) {
        return c == '\n';
    }

    public static boolean isCalc(int c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    public static boolean isBracket(int c) {
        return c == '{' || c == '}' ||
                c == '(' || c == ')' ||
                c == '[' || c == ']';
    }

    public static boolean isComma(int c) {
        return c == ',';
    }

    public static boolean isPound(int c) {
        return c == '#';
    }

    public static boolean isSem(int c) {
        return c == ';';
    }

    public static boolean isQuota(int c) {
        return c == '"';
    }

    public static boolean isSlash(int c) {
        return c == '/';
    }
}
