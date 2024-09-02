/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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