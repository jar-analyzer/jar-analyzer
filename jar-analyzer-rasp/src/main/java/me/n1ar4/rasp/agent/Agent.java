package me.n1ar4.rasp.agent;

import me.n1ar4.rasp.agent.core.Configuration;
import me.n1ar4.rasp.agent.core.CoreTransformer;
import me.n1ar4.rasp.agent.utils.Log;

import javax.naming.InitialContext;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

public class Agent {
    public static void premain(String agentArgs, Instrumentation ins) {
        try {
            ins.appendToBootstrapClassLoaderSearch(
                    new JarFile(new File(Agent.class.getProtectionDomain()
                            .getCodeSource().getLocation().getFile())));
        } catch (Exception ignored) {
        }
        Log.info("启动Agent");
        boolean success = Configuration.init();
        if (success) {
            Log.info("加载配置文件成功");
        } else {
            Log.error("加载配置文件失败");
            return;
        }
        ClassFileTransformer coreTransformer = new CoreTransformer();
        ins.addTransformer(coreTransformer, true);
        Log.info("添加Transformer完成");
        try {
            ins.retransformClasses(ProcessBuilder.class);
            ins.retransformClasses(InitialContext.class);
        } catch (UnmodifiableClassException ex) {
            Log.error(ex.toString());
        }
    }
}