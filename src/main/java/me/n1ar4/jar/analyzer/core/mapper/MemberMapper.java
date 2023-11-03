package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.MemberEntity;

import java.util.List;

public interface MemberMapper {
    int insertMember(List<MemberEntity> Member);
}
