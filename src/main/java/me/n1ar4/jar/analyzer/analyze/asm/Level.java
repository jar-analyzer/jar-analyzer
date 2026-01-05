/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.asm;

import java.util.ArrayDeque;

class Level {
    int lo, hi;
    ArrayDeque<int[]> branches = new ArrayDeque<>();

    Level(int[] b) {
        lo = b[0];
        hi = b[1];
        branches.add(b);
    }

    boolean insert(int[] b) {
        if (b[1] <= lo) {
            branches.addFirst(b);
            lo = b[0];
        } else if (b[0] >= hi) {
            branches.addLast(b);
            hi = b[1];
        } else return b[0] > lo && b[1] < hi
                && (b[0] + b[1]) >> 1 > (lo + hi) >> 1 ? tryTail(b, lo, hi) : tryHead(b, lo, hi);
        return true;
    }

    private boolean tryHead(int[] b, int lo, int hi) {
        int[] head = branches.removeFirst();
        try {
            if (head[1] > b[0]) return false;
            if (branches.isEmpty() || (lo = branches.getFirst()[0]) >= b[1]) {
                branches.addFirst(b);
                return true;
            } else return b[0] > lo && b[1] < hi
                    && (b[0] + b[1]) >> 1 > (lo + hi) >> 1 ? tryTail(b, lo, hi) : tryHead(b, lo, hi);
        } finally {
            branches.addFirst(head);
        }
    }

    private boolean tryTail(int[] b, int lo, int hi) {
        int[] tail = branches.removeLast();
        try {
            if (tail[0] < b[1]) return false;
            if (branches.isEmpty() || (hi = branches.getLast()[1]) <= b[0]) {
                branches.addLast(b);
                return true;
            } else return b[0] > lo && b[1] < hi
                    && (b[0] + b[1]) >> 1 > (lo + hi) >> 1 ? tryTail(b, lo, hi) : tryHead(b, lo, hi);
        } finally {
            branches.addLast(tail);
        }
    }
}