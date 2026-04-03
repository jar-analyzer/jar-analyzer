/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.sqlite.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public class SQLQueryTemplate {

    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>();

    static {
        // 基础查询
        TEMPLATES.put("查看所有表", "SELECT tbl_name FROM sqlite_master WHERE type='table';");
        TEMPLATES.put("查看表结构", "PRAGMA table_info(${table});");
        TEMPLATES.put("查看表数据(前100)", "SELECT * FROM ${table} LIMIT 100;");
        TEMPLATES.put("查看表行数", "SELECT COUNT(*) AS total FROM ${table};");

        // 类查询
        TEMPLATES.put("搜索类(按名称)", "SELECT class_name, super_class_name, is_interface\nFROM class_table\nWHERE class_name LIKE '%${keyword}%'\nLIMIT 100;");
        TEMPLATES.put("搜索接口实现", "SELECT class_name, interface_name\nFROM interface_table\nWHERE interface_name LIKE '%${keyword}%'\nLIMIT 100;");
        TEMPLATES.put("查看类继承关系", "SELECT class_name, super_class_name\nFROM class_table\nWHERE super_class_name LIKE '%${keyword}%'\nLIMIT 100;");

        // 方法查询
        TEMPLATES.put("搜索方法(按名称)", "SELECT method_name, method_desc, class_name, is_static, access\nFROM method_table\nWHERE method_name LIKE '%${keyword}%'\nLIMIT 100;");
        TEMPLATES.put("搜索方法调用(caller)", "SELECT caller_class_name, caller_method_name,\n       callee_class_name, callee_method_name\nFROM method_call_table\nWHERE caller_method_name LIKE '%${keyword}%'\nLIMIT 100;");
        TEMPLATES.put("搜索方法调用(callee)", "SELECT caller_class_name, caller_method_name,\n       callee_class_name, callee_method_name\nFROM method_call_table\nWHERE callee_method_name LIKE '%${keyword}%'\nLIMIT 100;");
        TEMPLATES.put("方法实现关系", "SELECT class_name, method_name, method_desc, impl_class_name\nFROM method_impl_table\nWHERE method_name LIKE '%${keyword}%'\nLIMIT 100;");

        // 注解查询
        TEMPLATES.put("搜索注解", "SELECT anno_name, method_name, class_name\nFROM anno_table\nWHERE anno_name LIKE '%${keyword}%'\nLIMIT 100;");

        // 字符串查询
        TEMPLATES.put("搜索字符串常量", "SELECT value, method_name, class_name\nFROM string_table\nWHERE value LIKE '%${keyword}%'\nLIMIT 100;");

        // 成员(字段)查询
        TEMPLATES.put("搜索字段", "SELECT member_name, type_class_name, class_name, modifiers\nFROM member_table\nWHERE member_name LIKE '%${keyword}%'\nLIMIT 100;");

        // Spring 相关查询
        TEMPLATES.put("Spring Controller列表", "SELECT class_name FROM spring_controller_table;");
        TEMPLATES.put("Spring 请求映射", "SELECT class_name, method_name, restful_type, path\nFROM spring_method_table\nLIMIT 100;");
        TEMPLATES.put("Spring 拦截器", "SELECT class_name FROM spring_interceptor_table;");

        // Java Web 相关
        TEMPLATES.put("Java Web 组件", "SELECT type_name, class_name FROM java_web_table;");

        // DFS 调用链
        TEMPLATES.put("DFS 分析结果", "SELECT source_class_name, source_method_name,\n       sink_class_name, sink_method_name,\n       dfs_depth, dfs_mode\nFROM dfs_result_table\nLIMIT 100;");

        // 收藏/历史
        TEMPLATES.put("我的收藏", "SELECT class_name, method_name, method_desc\nFROM note_favorite_table;");
        TEMPLATES.put("浏览历史", "SELECT class_name, method_name, method_desc\nFROM note_history_table;");

        // JAR 信息
        TEMPLATES.put("JAR 文件列表", "SELECT jid, jar_name, jar_abs_path FROM jar_table;");
    }

    public static Map<String, String> getTemplates() {
        return TEMPLATES;
    }

    public static String getTemplate(String name) {
        return TEMPLATES.get(name);
    }

    public static String[] getTemplateNames() {
        return TEMPLATES.keySet().toArray(new String[0]);
    }
}
