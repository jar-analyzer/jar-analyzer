/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils.canvas;

public class TextPixel implements Comparable<TextPixel> {
    public int row;
    public int col;
    public String value;

    public TextPixel(int row, int col, String value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    @Override
    public int compareTo(TextPixel item) {
        if (this.row < item.row) {
            return -1;
        } else if (this.row > item.row) {
            return 1;
        } else if (this.col < item.col) {
            return -1;
        } else if (this.col > item.col) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("TextPixel(%d, %d, %s)", row, col, value);
    }

    public static TextPixel valueOf(int row, int col, String value) {
        return new TextPixel(row, col, value);
    }
}
