/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MyersDiff {

    public enum OpType {
        EQUAL,
        INSERT,
        DELETE
    }

    public static final class Op {
        public final OpType type;
        public final String line;
        public final int leftLine;
        public final int rightLine;

        public Op(OpType type, String line, int leftLine, int rightLine) {
            this.type = type;
            this.line = line;
            this.leftLine = leftLine;
            this.rightLine = rightLine;
        }
    }

    private MyersDiff() {
    }

    public static List<Op> diff(String leftText, String rightText) {
        String[] a = splitLines(leftText);
        String[] b = splitLines(rightText);
        return diff(a, b);
    }

    public static List<Op> diff(String[] a, String[] b) {
        int n = a.length;
        int m = b.length;

        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (a[i].equals(b[j])) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        List<Op> ops = new ArrayList<>(n + m);
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (a[i].equals(b[j])) {
                ops.add(new Op(OpType.EQUAL, a[i], i + 1, j + 1));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                ops.add(new Op(OpType.DELETE, a[i], i + 1, -1));
                i++;
            } else {
                ops.add(new Op(OpType.INSERT, b[j], -1, j + 1));
                j++;
            }
        }
        while (i < n) {
            ops.add(new Op(OpType.DELETE, a[i], i + 1, -1));
            i++;
        }
        while (j < m) {
            ops.add(new Op(OpType.INSERT, b[j], -1, j + 1));
            j++;
        }
        return ops;
    }

    private static String[] splitLines(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\r?\\n", -1);
    }

    static String joinLines(String[] lines) {
        return String.join("\n", Arrays.asList(lines));
    }
}
