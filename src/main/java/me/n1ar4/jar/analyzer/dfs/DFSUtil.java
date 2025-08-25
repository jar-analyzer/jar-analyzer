/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.dfs;

import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.entity.DFSResultEntity;
import me.n1ar4.jar.analyzer.entity.DFSResultListEntity;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.List;
import java.util.UUID;

public class DFSUtil {
    private static final Logger logger = LogManager.getLogger();

    public static void save(List<DFSResult> dfsResults) {
        for (DFSResult dfsResult : dfsResults) {
            String id = UUID.randomUUID().toString();
            logger.info("save dfs {}", id);

            DFSResultEntity entity = new DFSResultEntity();

            entity.setDfsDepth(dfsResult.getDepth());
            entity.setDfsMode(dfsResult.getMode());

            entity.setDfsListUid(id);

            entity.setSourceClassName(dfsResult.getSource().getClassReference().getName());
            entity.setSourceMethodName(dfsResult.getSource().getName());
            entity.setSourceMethodDesc(dfsResult.getSource().getDesc());

            entity.setSinkClassName(dfsResult.getSink().getClassReference().getName());
            entity.setSinkMethodName(dfsResult.getSink().getName());
            entity.setSinkMethodDesc(dfsResult.getSink().getDesc());

            DatabaseManager.saveDFS(entity);

            List<MethodReference.Handle> methods = dfsResult.getMethodList();
            int i = 0;
            for (MethodReference.Handle method : methods) {
                DFSResultListEntity resultList = new DFSResultListEntity();
                resultList.setDfsListIndex(i);
                resultList.setDfsListUid(id);
                resultList.setDfsClassName(method.getClassReference().getName());
                resultList.setDfsMethodName(method.getName());
                resultList.setDfsMethodDesc(method.getDesc());
                DatabaseManager.saveDFSList(resultList);
                i++;
            }
        }
    }
}
