package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.JarEntity;

import java.util.List;

public interface JarMapper {
    int insertJar(List<JarEntity> jar);

    List<String> selectAllJars();
}
