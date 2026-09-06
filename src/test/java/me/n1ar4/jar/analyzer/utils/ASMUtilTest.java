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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DFS 链路展示格式化测试：类名点号化 + 参数简名。
 */
class ASMUtilTest {

    @Test
    void prettyMethod_normalCase() {
        assertEquals("Process java.lang.Runtime.exec(String)",
                ASMUtil.prettyMethod("java/lang/Runtime", "exec",
                        "(Ljava/lang/String;)Ljava/lang/Process;"));
    }

    @Test
    void prettyMethod_multipleArgs() {
        assertEquals("String a.b.C.foo(String, int, Object[])",
                ASMUtil.prettyMethod("a/b/C", "foo",
                        "(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/String;"));
    }

    @Test
    void prettyMethod_ctorAndVoidReturn() {
        assertEquals("void a.b.C.[init]()",
                ASMUtil.prettyMethod("a/b/C", "<init>", "()V"));

        assertEquals("void a.b.C.bar()",
                ASMUtil.prettyMethod("a/b/C", "bar", "()V"));
    }

    @Test
    void prettySignature_fromRawSignature() {
        assertEquals("Process java.lang.Runtime.exec(String)",
                ASMUtil.prettySignature(
                        "java/lang/Runtime.exec(Ljava/lang/String;)Ljava/lang/Process;"));
    }

    @Test
    void prettySignature_malformedInputReturnedAsIs() {
        assertEquals("no-desc-here", ASMUtil.prettySignature("no-desc-here"));
        assertEquals(null, ASMUtil.prettySignature(null));
        assertEquals("", ASMUtil.prettySignature(""));
    }
}
