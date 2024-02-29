package org.vidar.test;

import org.vidar.rules.ClazzRule;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TestYaml {
    public static void main(String[] args) throws FileNotFoundException {
        // 读取 YAML 文件
//        FileInputStream input = new FileInputStream("/Users/zhchen/Downloads/github-workspace/ClazzSearcher/src/main/resources/template.yml");
//        FileInputStream input = new FileInputStream("/Users/zhchen/Downloads/github-workspace/ClazzSearcher/src/main/resources/parent.yml");
//        FileInputStream input = new FileInputStream("/Users/zhchen/Downloads/github-workspace/ClazzSearcher/src/main/resources/annotation.yml");
        FileInputStream input = new FileInputStream("/Users/zhchen/Downloads/github-workspace/ClazzSearcher/src/main/resources/name.yml");
        // 创建 SnakeYAML 解析器
        Yaml yaml = new Yaml();
        // 将 YAML 转换为 Java 对象
        ClazzRule clazzRule = yaml.loadAs(input, ClazzRule.class);
        System.out.println(clazzRule);
    }
}
