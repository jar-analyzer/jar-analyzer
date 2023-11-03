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
