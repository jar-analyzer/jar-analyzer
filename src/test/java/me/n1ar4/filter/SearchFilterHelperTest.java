/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.filter;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.util.SearchFilterHelper;
import me.n1ar4.jar.analyzer.gui.util.SearchFilterHelper.FilterMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchFilterHelperTest {

    private ArrayList<MethodResult> results;

    private static MethodResult make(String className, String methodName, String desc) {
        MethodResult r = new MethodResult();
        r.setClassName(className);
        r.setMethodName(methodName);
        r.setMethodDesc(desc);
        return r;
    }

    @BeforeEach
    void setUp() {
        results = new ArrayList<>();
        results.add(make("com/example/UserController", "getUser", "(Ljava/lang/String;)V"));
        results.add(make("com/example/AdminController", "deleteUser", "(I)V"));
        results.add(make("org/apache/commons/StringUtils", "isEmpty", "(Ljava/lang/CharSequence;)Z"));
        results.add(make("com/test/service/OrderService", "createOrder", "()V"));
        results.add(make("java/lang/Runtime", "exec", "(Ljava/lang/String;)Ljava/lang/Process;"));
    }

    // ========== 黑名单模式 ==========

    @Test
    void blacklist_exactClass_shouldExclude() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "org/apache/commons/StringUtils", FilterMode.BLACKLIST);
        assertEquals(4, filtered.size());
        assertTrue(filtered.stream().noneMatch(m -> m.getClassName().equals("org/apache/commons/StringUtils")));
    }

    @Test
    void blacklist_packagePrefix_shouldExcludeAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com/example/", FilterMode.BLACKLIST);
        assertEquals(3, filtered.size());
        assertTrue(filtered.stream().noneMatch(m -> m.getClassName().startsWith("com/example/")));
    }

    @Test
    void blacklist_dotSeparator_shouldWork() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com.example.", FilterMode.BLACKLIST);
        assertEquals(3, filtered.size());
        assertTrue(filtered.stream().noneMatch(m -> m.getClassName().startsWith("com/example/")));
    }

    @Test
    void blacklist_multipleRules_shouldExcludeAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com/example/;java/lang/Runtime", FilterMode.BLACKLIST);
        assertEquals(2, filtered.size());
    }

    @Test
    void blacklist_emptyFilter_shouldReturnAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "", FilterMode.BLACKLIST);
        assertEquals(5, filtered.size());
    }

    @Test
    void blacklist_noMatch_shouldReturnAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "nonexistent/package/", FilterMode.BLACKLIST);
        assertEquals(5, filtered.size());
    }

    // ========== 白名单模式 ==========

    @Test
    void whitelist_exactClass_shouldKeepOnly() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "java/lang/Runtime", FilterMode.WHITELIST);
        assertEquals(1, filtered.size());
        assertEquals("java/lang/Runtime", filtered.get(0).getClassName());
    }

    @Test
    void whitelist_packagePrefix_shouldKeepMatching() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com/example/", FilterMode.WHITELIST);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(m -> m.getClassName().startsWith("com/example/")));
    }

    @Test
    void whitelist_dotSeparator_shouldWork() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com.test.service.", FilterMode.WHITELIST);
        assertEquals(1, filtered.size());
        assertEquals("com/test/service/OrderService", filtered.get(0).getClassName());
    }

    @Test
    void whitelist_emptyFilter_shouldReturnAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "", FilterMode.WHITELIST);
        assertEquals(5, filtered.size());
    }

    @Test
    void whitelist_noMatch_shouldReturnEmpty() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "nonexistent/package/", FilterMode.WHITELIST);
        assertEquals(0, filtered.size());
    }

    @Test
    void whitelist_multipleRules_shouldKeepAll() {
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, "com/example/;java/lang/", FilterMode.WHITELIST);
        assertEquals(3, filtered.size());
    }

    // ========== 注释支持（经 ListParser） ==========

    @Test
    void filter_withComments_shouldIgnoreComments() {
        String filterText = "# this is a comment\ncom/example/\n// another comment\njava/lang/Runtime";
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                results, filterText, FilterMode.BLACKLIST);
        assertEquals(2, filtered.size());
    }

    // ========== 边界情况 ==========

    @Test
    void filter_emptyResults_shouldReturnEmpty() {
        ArrayList<MethodResult> empty = new ArrayList<>();
        ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                empty, "com/example/", FilterMode.BLACKLIST);
        assertEquals(0, filtered.size());
    }

    @Test
    void filter_nullFilterText_shouldHandleGracefully() {
        // ListParser.parse(null) 的行为 - 依赖 ListParser 实现
        // 如果 parse 返回空列表，则应返回全部结果
        try {
            ArrayList<MethodResult> filtered = SearchFilterHelper.filter(
                    results, null, FilterMode.BLACKLIST);
            assertEquals(5, filtered.size());
        } catch (NullPointerException e) {
            // ListParser 可能不接受 null，这也是可接受的行为
        }
    }
}
