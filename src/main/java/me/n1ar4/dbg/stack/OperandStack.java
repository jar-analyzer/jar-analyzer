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

package me.n1ar4.dbg.stack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("all")
public class OperandStack<T> {
    private final LinkedList<Set<T>> stack;

    public OperandStack() {
        this.stack = new LinkedList<>();
    }

    public Set<T> pop() {
        return stack.remove(stack.size() - 1);
    }

    public void push(T t) {
        Set<T> set = new HashSet<>();
        set.add(t);
        stack.add(set);
    }

    public void push() {
        stack.add(new HashSet<>());
    }

    public void push(Set<T> t) {
        stack.add(t);
    }

    public void clear() {
        stack.clear();
    }

    public Set<T> get(int index) {
        return stack.get(stack.size() - index - 1);
    }

    public void set(int index, Set<T> t) {
        stack.set(stack.size() - index - 1, t);
    }

    public void set(int index, T t) {
        Set<T> set = new HashSet<>();
        set.add(t);
        stack.set(stack.size() - index - 1, set);
    }

    public void add(Set<T> t) {
        this.stack.add(t);
    }

    public int size() {
        return this.stack.size();
    }

    public void remove(int index) {
        this.stack.remove(index);
    }

    public List<Set<T>> getList() {
        return this.stack;
    }
}
