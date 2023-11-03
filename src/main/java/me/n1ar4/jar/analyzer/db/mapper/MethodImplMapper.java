package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.entity.MethodImplEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodImplMapper {
    int insertMethodImpl(List<MethodImplEntity> impl);

    List<MethodResult> selectImplClassName(@Param("className") String className,
                                           @Param("methodName") String methodName,
                                           @Param("methodDesc") String methodDesc);

    List<MethodResult> selectSuperImpls(@Param("className") String className,
                                        @Param("methodName") String methodName,
                                        @Param("methodDesc") String methodDesc);
}
