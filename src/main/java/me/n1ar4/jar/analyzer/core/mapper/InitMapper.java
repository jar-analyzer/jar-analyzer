/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.mapper;

public interface InitMapper {
    void createJarTable();

    void createClassTable();

    void createClassFileTable();

    void createMemberTable();

    void createMethodTable();

    void createAnnoTable();

    void createInterfaceTable();

    void createMethodCallTable();

    void createMethodImplTable();

    void createStringTable();

    void createSpringControllerTable();

    void createSpringMappingTable();

    void createSpringInterceptorTable();

    void createJavaWebTable();

    void createDFSResultTable();

    void createDFSResultListTable();

    void createFavoriteTable();

    void createHistoryTable();

    /**
     * Creates the indexes that the EL search engine relies on for
     * O(log N) lookups instead of full table scans. All idempotent.
     * <p>
     * Each statement is its own method so we don't depend on the
     * driver's multi-statement support.
     */
    void createIdxMethodClass();

    void createIdxMethodName();

    void createIdxAnnoClassMethod();

    void createIdxAnnoName();

    void createIdxMemberClass();

    void createIdxCallCaller();

    void createIdxCallCallee();

    void createIdxClassTableName();

    void createIdxIfaceClass();

    void createIdxImplClass();

    void createIdxImplImpl();
}
