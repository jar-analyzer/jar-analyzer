/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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