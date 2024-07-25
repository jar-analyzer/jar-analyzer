package me.n1ar4.jar.analyzer.mybatis;

import com.alibaba.fastjson2.JSON;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.MenuUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class})})
public class PrintSqlInterceptor implements Interceptor {
    private static final Path logDir = Paths.get("logs");
    private static final Path outputPath = logDir.resolve(Paths.get("sql.log"));
    private static final Logger logger = LogManager.getLogger();

    static {
        try {
            Files.createDirectories(logDir);
        } catch (Exception ignored) {
        }
        try {
            Files.createFile(outputPath);
        } catch (Exception ignored) {
        }
        logger.info("init sql interceptor");
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
            return invocation.proceed();
        }
        if (!MenuUtil.getLogAllSqlConfig().getState()) {
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = boundSql.getSql().toLowerCase();
        if (sql.trim().isEmpty()) {
            return invocation.proceed();
        }
        // 不记录 INSERT 阶段的 SQL 语句
        // 这样效率会大幅下降
        if (sql.startsWith("insert") || sql.startsWith("create")) {
            return invocation.proceed();
        }
        long start = System.currentTimeMillis();
        Object returnValue = invocation.proceed();
        long time = System.currentTimeMillis() - start;
        showSql(configuration, boundSql, time);
        return returnValue;
    }

    private static void showSql(Configuration configuration, BoundSql boundSql, long time) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        if (!parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        logs(time, sql);
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value.replace("$", "\\$");
    }

    static class SqlLog {
        private long costTime;
        private String sql;

        public long getCostTime() {
            return costTime;
        }

        public void setCostTime(long costTime) {
            this.costTime = costTime;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }
    }

    private static void logs(long time, String sql) {
        SqlLog sqlLog = new SqlLog();
        sqlLog.setCostTime(time);
        sqlLog.setSql(sql);
        String data = JSON.toJSONString(sqlLog);
        data = data + "\n";
        try {
            Files.write(outputPath, data.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            logger.error(ex.toString());
        }
    }

    @Override
    @SuppressWarnings("all")
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    @SuppressWarnings("all")
    public void setProperties(Properties properties0) {
    }
}