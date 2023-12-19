package me.n1ar4.y4lang.lexer;

import me.n1ar4.y4lang.exception.ParseException;
import me.n1ar4.y4lang.token.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Lexical Analysis
 * Coded By 4ra1n
 */
public class Lexer {
    private final Reader reader;
    private static final int EMPTY = -1;
    private int lastChar = EMPTY;

    private final ArrayList<Token> queue = new ArrayList<>();
    private static int LINE_NUMBER = 1;

    public Lexer(Reader r) {
        this.reader = r;
    }

    public Token read() throws ParseException {
        if (fillQueue(0)) {
            return queue.remove(0);
        } else {
            return Token.EOF;
        }
    }

    public Token peek(int i) throws ParseException {
        if (fillQueue(i)) {
            return queue.get(i);
        } else {
            return Token.EOF;
        }
    }

    private boolean fillQueue(int i) throws ParseException {
        while (queue.size() <= i) {
            Token token = read0();
            if (token == null) {
                continue;
            }
            if (token == Token.EOF) {
                return false;
            }
            queue.add(token);
        }
        return true;
    }

    private int getChar() throws IOException {
        if (lastChar == EMPTY) {
            return reader.read();
        } else {
            int c = lastChar;
            lastChar = EMPTY;
            return c;
        }
    }

    @SuppressWarnings("all")
    private void ungetChar(int c) {
        lastChar = c;
    }

    public Token read0() throws ParseException {
        try {
            StringBuilder sb = new StringBuilder();
            int c = getChar();
            while (LexerHelper.isSpace(c)) {
                c = getChar();
            }
            if (c < 0) {
                return Token.EOF;
            } else if (LexerHelper.isPound(c)) {
                sb.append((char) c);
                c = getChar();
                while (LexerHelper.isLetter(c)) {
                    sb.append((char) c);
                    c = getChar();
                }
            } else if (LexerHelper.isSlash(c)) {
                c = getChar();
                if (LexerHelper.isSlash(c)) {
                    while (!LexerHelper.isLF(c)) {
                        c = getChar();
                        sb.append((char) c);
                    }
                    LINE_NUMBER++;
                    return null;
                }
            } else if (LexerHelper.isDigit(c)) {
                sb.append((char) c);
                c = getChar();
                while (LexerHelper.isDigit(c)) {
                    sb.append((char) c);
                    c = getChar();
                }
            } else if (LexerHelper.isLetter(c)) {
                sb.append((char) c);
                c = getChar();
                while (LexerHelper.isLetter(c) ||
                        LexerHelper.isDigit(c)) {
                    sb.append((char) c);
                    c = getChar();
                }
            } else if (LexerHelper.isQuota(c)) {
                sb.append((char) c);
                c = getChar();
                while (!LexerHelper.isQuota(c)) {
                    if (c == '\\') {
                        c = getChar();
                        if (LexerHelper.isQuota(c)) {
                            sb.append((char) c);
                            c = getChar();
                        }
                    } else {
                        sb.append((char) c);
                        c = getChar();
                    }
                }
                c = getChar();
            } else if (LexerHelper.isCR(c)) {
                c = getChar();
                if (LexerHelper.isLF(c)) {
                    LINE_NUMBER++;
                    return new IdToken(LINE_NUMBER - 1, Token.EOL);
                } else {
                    throw new ParseException("error token");
                }
            } else if (LexerHelper.isLF(c)) {
                LINE_NUMBER++;
                return new IdToken(LINE_NUMBER - 1, Token.EOL);
            } else if (c == '=') {
                c = getChar();
                if (c == '=') {
                    return new IdToken(LINE_NUMBER, "==");
                } else {
                    ungetChar(c);
                    return new IdToken(LINE_NUMBER, "=");
                }
            } else if (c == '>') {
                c = getChar();
                if (c == '=') {
                    return new IdToken(LINE_NUMBER, ">=");
                } else {
                    ungetChar(c);
                    return new IdToken(LINE_NUMBER, ">");
                }
            } else if (c == '<') {
                c = getChar();
                if (c == '=') {
                    return new IdToken(LINE_NUMBER, "<=");
                } else {
                    ungetChar(c);
                    return new IdToken(LINE_NUMBER, "<");
                }
            } else if (LexerHelper.isCalc(c)) {
                return new IdToken(LINE_NUMBER, String.valueOf((char) c));
            } else if (LexerHelper.isBracket(c)) {
                return new IdToken(LINE_NUMBER, String.valueOf((char) c));
            } else if (LexerHelper.isSem(c)) {
                return new IdToken(LINE_NUMBER, ";");
            } else if (LexerHelper.isComma(c)) {
                return new IdToken(LINE_NUMBER, ",");
            } else {
                throw new ParseException("error token");
            }
            if (c >= 0) {
                ungetChar(c);
            }
            String temp = sb.toString();
            if (LexerHelper.isLetter(temp.toCharArray()[0]) ||
                    LexerHelper.isPound(temp.toCharArray()[0])) {
                if (temp.equalsIgnoreCase("true")) {
                    return new NumToken(LINE_NUMBER, 1);
                }
                if (temp.equalsIgnoreCase("false")) {
                    return new NumToken(LINE_NUMBER, 0);
                }
                if (temp.equalsIgnoreCase("null")) {
                    return new NumToken(LINE_NUMBER, 0);
                }
                return new IdToken(LINE_NUMBER, temp);
            } else if (LexerHelper.isDigit(temp.toCharArray()[0])) {
                return new NumToken(LINE_NUMBER, Integer.parseInt(temp));
            } else if (LexerHelper.isQuota(temp.toCharArray()[0])) {
                temp = temp.substring(1);
                return new StrToken(LINE_NUMBER, temp);
            } else {
                throw new ParseException("error token");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Token.EOF;
    }
}
