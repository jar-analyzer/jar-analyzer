/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * 防止被人 RCE
 * 因为功能实现基于原生反序列化
 */
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
