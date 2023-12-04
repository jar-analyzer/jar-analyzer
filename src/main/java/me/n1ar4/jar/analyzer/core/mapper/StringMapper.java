package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.entity.StringEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StringMapper {
    int insertString(List<StringEntity> str);

    List<MethodResult> selectMethodByString(@Param("value") String value);

    List<String> selectStrings(int offset);

    int selectCount();
}
