package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.analyze.spring.SpringService;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.core.asm.FixClassVisitor;
import me.n1ar4.jar.analyzer.core.asm.StringClassVisitor;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.CoreUtil;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.IOUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class CoreRunner {
    private static final Logger logger = LogManager.getLogger();

    public static void run(Path jarPath, Path rtJarPath, boolean fixClass) {
        // 2024-07-05 不允许太大的 JAR 文件
        long totalSize = 0;
        if (Files.isDirectory(jarPath)) {
            List<String> files = DirUtil.GetFiles(jarPath.toAbsolutePath().toString());
            if (rtJarPath != null) {
                files.add(rtJarPath.toAbsolutePath().toString());
            }
            for (String s : files) {
                if (s.toLowerCase().endsWith(".jar") || s.toLowerCase().endsWith(".war")) {
                    totalSize += Paths.get(s).toFile().length();
                }
            }
        } else {
            List<String> jarList = new ArrayList<>();
            if (rtJarPath != null) {
                jarList.add(rtJarPath.toAbsolutePath().toString());
            }
            jarList.add(jarPath.toAbsolutePath().toString());
            for (String s : jarList) {
                if (s.toLowerCase().endsWith(".jar") || s.toLowerCase().endsWith(".war")) {
                    totalSize += Paths.get(s).toFile().length();
                }
            }
        }

        int totalM = (int) (totalSize / 1024 / 1024);

        int chose;
        if (totalM > 1024) {
            // 对于大于 1G 的 JAR 输入进行提示
            chose = JOptionPane.showConfirmDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>加载 JAR/WAR 总大小 <strong>" + totalM + "</strong> MB<br>" +
                            "文件内容过大，可能产生巨大的临时文件和数据库，可能非常消耗内存<br>" +
                            "请确认是否要继续进行分析" +
                            "</html>");
        } else {
            chose = JOptionPane.showConfirmDialog(MainForm.getInstance().getMasterPanel(),
                    "加载 JAR/WAR 总大小 " + totalM + " MB 是否继续");
        }
        if (chose != 0) {
            MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(true);
            return;
        }

        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);

        List<ClassFileEntity> cfs;
        MainForm.getInstance().getBuildBar().setValue(10);
        if (Files.isDirectory(jarPath)) {
            logger.info("input is a dir");
            LogUtil.info("input is a dir");
            List<String> files = DirUtil.GetFiles(jarPath.toAbsolutePath().toString());
            if (rtJarPath != null) {
                files.add(rtJarPath.toAbsolutePath().toString());
                LogUtil.info("analyze with rt.jar file");
            }
            MainForm.getInstance().getTotalJarVal().setText(String.valueOf(files.size()));
            for (String s : files) {
                if (s.toLowerCase().endsWith(".jar")) {
                    DatabaseManager.saveJar(s);
                }
            }
            cfs = CoreUtil.getAllClassesFromJars(files);
        } else {
            logger.info("input is a jar file");
            LogUtil.info("input is a jar");

            List<String> jarList = new ArrayList<>();
            if (rtJarPath != null) {
                jarList.add(rtJarPath.toAbsolutePath().toString());
                MainForm.getInstance().getTotalJarVal().setText("2");
                LogUtil.info("analyze with rt.jar file");
            } else {
                MainForm.getInstance().getTotalJarVal().setText("1");
            }

            MainForm.getInstance().getTotalJarVal().setText("1");
            jarList.add(jarPath.toAbsolutePath().toString());
            for (String s : jarList) {
                DatabaseManager.saveJar(s);
            }
            cfs = CoreUtil.getAllClassesFromJars(jarList);
        }
        // BUG CLASS NAME
        for (ClassFileEntity cf : cfs) {
            String className = cf.getClassName();
            int i = className.indexOf("classes");
            if (className.contains("BOOT-INF")) {
                className = className.substring(i + 8);
            } else if (className.contains("WEB-INF")) {
                className = className.substring(i + 7);
            }
            if (fixClass) {
                // fix class name
                Path parPath = Paths.get(Const.tempDir);
                FixClassVisitor cv = new FixClassVisitor();
                ClassReader cr = new ClassReader(cf.getFile());
                cr.accept(cv, Const.AnalyzeASMOptions);
                // get actual class name
                Path path = parPath.resolve(Paths.get(cv.getName()));
                File file = path.toFile();
                // write file
                if (!file.getParentFile().mkdirs()) {
                    logger.error("fix class mkdirs error");
                }
                className = file.getPath() + ".class";
                try {
                    IOUtil.copy(new ByteArrayInputStream(cf.getFile()),
                            new FileOutputStream(className));
                } catch (FileNotFoundException ignored) {
                    logger.error("fix path copy bytes error");
                }
                cf.setClassName(className);
                cf.setPath(Paths.get(className));
            } else {
                cf.setClassName(className);
            }
        }

        MainForm.getInstance().getBuildBar().setValue(15);
        AnalyzeEnv.classFileList.addAll(cfs);
        logger.info("get all class");
        LogUtil.info("get all class");
        DatabaseManager.saveClassFiles(AnalyzeEnv.classFileList);
        MainForm.getInstance().getBuildBar().setValue(20);
        DiscoveryRunner.start(AnalyzeEnv.classFileList, AnalyzeEnv.discoveredClasses,
                AnalyzeEnv.discoveredMethods, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
        DatabaseManager.saveClassInfo(AnalyzeEnv.discoveredClasses);
        MainForm.getInstance().getBuildBar().setValue(25);
        DatabaseManager.saveMethods(AnalyzeEnv.discoveredMethods);
        MainForm.getInstance().getBuildBar().setValue(30);
        logger.info("analyze class finish");
        LogUtil.info("analyze class finish");
        for (MethodReference mr : AnalyzeEnv.discoveredMethods) {
            ClassReference.Handle ch = mr.getClassReference();
            if (AnalyzeEnv.methodsInClassMap.get(ch) == null) {
                List<MethodReference> ml = new ArrayList<>();
                ml.add(mr);
                AnalyzeEnv.methodsInClassMap.put(ch, ml);
            } else {
                List<MethodReference> ml = AnalyzeEnv.methodsInClassMap.get(ch);
                ml.add(mr);
                AnalyzeEnv.methodsInClassMap.put(ch, ml);
            }
        }
        MainForm.getInstance().getBuildBar().setValue(35);
        MethodCallRunner.start(AnalyzeEnv.classFileList, AnalyzeEnv.methodCalls);
        MainForm.getInstance().getBuildBar().setValue(40);
        AnalyzeEnv.inheritanceMap = InheritanceRunner.derive(AnalyzeEnv.classMap);
        MainForm.getInstance().getBuildBar().setValue(50);
        logger.info("build inheritance");
        LogUtil.info("build inheritance");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> implMap =
                InheritanceRunner.getAllMethodImplementations(AnalyzeEnv.inheritanceMap, AnalyzeEnv.methodMap);
        DatabaseManager.saveImpls(implMap);
        MainForm.getInstance().getBuildBar().setValue(60);
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry :
                implMap.entrySet()) {
            MethodReference.Handle k = entry.getKey();
            Set<MethodReference.Handle> v = entry.getValue();
            HashSet<MethodReference.Handle> calls = AnalyzeEnv.methodCalls.get(k);
            calls.addAll(v);
        }
        DatabaseManager.saveMethodCalls(AnalyzeEnv.methodCalls);
        MainForm.getInstance().getBuildBar().setValue(70);
        logger.info("build extra inheritance");
        LogUtil.info("build extra inheritance");
        for (ClassFileEntity file : AnalyzeEnv.classFileList) {
            try {
                StringClassVisitor dcv = new StringClassVisitor(AnalyzeEnv.strMap, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, Const.AnalyzeASMOptions);
            } catch (Exception ex) {
                logger.error("string analyze error: {}", ex.toString());
            }
        }
        MainForm.getInstance().getBuildBar().setValue(80);
        DatabaseManager.saveStrMap(AnalyzeEnv.strMap);

        SpringService.start(AnalyzeEnv.classFileList, AnalyzeEnv.controllers, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
        DatabaseManager.saveSpring(AnalyzeEnv.controllers);

        MainForm.getInstance().getBuildBar().setValue(90);
        logger.info("build database finish");
        LogUtil.info("build database finish");

        long fileSizeBytes = getFileSize();
        String fileSizeMB = formatSizeInMB(fileSizeBytes);
        MainForm.getInstance().getDatabaseSizeVal().setText(fileSizeMB);
        MainForm.getInstance().getBuildBar().setValue(100);
        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);

        MainForm.getInstance().getEngineVal().setText("RUNNING");
        MainForm.getInstance().getEngineVal().setForeground(Color.GREEN);

        MainForm.getInstance().getLoadDBText().setText(Const.dbFile);

        ConfigFile config = MainForm.getConfig();
        if (config == null) {
            config = new ConfigFile();
        }
        config.setTotalMethod(MainForm.getInstance().getTotalMethodVal().getText());
        config.setTotalClass(MainForm.getInstance().getTotalClassVal().getText());
        config.setTotalJar(MainForm.getInstance().getTotalJarVal().getText());
        config.setTempPath(Const.tempDir);
        config.setDbPath(Const.dbFile);
        config.setJarPath(MainForm.getInstance().getFileText().getText());
        config.setDbSize(fileSizeMB);
        config.setLang("en");
        MainForm.setConfig(config);
        MainForm.setEngine(new CoreEngine(config));

        if (MainForm.getInstance().getAutoSaveCheckBox().isSelected()) {
            ConfigEngine.saveConfig(config);
            logger.info("auto save finish");
            LogUtil.info("auto save finish");
        }

        MainForm.getInstance().getFileTree().refresh();

        // GC
        AnalyzeEnv.classFileList.clear();
        AnalyzeEnv.discoveredClasses.clear();
        AnalyzeEnv.discoveredMethods.clear();
        AnalyzeEnv.methodsInClassMap.clear();
        AnalyzeEnv.classMap.clear();
        AnalyzeEnv.methodMap.clear();
        AnalyzeEnv.methodCalls.clear();
        AnalyzeEnv.strMap.clear();
        AnalyzeEnv.inheritanceMap.getInheritanceMap().clear();
        AnalyzeEnv.inheritanceMap.getSubClassMap().clear();
        AnalyzeEnv.controllers.clear();
        System.gc();

        CoreHelper.refreshSpringC();
    }

    private static long getFileSize() {
        File file = new File(Const.dbFile);
        return file.length();
    }

    private static String formatSizeInMB(long fileSizeBytes) {
        double fileSizeMB = (double) fileSizeBytes / (1024 * 1024);
        return String.format("%.2f MB", fileSizeMB);
    }
}
