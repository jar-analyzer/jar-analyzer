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
