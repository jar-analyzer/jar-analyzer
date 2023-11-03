package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.ClassEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassMapper {
    int insertClass(List<ClassEntity> c);

    List<ClassResult> selectClassByClassName(@Param("className") String className);

    String selectJarByClass(@Param("className") String className);
}
