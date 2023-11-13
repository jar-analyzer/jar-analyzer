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
}