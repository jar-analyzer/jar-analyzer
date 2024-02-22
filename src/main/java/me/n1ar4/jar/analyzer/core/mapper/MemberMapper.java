package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MemberEntity;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

public interface MemberMapper {
    int insertMember(List<MemberEntity> Member);

    ArrayList<MemberEntity> selectMembersByClass(@Param("className") String className);
}
