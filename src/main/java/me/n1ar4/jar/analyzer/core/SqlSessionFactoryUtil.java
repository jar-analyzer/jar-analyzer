/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

public class SqlSessionFactoryUtil {
    private static final Logger logger = LogManager.getLogger();
    public static SqlSessionFactory sqlSessionFactory = null;

    private SqlSessionFactoryUtil() {
    }

    static {
        logger.info("init mybatis factory");
        build();
        logger.info("init mybatis factory finish");
    }

    private static void build() {
        String resource = "mybatis.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            logger.error("error: {}", e.toString());
        }
    }

    /**
     * 重建 jar-analyzer.db 前调用：关闭旧数据源并构建全新工厂。
     * 连接池内空闲连接立即关闭，借出中的连接归还时关闭。
     * 不关闭连接池时，macOS/Linux 上删除 db 文件后旧连接仍指向
     * 孤儿 inode 继续读写旧数据，其他线程的新查询会打开空库；
     * Windows 上则因文件被占用直接删除失败。
     * 旧的 CoreEngine 持有旧工厂引用，其后续查询因数据源已关闭
     * 而快速失败，自然失效。
     */
    public static synchronized void rebuildFactory() {
        try {
            if (sqlSessionFactory != null) {
                DataSource ds = sqlSessionFactory.getConfiguration()
                        .getEnvironment().getDataSource();
                if (ds instanceof BasicDataSource) {
                    ((BasicDataSource) ds).close();
                }
            }
        } catch (Throwable t) {
            logger.warn("close old datasource failed: {}", t.toString());
        }
        logger.info("rebuild mybatis factory");
        build();
        logger.info("rebuild mybatis factory finish");
    }
}
