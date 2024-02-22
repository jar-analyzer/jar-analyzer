package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.AnnoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface AnnoMapper {
    int insertAnno(List<AnnoEntity> anno);

    ArrayList<String> selectAnnoByClassName(@Param("className") String className);

    ArrayList<String> selectAnnoByClassAndMethod(@Param("className") String className,
                                            @Param("methodName") String methodName);
}
