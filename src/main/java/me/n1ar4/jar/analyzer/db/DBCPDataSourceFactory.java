package me.n1ar4.jar.analyzer.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class DBCPDataSourceFactory extends UnpooledDataSourceFactory {
    public DBCPDataSourceFactory() {
        this.dataSource = new BasicDataSource();
    }
}