/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.frame;

import org.objectweb.asm.tree.analysis.BasicValue;

public class ValueUtils {
    public static String fromBasicValue2String(BasicValue basicValue) {
        String descriptor = basicValue.toString();
        return DescriptorUtils.simplify(descriptor);
    }
}
