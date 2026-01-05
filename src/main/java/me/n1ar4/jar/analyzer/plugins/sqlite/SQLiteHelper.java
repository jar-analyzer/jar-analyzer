/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.sqlite;

import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.InputStream;
import java.io.StringReader;
import java.sql.*;
import java.util.Properties;

public class SQLiteHelper {
    private static final Logger logger = LogManager.getLogger();
    private static final String JDBC_DRIVER;
    private static final String DATABASE_URL;

    static {
        InputStream is = SQLiteHelper.class.getClassLoader().getResourceAsStream("jdbc.properties");
        String data = IOUtil.readString(is);
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("read database config error");
        }
        try {
            final Properties p = new Properties();
            p.load(new StringReader(data));
            JDBC_DRIVER = p.getProperty("driver");
            logger.info("jdbc driver: {}", JDBC_DRIVER);
            DATABASE_URL = p.getProperty("url");
            logger.info("jdbc url: {}", DATABASE_URL);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Connection connection;

    public void connect() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DATABASE_URL);
            logger.info("connect sqlite success");
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("close sqlite connection");
            } catch (SQLException e) {
                logger.error("error: {}", e.toString());
            }
        }
    }

    @SuppressWarnings("all")
    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }
}