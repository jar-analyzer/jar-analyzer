package me.n1ar4.y4lang.natives;

import me.n1ar4.y4lang.env.Environment;
import me.n1ar4.y4lang.exception.Y4LangException;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class Collection {
    public static HashMap<Object, Object> newMap() {
        return new HashMap<>();
    }

    public static int putMap(Object map, Object key, Object value) {
        if (!(map instanceof HashMap)) {
            throw new Y4LangException("map error");
        }
        ((HashMap<Object, Object>) map).put(key, value);
        return Environment.TRUE;
    }

    public static Object getMap(Object map, Object key) {
        if (!(map instanceof HashMap)) {
            throw new Y4LangException("map error");
        }
        return ((HashMap<Object, Object>) map).get(key);
    }

    public static Object clearMap(Object map) {
        if (!(map instanceof HashMap)) {
            throw new Y4LangException("map error");
        }
        ((HashMap<Object, Object>) map).clear();
        return Environment.TRUE;
    }
}
