package me.n1ar4.jar.analyzer.db.mapper;

import me.n1ar4.jar.analyzer.entity.SpringMethodEntity;

import java.util.List;

public interface SpringMethodMapper {
    int insertMappings(List<SpringMethodEntity> mappings);
}
