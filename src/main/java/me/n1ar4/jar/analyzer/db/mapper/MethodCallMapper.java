package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.dto.MethodResult;
import me.n1ar4.jar.analyzer.entity.MethodCallEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MethodCallMapper {
    int insertMethodCall(List<MethodCallEntity> mce);

    List<MethodResult> selectCallers(@Param("calleeMethodName") String calleeMethodName,
                                     @Param("calleeMethodDesc") String calleeMethodDesc,
                                     @Param("calleeClassName") String calleeClassName);

    List<MethodResult> selectCallee(@Param("callerMethodName") String callerMethodName,
                                    @Param("callerMethodDesc") String callerMethodDesc,
                                    @Param("callerClassName") String callerClassName);
}
