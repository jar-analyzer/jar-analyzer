package me.n1ar4.jar.analyzer.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigEngine {
    private static final Logger logger = LogManager.getLogger();
    public static final String CONFIG_FILE_PATH = ".jar-analyzer";

    public static boolean exist() {
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        return Files.exists(configPath);
    }

    public static ConfigFile parseConfig() {
        try {
            LoaderOptions loaderOptions = new LoaderOptions();
            TagInspector taginspector =
                    tag -> tag.getClassName().equals(ConfigFile.class.getName());
            loaderOptions.setTagInspector(taginspector);
            Yaml yaml = new Yaml(new Constructor(ConfigFile.class, loaderOptions));
            Path configPath = Paths.get(CONFIG_FILE_PATH);
            if (!Files.exists(configPath)) {
                return null;
            }
            Object obj = yaml.load(new String(Files.readAllBytes(configPath)));
            return (ConfigFile) obj;
        } catch (Exception ex) {
            logger.error("parse yaml error: {}", ex.toString());
        }
        return null;
    }

    public static void saveConfig(ConfigFile configFile) {
        try {
            Path configPath = Paths.get(CONFIG_FILE_PATH);
            Yaml yaml = new Yaml();
            String yamlStr = yaml.dump(configFile);
            Files.write(configPath, yamlStr.getBytes());
        } catch (Exception ex) {
            logger.error("save yaml error: {}", ex.toString());
        }
    }
}
