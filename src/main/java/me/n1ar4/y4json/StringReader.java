package me.n1ar4.y4json;

/**
 * StringReader
 *
 * <p>提供读取字符串中字符的功能</p>
 * <p>支持改变索引的 read 和不改变的 peek</p>
 * <p>另外支持 peek next 和 prev 操作</p>
 */
@SuppressWarnings("unused")
public class StringReader implements JSONConst {
    private final String str;
    private int index = 0;

    public StringReader(String str) {
        this.str = str;
    }

    /**
     * 改变索引的 read
     *
     * @return 当前字符
     */
    public int read() {
        if (index < str.length()) {
            return str.charAt(index++);
        } else {
            return EOF;
        }
    }

    /**
     * 不改变索引的读
     *
     * @return 当前字符
     */
    public int peek() {
        if (index < str.length()) {
            return str.charAt(index);
        } else {
            return EOF;
        }
    }

    /**
     * 不改变索引读上一个
     *
     * @return 上一个字符
     */
    public int peekPrev() {
        if (index > 0 && index <= str.length()) {
            return str.charAt(index - 1);
        } else {
            return EOF;
        }
    }

    /**
     * 不改变索引读下一个
     *
     * @return 下一个字符
     */
    public int peekNext() {
        if (index >= 0 && index < str.length() - 1) {
            return str.charAt(index + 1);
        } else {
            return EOF;
        }
    }
}