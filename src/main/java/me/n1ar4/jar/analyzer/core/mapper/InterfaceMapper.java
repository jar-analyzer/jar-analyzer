package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.InterfaceEntity;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface InterfaceMapper {
    int insertInterface(List<InterfaceEntity> in);

    ArrayList<String> selectInterfacesByClass(@Param("className") String className);
}
