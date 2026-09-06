/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import org.objectweb.asm.Type;

public class ASMUtil {
    public static String convertMethodDesc(String methodName, String methodDesc) {
        StringBuilder sb = new StringBuilder();

        Type returnType = Type.getReturnType(methodDesc);
        String className = returnType.getClassName();
        int lastDotIndex = className.lastIndexOf('.');
        String finalClassName = className.substring(lastDotIndex + 1);

        sb.append("<font style=\"color: blue; font-weight: bold;\">");
        sb.append(finalClassName);
        sb.append("</font>");

        sb.append(" ");

        Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
        sb.append("<font style=\"color: red; font-weight: bold;\">");
        if (methodName.equals("<init>")) {
            methodName = "[init]";
        }
        if (methodName.equals("<clinit>")) {
            methodName = "[clinit]";
        }
        sb.append(methodName);
        sb.append("</font>");
        sb.append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            className = argumentTypes[i].getClassName();
            lastDotIndex = className.lastIndexOf('.');
            finalClassName = className.substring(lastDotIndex + 1);
            sb.append(finalClassName);
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * 简单类名：取 FQN 最后一段（数组后缀保留，如 java.lang.String[] -> String[]）
     */
    private static String simpleName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return className.substring(lastDotIndex + 1);
    }

    /**
     * 纯文本的人可读方法签名：返回值简名 + 全限定点号类名 + 方法名(参数简名列表)。
     * 样式与 {@link #convertMethodDesc} 一致（无 HTML 颜色），例：
     * <pre>Process a.b.C.exec(String)</pre>
     */
    public static String prettyMethod(String className, String methodName, String methodDesc) {
        String dotted = className.replace('/', '.');
        if ("<init>".equals(methodName)) {
            methodName = "[init]";
        }
        if ("<clinit>".equals(methodName)) {
            methodName = "[clinit]";
        }
        StringBuilder sb = new StringBuilder();
        try {
            String ret = simpleName(Type.getReturnType(methodDesc).getClassName());
            if (!ret.isEmpty()) {
                sb.append(ret).append(' ');
            }
        } catch (Exception ignored) {
        }
        sb.append(dotted).append('.').append(methodName).append('(');
        try {
            Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
            for (int i = 0; i < argumentTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(simpleName(argumentTypes[i].getClassName()));
            }
        } catch (Exception ignored) {
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * 解析原始签名（内部名类 + 方法名 + 描述符，如 com/foo/Bar.exec(Ljava/lang/String;)V）
     * 并转成人可读格式；解析失败原样返回。
     */
    public static String prettySignature(String rawSignature) {
        if (rawSignature == null || rawSignature.isEmpty()) {
            return rawSignature;
        }
        int descStart = rawSignature.indexOf('(');
        if (descStart < 0) {
            return rawSignature;
        }
        int methodStart = rawSignature.lastIndexOf('.', descStart);
        if (methodStart <= 0) {
            return rawSignature;
        }
        return prettyMethod(
                rawSignature.substring(0, methodStart),
                rawSignature.substring(methodStart + 1, descStart),
                rawSignature.substring(descStart));
    }
}