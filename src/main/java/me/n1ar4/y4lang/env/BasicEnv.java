package me.n1ar4.y4lang.env;

import java.util.HashMap;

public class BasicEnv implements Environment {
    protected HashMap<String, Object> values;
    protected Environment outer;

    public BasicEnv() {
        this(null);
    }

    public BasicEnv(Environment e) {
        values = new HashMap<>();
        outer = e;
    }

    @Override
    public void setOuter(Environment e) {
        outer = e;
    }

    @Override
    public Symbols symbols() {
        return null;
    }

    @Override
    public void put(int nest, int index, Object value) {

    }

    @Override
    public Object get(int nest, int index) {
        return null;
    }

    @Override
    public Object get(String name) {
        Object v = values.get(name);
        if (v == null && outer != null) {
            return outer.get(name);
        } else {
            return v;
        }
    }

    @Override
    public void putNew(String name, Object value) {
        values.put(name, value);
    }

    @Override
    public void put(String name, Object value) {
        Environment e = where(name);
        if (e == null) {
            e = this;
        }
        e.putNew(name, value);
    }

    @Override
    public Environment where(String name) {
        if (values.get(name) != null) {
            return this;
        } else if (outer == null) {
            return null;
        } else {
            return outer.where(name);
        }
    }
}
