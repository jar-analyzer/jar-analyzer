package org.vidar.data;

/**
 * @author zhchen
 */
public interface DataFactory<T> {
    T parse(String[] fields);

    String[] serialize(T obj);
}
