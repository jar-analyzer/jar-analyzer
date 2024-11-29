/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
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
}
