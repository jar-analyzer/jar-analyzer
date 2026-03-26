/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.el;

import me.n1ar4.jar.analyzer.el.MethodEL;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MethodELTest {

    // ========== 基础字段 ==========

    @Test
    void fluentApi_nameContains() {
        MethodEL el = new MethodEL();
        MethodEL result = el.nameContains("exec");
        assertSame(el, result, "应返回 this 以支持链式调用");
        assertEquals("exec", el.getNameContains());
    }

    @Test
    void fluentApi_classNameContains() {
        MethodEL el = new MethodEL();
        el.classNameContains("Controller");
        assertEquals("Controller", el.getClassNameContains());
    }

    @Test
    void fluentApi_chaining() {
        MethodEL el = new MethodEL();
        el.startWith("get")
                .endWith("Value")
                .nameContains("User")
                .classNameContains("Service")
                .returnType("java.lang.String")
                .paramsNum(2)
                .isStatic(false)
                .isPublic(true);

        assertEquals("get", el.getStartWith());
        assertEquals("Value", el.getEndWith());
        assertEquals("User", el.getNameContains());
        assertEquals("Service", el.getClassNameContains());
        assertEquals("java.lang.String", el.getReturnType());
        assertEquals(2, el.getParamsNum());
        assertFalse(el.getStatic());
        assertTrue(el.getPublic());
    }

    // ========== containsInvoke / excludeInvoke ==========

    @Test
    void containsInvoke_single() {
        MethodEL el = new MethodEL();
        el.containsInvoke("java.lang.Runtime", "exec");

        assertNotNull(el.getContainsInvokeList());
        assertEquals(1, el.getContainsInvokeList().size());
        assertArrayEquals(new String[]{"java.lang.Runtime", "exec"},
                el.getContainsInvokeList().get(0));
    }

    @Test
    void containsInvoke_multiple() {
        MethodEL el = new MethodEL();
        el.containsInvoke("java.lang.Runtime", "exec")
                .containsInvoke("java.lang.ProcessBuilder", "start");

        assertEquals(2, el.getContainsInvokeList().size());
        assertArrayEquals(new String[]{"java.lang.Runtime", "exec"},
                el.getContainsInvokeList().get(0));
        assertArrayEquals(new String[]{"java.lang.ProcessBuilder", "start"},
                el.getContainsInvokeList().get(1));
    }

    @Test
    void excludeInvoke_single() {
        MethodEL el = new MethodEL();
        el.excludeInvoke("java.lang.System", "exit");

        assertNotNull(el.getExcludeInvokeList());
        assertEquals(1, el.getExcludeInvokeList().size());
        assertArrayEquals(new String[]{"java.lang.System", "exit"},
                el.getExcludeInvokeList().get(0));
    }

    @Test
    void containsAndExclude_combined() {
        MethodEL el = new MethodEL();
        el.containsInvoke("java.lang.Runtime", "exec")
                .excludeInvoke("java.lang.System", "exit");

        assertEquals(1, el.getContainsInvokeList().size());
        assertEquals(1, el.getExcludeInvokeList().size());
    }

    // ========== nameRegex / classNameRegex ==========

    @Test
    void nameRegex_set() {
        MethodEL el = new MethodEL();
        el.nameRegex("get.*|set.*");
        assertEquals("get.*|set.*", el.getNameRegex());
    }

    @Test
    void classNameRegex_set() {
        MethodEL el = new MethodEL();
        el.classNameRegex(".*Controller.*");
        assertEquals(".*Controller.*", el.getClassNameRegex());
    }

    @Test
    void regex_chained_with_invoke() {
        MethodEL el = new MethodEL();
        el.containsInvoke("java.lang.Runtime", "exec")
                .nameRegex("do.*")
                .classNameRegex(".*Servlet.*")
                .excludeInvoke("java.lang.System", "exit");

        assertEquals("do.*", el.getNameRegex());
        assertEquals(".*Servlet.*", el.getClassNameRegex());
        assertEquals(1, el.getContainsInvokeList().size());
        assertEquals(1, el.getExcludeInvokeList().size());
    }

    // ========== 初始状态 ==========

    @Test
    void constructor_initialState() {
        MethodEL el = new MethodEL();
        assertNotNull(el.getContainsInvokeList());
        assertTrue(el.getContainsInvokeList().isEmpty());
        assertNotNull(el.getExcludeInvokeList());
        assertTrue(el.getExcludeInvokeList().isEmpty());
        assertNull(el.getNameRegex());
        assertNull(el.getClassNameRegex());
        assertNull(el.getStatic());
        assertNull(el.getPublic());
        assertNull(el.getParamsNum());
    }
}
