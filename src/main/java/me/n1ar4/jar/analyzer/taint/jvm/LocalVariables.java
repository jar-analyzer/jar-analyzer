/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint.jvm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalVariables<T> {
    private final ArrayList<Set<T>> array;

    public LocalVariables() {
        this.array = new ArrayList<>();
    }

    public void clear() {
        this.array.clear();
    }

    public void add(Set<T> t) {
        this.array.add(t);
    }

    public void set(int index, Set<T> t) {
        array.set(index, t);
    }

    public void set(int index, T t) {
        Set<T> set = new HashSet<>();
        set.add(t);
        array.set(index, set);
    }

    public Set<T> get(int index) {
        return array.get(index);
    }

    public int size() {
        return this.array.size();
    }

    public void remove(int index) {
        this.array.remove(index);
    }

    public List<Set<T>> getList() {
        return this.array;
    }
}
