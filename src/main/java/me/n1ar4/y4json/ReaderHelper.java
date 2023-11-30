package me.n1ar4.y4json;

public class ReaderHelper implements JSONConst {
    public static boolean isWhite(int c) {
        return c == space ||
                c == lineFeed ||
                c == horizontalTab ||
                c == carriageReturn;
    }

    public static boolean isDigitAll(int c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isDigit19(int c) {
        return c >= '1' && c <= '9';
    }

    public static boolean isDigitE(int c) {
        return c == 'e' || c == 'E';
    }

    public static boolean isDigitEPlus(int c) {
        return c == '+' || c == '-';
    }

    public static boolean isHex(int c) {
        return isDigitAll(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }
}
