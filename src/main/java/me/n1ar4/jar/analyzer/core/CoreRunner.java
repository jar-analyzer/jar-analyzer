package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.analyze.spring.SpringService;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.core.asm.StringClassVisitor;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.CoreUtil;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;

public class CoreRunner {
    private static final Logger logger = LogManager.getLogger();

    public static void run(Path jarPath, Path rtJarPath) {
        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);

        List<ClassFileEntity> cfs;
        MainForm.getInstance().getBuildBar().setValue(10);
        if (Files.isDirectory(jarPath)) {
            logger.info("input is a jar dir");
            LogUtil.log("input is a jar dir");
            List<String> files = DirUtil.GetFiles(jarPath.toAbsolutePath().toString());

            if (rtJarPath != null) {
                files.add(rtJarPath.toAbsolutePath().toString());
                LogUtil.log("analyze with rt.jar file");
            }

            MainForm.getInstance().getTotalJarVal().setText(String.valueOf(files.size()));
            for (String s : files) {
                DatabaseManager.saveJar(s);
            }
            cfs = CoreUtil.getAllClassesFromJars(files);
        } else {
            logger.info("input is a jar file");
            LogUtil.log("input is a jar");

            List<String> jarList = new ArrayList<>();
            if (rtJarPath != null) {
                jarList.add(rtJarPath.toAbsolutePath().toString());
                MainForm.getInstance().getTotalJarVal().setText("2");
                LogUtil.log("analyze with rt.jar file");
            } else {
                MainForm.getInstance().getTotalJarVal().setText("1");
            }

            MainForm.getInstance().getTotalJarVal().setText("1");
            jarList.add(jarPath.toAbsolutePath().toString());
            DatabaseManager.saveJar(jarList.get(0));
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
            cf.setClassName(className);
        }

        MainForm.getInstance().getBuildBar().setValue(15);
        AnalyzeEnv.classFileList.addAll(cfs);
        logger.info("get all class");
        LogUtil.log("get all class");
        DatabaseManager.saveClassFiles(AnalyzeEnv.classFileList);
        MainForm.getInstance().getBuildBar().setValue(20);
        DiscoveryRunner.start(AnalyzeEnv.classFileList, AnalyzeEnv.discoveredClasses,
                AnalyzeEnv.discoveredMethods, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
        DatabaseManager.saveClassInfo(AnalyzeEnv.discoveredClasses);
        MainForm.getInstance().getBuildBar().setValue(25);
        DatabaseManager.saveMethods(AnalyzeEnv.discoveredMethods);
        MainForm.getInstance().getBuildBar().setValue(30);
        logger.info("analyze class finish");
        LogUtil.log("analyze class finish");
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
        LogUtil.log("build inheritance");
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
        LogUtil.log("build extra inheritance");
        for (ClassFileEntity file : AnalyzeEnv.classFileList) {
            try {
                StringClassVisitor dcv = new StringClassVisitor(AnalyzeEnv.strMap, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
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
        LogUtil.log("build database finish");

        long fileSizeBytes = getFileSize();
        String fileSizeMB = formatSizeInMB(fileSizeBytes);
        MainForm.getInstance().getDatabaseSizeVal().setText(fileSizeMB);
        MainForm.getInstance().getBuildBar().setValue(100);
        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(true);

        MainForm.getInstance().getEngineVal().setText("RUNNING");
        MainForm.getInstance().getEngineVal().setForeground(Color.GREEN);

        MainForm.getInstance().getLoadDBText().setText(Const.dbFile);

        ConfigFile config = new ConfigFile();
        config.setTotalMethod(MainForm.getInstance().getTotalMethodVal().getText());
        config.setTotalClass(MainForm.getInstance().getTotalClassVal().getText());
        config.setTotalJar(MainForm.getInstance().getTotalJarVal().getText());
        config.setTempPath(Const.tempDir);
        config.setDbPath(Const.dbFile);
        config.setJarPath(MainForm.getInstance().getFileText().getText());
        config.setDbSize(fileSizeMB);
        MainForm.setConfig(config);
        MainForm.setEngine(new CoreEngine(config));

        if (MainForm.getInstance().getAutoSaveCheckBox().isSelected()) {
            ConfigEngine.saveConfig(config);
            logger.info("auto save finish");
            LogUtil.log("auto save finish");
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
