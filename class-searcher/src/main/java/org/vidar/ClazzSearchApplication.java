package org.vidar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vidar.discovery.ClassResourceEnumerator;
import org.vidar.discovery.ClazzDiscovery;
import org.vidar.discovery.MetaDiscovery;
import org.vidar.rules.ClazzRule;
import org.vidar.utils.LoaderUtil;
import org.vidar.utils.YamlUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author zhchen
 */
public class ClazzSearchApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClazzSearchApplication.class);

    static {
        try {
            if (!Files.exists(Paths.get("result"))) {
                Files.createDirectory(Paths.get("result"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        printLOGO();
        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        //是否不删除所有的dat文件
        boolean resume = false;
        //是否Spring-Boot jar项目
        boolean boot = false;
        // 搜索target的规则
        ClazzRule clazzRule = null;


        int argIndex = 0;
        while (argIndex < args.length) {
            String arg = args[argIndex];
            if (!arg.startsWith("--")) {
                break;
            }
            switch (arg) {
                case "--f":
                    argIndex++;
                    clazzRule = YamlUtil.path2ClazzRule(args[argIndex]);
                    break;
                case "--boot":
                    boot = true;
                    break;
                case "--resume":
                    resume = true;
                    break;
                case "--onlyJDK":
                    ConfigHelper.onlyJDK = true;
                default:
                    break;
            }
            argIndex++;
        }

        List<Path> pathList = new ArrayList<>();
        ClassLoader classLoader = initJarData(args, boot, argIndex, pathList);
        // 类枚举加载器
        // getRuntimeClasses获取rt.jar的所有的class
        // getAllClasses获取rt.jar以及classLoader加载的class
        final ClassResourceEnumerator classResourceEnumerator = new ClassResourceEnumerator(classLoader);

        // 删除原先的dat文件
        if (!resume) {
            // Delete all existing dat files
            LOGGER.info("Deleting stale data...");
            for (String datFile : Arrays
                    .asList("classes.dat", "methods.dat", "inheritanceMap.dat", "calls.dat")) {
                final Path path = Paths.get(datFile);
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            }
        }

        try {
            // 扫描java runtime所有的class（rt.jar）和指定的jar或war中的所有class
            beginDiscovery(classResourceEnumerator, pathList, clazzRule);
        } catch (Throwable t) {
            t.printStackTrace();
            //捕获异常，避免异常导致程序终止
        }

    }

    private static void printLOGO() {
        System.out.print(" ______  __      ______  ______  ______       ______  ______  ______  ______  ______  __  __  ______  ______    \n" +
                "/\\  ___\\/\\ \\    /\\  __ \\/\\___  \\/\\___  \\     /\\  ___\\/\\  ___\\/\\  __ \\/\\  == \\/\\  ___\\/\\ \\_\\ \\/\\  ___\\/\\  == \\   \n" +
                "\\ \\ \\___\\ \\ \\___\\ \\  __ \\/_/  /_\\/_/  /__    \\ \\___  \\ \\  __\\\\ \\  __ \\ \\  __<\\ \\ \\___\\ \\  __ \\ \\  __\\\\ \\  __<   \n" +
                " \\ \\_____\\ \\_____\\ \\_\\ \\_\\/\\_____\\/\\_____\\    \\/\\_____\\ \\_____\\ \\_\\ \\_\\ \\_\\ \\_\\ \\_____\\ \\_\\ \\_\\ \\_____\\ \\_\\ \\_\\ \n" +
                "  \\/_____/\\/_____/\\/_/\\/_/\\/_____/\\/_____/     \\/_____/\\/_____/\\/_/\\/_/\\/_/ /_/\\/_____/\\/_/\\/_/\\/_____/\\/_/ /_/ \n" +
                "                                                                                                                ");
        System.out.println();
    }


    private static void beginDiscovery(ClassResourceEnumerator classResourceEnumerator, List<Path> pathList, ClazzRule clazzRule) throws Exception {
        if (!Files.exists(Paths.get("classes.dat"))
                || !Files.exists(Paths.get("methods.dat"))
                || !Files.exists(Paths.get("inheritanceMap.dat"))
                || !Files.exists(Paths.get("calls.dat"))) {
            LOGGER.info("Running method discovery...");
            MetaDiscovery methodDiscovery = new MetaDiscovery();
            methodDiscovery.discover(classResourceEnumerator);
            // 保存类信息、方法信息、继承实现信息
            methodDiscovery.save();
        }

        {
            LOGGER.info("Searching target class for yml ...");
            ClazzDiscovery clazzDiscovery = new ClazzDiscovery();
            System.out.println(clazzRule);
            clazzDiscovery.discover(pathList, clazzRule);
        }
    }


    private static ClassLoader initJarData(String[] args, boolean boot, int argIndex, List<Path> pathList) throws IOException {
        ClassLoader classLoader = null;
        if (!ConfigHelper.onlyJDK) {
            // 程序参数的最后部分，即最后一个具有前缀--的参数
            if (args.length == argIndex + 1
                    && args[argIndex].toLowerCase().endsWith(".war")) {
                // 加载war文件
                Path path = Paths.get(args[argIndex]);
                LOGGER.info("Using WAR classpath: " + path);
                // 实现为URLClassLoader, 加载war包下的WEB-INF/lib和WEB-INF/classes
                classLoader = LoaderUtil.getWarClassLoader(path);
            } else if (args.length == argIndex + 1
                    && args[argIndex].toLowerCase().endsWith(".jar")
                    && boot) {
                Path path = Paths.get(args[argIndex]);
                LOGGER.info("Using JAR classpath: " + path);
                //实现为URLClassLoader，加载jar包下的BOOT-INF/lib和BOOT-INF/classes
                classLoader = LoaderUtil.getJarAndLibClassLoader(path);
            } else {
                // 加载jar文件, java命令后部, 可配置多个
                AtomicInteger jarCount = new AtomicInteger(0);
                for (int i = 0; i < args.length - argIndex; i++) {
                    String pathStr = args[argIndex + i];
                    //非.jar结尾，即目录，需要遍历目录找出所有jar文件
                    if (!pathStr.endsWith(".jar")) {
                        File file = Paths.get(pathStr).toFile();
                        if (file == null || !file.exists()) {
                            continue;
                        }
                        Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                if (!file.getFileName().toString().endsWith(".jar")) {
                                    return FileVisitResult.CONTINUE;
                                }
                                File readFile = file.toFile();
                                Path path = Paths.get(readFile.getAbsolutePath());
                                if (Files.exists(path) && jarCount.incrementAndGet() <= ConfigHelper.maxJarCount) {
                                    pathList.add(path);

                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        continue;
                    }
                    Path path = Paths.get(pathStr).toAbsolutePath();
                    if (!Files.exists(path)) {
                        throw new IllegalArgumentException("Invalid jar path: " + path);
                    }
                    pathList.add(path);
                }
                LOGGER.info("Using classpath: " + Arrays.toString(pathList.toArray()));
                // 实现为URLClassLoader, 加载所有指定的jar
                classLoader = LoaderUtil.getJarClassLoader(pathList.toArray(new Path[0]));
            }
        }
        return classLoader;
    }

    public static void printUsage() {
        System.out.println("Usage:");
        System.out.println("java -jar ClazzSearcher.jar [options]");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("--resume\t\tWhether to use existing data cache");
        System.out.println("--f <config.yml>\tSpecify the configuration YAML file");
        System.out.println("--onlyJDK\t\tFilter only in JDK");
        System.out.println("--boot\t\tWhether it is a Spring Boot JAR");
    }
}
