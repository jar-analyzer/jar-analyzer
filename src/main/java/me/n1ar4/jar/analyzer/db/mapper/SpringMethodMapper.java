package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.entity.SpringMethodEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SpringMethodMapper {
    int insertMappings(List<SpringMethodEntity> mappings);

    List<MethodResult> selectMappingsByClassName(@Param("className") String className);
}
