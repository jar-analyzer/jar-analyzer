package me.n1ar4.jar.analyzer.starter;

import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ClassSearcher {
    private static final Logger logger = LogManager.getLogger();

    static void start(String[] args) {
        try {
            Path p = Paths.get("class-searcher-1.0.jar");
            if (!Files.exists(p)) {
                p = Paths.get("lib").resolve(p);
                if (!Files.exists(p)) {
                    logger.error("class-searcher is missing");
                    return;
                }
            }
            URL jarUrl = p.toUri().toURL();
            logger.info("load: {}", jarUrl.toString());
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, ClassSearcher.class.getClassLoader());
            Class<?> mainClass = classLoader.loadClass("org.vidar.ClazzSearchApplication");
            Method mainMethod = mainClass.getMethod("main", String[].class);

            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            System.out.println(Arrays.toString(newArgs));

            mainMethod.invoke(null, (Object) newArgs);
            classLoader.close();
        } catch (Exception ex) {
            logger.error("start class searcher error: {}", ex.toString());
        }
    }
}
