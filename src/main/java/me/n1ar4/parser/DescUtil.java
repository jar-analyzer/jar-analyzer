/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.parser;

import org.objectweb.asm.Type;

public class DescUtil {
    public static DescInfo parseDesc(String desc) {
        DescInfo ret = new DescInfo();
        Type methodType = Type.getMethodType(desc);
        Type[] argumentTypes = methodType.getArgumentTypes();
        for (Type argumentType : argumentTypes) {
            ret.getParams().add(argumentType.getClassName());
        }
        Type returnType = methodType.getReturnType();
        ret.setRet(returnType.getClassName());
        return ret;
    }

    public static String cleanJavaLang(String c) {
        int lastIndex = Math.max(c.lastIndexOf('.'), c.lastIndexOf('$'));
        if (lastIndex != -1) {
            return c.substring(lastIndex + 1);
        } else {
            return c;
        }
    }
}
