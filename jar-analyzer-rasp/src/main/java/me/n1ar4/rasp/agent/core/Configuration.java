package me.n1ar4.rasp.agent.core;

import me.n1ar4.rasp.agent.ent.Config;
import me.n1ar4.rasp.agent.ent.HookInfo;
import me.n1ar4.rasp.agent.utils.Log;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static final Yaml yaml;
    private static Config config;

    static {
        DumperOptions options = new DumperOptions();
        options.setExplicitStart(false);
        options.setExplicitEnd(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
    }

    public static List<HookInfo> getHookInfoByClassName(String className) {
        if (config == null || config.getHooks() == null || config.getHooks().isEmpty()) {
            return null;
        }
        List<HookInfo> hooks = new ArrayList<>();
        for (HookInfo h : config.getHooks()) {
            if (h.getClassName().equals(className)) {
                hooks.add(h);
            }
        }
        return hooks;
    }

    public static boolean shouldBlock() {
        if (config == null) {
            return false;
        }
        return config.isBlock();
    }

    public static boolean isDebug() {
        if (config == null) {
            return false;
        }
        return config.isDebug();
    }

    public static boolean init() {
        Path path = Paths.get("config.yaml");
        if (!Files.exists(path)) {
            Log.warn("配置文件不存在");
            Config c = getConfig();
            String data = yaml.dump(c);

            String[] splits = data.split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < splits.length; i++) {
                sb.append(splits[i]);
                sb.append("\n");
            }
            String finalStr = sb.toString().trim();

            try {
                Files.write(path, finalStr.getBytes());
                Log.info("已生成配置文件请重启");
            } catch (Exception ex) {
                Log.error("无法生成配置文件");
            }
            System.exit(0);
        }
        try {
            byte[] b = Files.readAllBytes(path);
            config = yaml.loadAs(new String(b), Config.class);
            return true;
        } catch (Exception ex) {
            Log.error("无法处理配置文件: %s", ex.toString());
        }
        return false;
    }

    private static Config getConfig() {
        Config c = new Config();
        c.setDebug(false);
        c.setBlock(true);
        List<HookInfo> hooks = new ArrayList<>();

        HookInfo hook1 = new HookInfo();
        hook1.setClassName("java/lang/ProcessBuilder");
        hook1.setMethodName("start");
        hook1.setMethodDesc("()Ljava/lang/Process;");
        hook1.setVulTYpe("RCE");
        hooks.add(hook1);

        HookInfo hook2 = new HookInfo();
        hook2.setClassName("javax/naming/InitialContext");
        hook2.setMethodName("lookup");
        hook2.setMethodDesc("(Ljava/lang/String;)Ljava/lang/Object;");
        hook2.setVulTYpe("JNDI");
        hooks.add(hook2);

        HookInfo hook3 = new HookInfo();
        hook3.setClassName("java/io/ObjectInputStream");
        hook3.setMethodName("readObject");
        hook3.setMethodDesc("()Ljava/lang/Object;");
        hook3.setVulTYpe("Deserialization");
        hooks.add(hook3);

        c.setHooks(hooks);
        return c;
    }
}
