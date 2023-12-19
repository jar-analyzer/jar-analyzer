package me.n1ar4.y4lang.env;

import me.n1ar4.y4lang.exception.Y4LangException;

public class ArrayEnv implements Environment {
    protected Object[] values;
    protected Environment outer;

    public ArrayEnv(int size, Environment out) {
        values = new Object[size];
        outer = out;
    }

    @Override
    public Symbols symbols() {
        throw new Y4LangException("no symbols");
    }

    @Override
    public Object get(int nest, int index) {
        if (nest == 0) {
            return values[index];
        } else if (outer == null) {
            return null;
        } else {
            return outer.get(nest - 1, index);
        }
    }

    @Override
    public void put(int nest, int index, Object value) {
        if (nest == 0) {
            values[index] = value;
        } else if (outer == null) {
            throw new Y4LangException("no outer environment");
        } else {
            outer.put(nest - 1, index, value);
        }
    }

    @Override
    public Object get(String name) {
        error(name);
        return null;
    }

    @Override
    public void put(String name, Object value) {
        error(name);
    }

    @Override
    public void putNew(String name, Object value) {
        error(name);
    }

    @Override
    public Environment where(String name) {
        error(name);
        return null;
    }

    @Override
    public void setOuter(Environment e) {
        outer = e;
    }

    private void error(String name) {
        throw new Y4LangException("cannot access by name: " + name);
    }
}
