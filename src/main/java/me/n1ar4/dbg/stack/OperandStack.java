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
