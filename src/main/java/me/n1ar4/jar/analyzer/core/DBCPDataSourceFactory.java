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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class DBCPDataSourceFactory extends UnpooledDataSourceFactory {
    public DBCPDataSourceFactory() {
        BasicDataSource ds = new BasicDataSource();
        // 2026-06-20 FIX: 配置连接池上限和获取连接的超时，避免 MCP 高并发下连接耗尽导致
        // "Cannot get a connection, general error" 以及调用方无限阻塞
        // 与 McpServer 默认 toolMaxConcurrency(16) 对齐，再留一些余量给 GUI 端使用
        ds.setMaxTotal(32);
        ds.setMaxIdle(16);
        ds.setMinIdle(2);
        // 获取连接最多等 10s，超时直接抛 SQLException，避免阻塞 MCP 线程
        ds.setMaxWaitMillis(10_000L);
        // SQLite 单写多读，连接拿出来如果失效（例如关库）应主动剔除
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(2);
        // 空闲连接驱逐：避免长时间空闲后被 OS / 驱动判定异常
        ds.setTimeBetweenEvictionRunsMillis(60_000L);
        ds.setMinEvictableIdleTimeMillis(300_000L);
        // 连接池放在静态工厂里，归还时不必回滚（autoCommit=true）
        ds.setDefaultAutoCommit(Boolean.TRUE);
        this.dataSource = ds;
    }
}
