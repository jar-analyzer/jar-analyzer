package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.ClassResult;

import java.util.List;

public interface JavaWebMapper {
    int insertServlets(List<String> filters);

    int insertFilters(List<String> filters);

    int insertListeners(List<String> filters);

    List<ClassResult> selectAllServlets();

    List<ClassResult> selectAllFilters();

    List<ClassResult> selectAllListeners();
}
