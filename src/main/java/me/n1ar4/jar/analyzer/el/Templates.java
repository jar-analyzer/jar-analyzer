/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import java.util.LinkedHashMap;

public class Templates {
    public static final LinkedHashMap<String, String> data = new LinkedHashMap<>();

    static {
        data.put("默认模板", "// 注意：过大的 JAR 文件可能非常耗时\n" +
                "#method\n" +
                "        .startWith(\"set\")\n" +
                "        .endWith(\"value\")\n" +
                "        .nameContains(\"lookup\")\n" +
                "        .nameNotContains(\"internal\")\n" +
                "        .classNameContains(\"Context\")\n" +
                "        .classNameNotContains(\"Abstract\")\n" +
                "        .returnType(\"java.lang.Process\")\n" +
                "        .paramTypeMap(0,\"java.lang.String\")\n" +
                "        .paramsNum(1)\n" +
                "        .isStatic(false)\n" +
                "        .isPublic(true)\n" +
                "        .isSubClassOf(\"java.awt.Component\")\n" +
                "        .isSuperClassOf(\"com.test.SomeClass\")\n" +
                "        .hasClassAnno(\"Controller\")\n" +
                "        .hasAnno(\"RequestMapping\")\n" +
                "        .excludeAnno(\"Auth\")\n" +
                "        .hasField(\"context\")");
        data.put("单 String 参数 public 构造方法", "// 注意：过大的 JAR 文件可能非常耗时\n" +
                "// 历史参考 PGSQL Driver RCE (CVE-2022-21724)\n" +
                "// 也可参考 Apache ActiveMQ RCE (CVE-2023-46604)\n" +
                "#method\n" +
                "        .nameContains(\"<init>\")\n" +
                "        .paramTypeMap(0,\"java.lang.String\")\n" +
                "        .paramsNum(1)\n" +
                "        .isStatic(false)\n" +
                "        .isPublic(true)");
        data.put("搜索可能符合 Swing RCE 条件的方法", "// 注意：过大的 JAR 文件可能非常耗时\n" +
                "// JDK CVE-2023-21939\n" +
                "// 必须有一个 set 方法（以 set 开头且 public）\n" +
                "// set 方法必须只有一个 String 类型参数\n" +
                "// 该类必须是 Component 子类（包括间接子类）\n" +
                "// 注意：由于多层继承关系建议加入 rt.jar 环境\n" +
                "#method\n" +
                "        .startWith(\"set\")\n" +
                "        .paramsNum(1)\n" +
                "        .isStatic(false)\n" +
                "        .isPublic(true)\n" +
                "        .paramTypeMap(0,\"java.lang.String\")\n" +
                "        .isSubClassOf(\"java.awt.Component\")");
    }
}
