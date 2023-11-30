package me.n1ar4.jar.analyzer.core;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class SqlSessionFactoryUtil {
    private static final Logger logger = LogManager.getLogger();
    public static SqlSessionFactory sqlSessionFactory = null;

    private SqlSessionFactoryUtil() {
    }

    static {
        logger.info("init mybatis factory");
        String resource = "mybatis.xml";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            logger.error("error: {}", e.toString());
        }
        logger.info("init mybatis factory finish");
    }
}