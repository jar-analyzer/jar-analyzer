package me.n1ar4.jar.analyzer.core.mapper;

import me.n1ar4.jar.analyzer.entity.AnnoEntity;

import java.util.List;

public interface AnnoMapper {
    int insertAnno(List<AnnoEntity> anno);
}
