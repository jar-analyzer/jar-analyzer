package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.SpringInterceptorEntity;

import java.util.List;

public interface SpringInterceptorMapper {
    int insertInterceptors(List<SpringInterceptorEntity> interceptors);

    List<ClassResult> selectAllSpringI();
}
