package me.n1ar4.y4lang.env;

public interface Environment {
    int TRUE = 1;
    int FALSE = 0;

    void put(String name, Object value);

    Object get(String name);

    void setOuter(Environment e);

    Symbols symbols();

    void put(int nest, int index, Object value);

    Object get(int nest, int index);

    void putNew(String name, Object value);

    Environment where(String name);
}
