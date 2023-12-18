package com.n1ar4.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class FilterObjectInputStream extends ObjectInputStream {
    public FilterObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        if (classDesc.getName().equals("[Ljava.lang.String;") ||
                classDesc.getName().equals("java.lang.String")) {
            return super.resolveClass(classDesc);
        }
        throw new RuntimeException(String.format("not support class: %s", classDesc.getName()));
    }
}
