package me.n1ar4.jar.analyzer.analyze.frame;

import org.objectweb.asm.tree.analysis.BasicValue;

public class ValueUtils {
    public static String fromBasicValue2String(BasicValue basicValue) {
        String descriptor = basicValue.toString();
        return DescriptorUtils.simplify(descriptor);
    }
}
