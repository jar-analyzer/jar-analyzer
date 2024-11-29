/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.rule;

import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.vul.Rule;
import me.n1ar4.jar.analyzer.utils.YamlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleExporter {
    public static void main(String[] args) throws Exception {
        Rule rule = new Rule();
        rule.setName("jar-analyzer-vulnerability-rule");

        Map<String, List<SearchCondition>> map = new HashMap<>();
        List<SearchCondition> conditions = new ArrayList<>();
        SearchCondition sc1 = new SearchCondition();
        sc1.setClassName("javax/naming/Context");
        sc1.setMethodName("lookup");
        sc1.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        conditions.add(sc1);
        map.put("JNDI", conditions);
        rule.setVulnerabilities(map);

        YamlUtil.dumpFile(rule, "test.yaml");
    }
}
