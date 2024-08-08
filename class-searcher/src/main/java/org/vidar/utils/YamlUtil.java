package org.vidar.utils;

import org.vidar.rules.ClazzRule;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * @author zhchen
 */
public class YamlUtil {

    private static final Yaml yaml = new Yaml();

    //    public static void test() {
//        path2ClazzRule("sad");
//    }
    public static ClazzRule path2ClazzRule(String path) {
        File file = new File(path);
        return file2ClazzRule(file);
    }

    public static ClazzRule file2ClazzRule(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return yaml.loadAs(fileInputStream, ClazzRule.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
