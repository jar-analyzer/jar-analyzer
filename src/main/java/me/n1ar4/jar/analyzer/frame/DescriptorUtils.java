package me.n1ar4.jar.analyzer.frame;

public class DescriptorUtils {
    public static String simplify(String descriptor) {
        int squareIndex = descriptor.lastIndexOf("[");
        String prefix = descriptor.substring(0, squareIndex + 1);

        String simpleName = descriptor.substring(squareIndex + 1);
        if (simpleName.startsWith("L") && simpleName.endsWith(";")) {
            simpleName = simpleName.substring(1, simpleName.length() - 1);
        }

        int slashIndex = simpleName.lastIndexOf("/");
        simpleName = simpleName.substring(slashIndex + 1);

        return prefix + simpleName;
    }
}
