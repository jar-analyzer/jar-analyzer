/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.vul.Rule;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.FileWriter;

public class YamlUtil {
    private static final LoaderOptions lOptions = new LoaderOptions();
    private static final DumperOptions dOptions = new DumperOptions();
    private static final Yaml yaml;

    static {
        // 允许反序列化的类
        TagInspector taginspector = tag ->
                // Rule
                tag.getClassName().equals(Rule.class.getName()) ||
                        // SearchCondition
                        tag.getClassName().equals(SearchCondition.class.getName());
        lOptions.setTagInspector(taginspector);
        // 输出格式
        dOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dOptions.setPrettyFlow(true);
        yaml = new Yaml(lOptions, dOptions);
    }

    public static Rule loadAs(byte[] data) {
        return yaml.loadAs(new String(data), Rule.class);
    }

    public static void dumpFile(Rule rule, String output) {
        try {
            yaml.dump(rule, new FileWriter(output));
        } catch (Exception ignored) {
        }
    }
}
