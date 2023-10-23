package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassFileMapper {
    int insertClassFile(List<ClassFileEntity> ct);

    String selectPathByClass(@Param("className") String className);
}
