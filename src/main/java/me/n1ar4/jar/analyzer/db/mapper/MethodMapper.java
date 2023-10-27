package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.dto.MethodResult;
import me.n1ar4.jar.analyzer.entity.MethodEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodMapper {
    int insertMethod(List<MethodEntity> method);

    List<MethodResult> selectMethodsByClassName(@Param("className") String className);

    List<MethodResult> selectMethods(@Param("className") String className,
                                     @Param("methodName") String methodName,
                                     @Param("methodDesc") String methodDesc);

    List<MethodResult> selectMethodsLike(@Param("className") String className,
                                         @Param("methodName") String methodName,
                                         @Param("methodDesc") String methodDesc);
}
