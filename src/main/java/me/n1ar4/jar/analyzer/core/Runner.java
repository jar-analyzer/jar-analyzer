package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.core.asm.StringClassVisitor;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.env.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.spring.SpringService;
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

public class Runner {
    private static final Logger logger = LogManager.getLogger();

    public static void run(Path jarPath, Path rtJarPath) {
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
                DB.saveJar(s);
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
            DB.saveJar(jarList.get(0));
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
        Env.classFileList.addAll(cfs);
        logger.info("get all class");
        LogUtil.log("get all class");
        DB.saveClassFiles(Env.classFileList);
        MainForm.getInstance().getBuildBar().setValue(20);
        Discovery.start(Env.classFileList, Env.discoveredClasses,
                Env.discoveredMethods, Env.classMap, Env.methodMap);
        DB.saveClassInfo(Env.discoveredClasses);
        MainForm.getInstance().getBuildBar().setValue(25);
        DB.saveMethods(Env.discoveredMethods);
        MainForm.getInstance().getBuildBar().setValue(30);
        logger.info("analyze class finish");
        LogUtil.log("analyze class finish");
        for (MethodReference mr : Env.discoveredMethods) {
            ClassReference.Handle ch = mr.getClassReference();
            if (Env.methodsInClassMap.get(ch) == null) {
                List<MethodReference> ml = new ArrayList<>();
                ml.add(mr);
                Env.methodsInClassMap.put(ch, ml);
            } else {
                List<MethodReference> ml = Env.methodsInClassMap.get(ch);
                ml.add(mr);
                Env.methodsInClassMap.put(ch, ml);
            }
        }
        MainForm.getInstance().getBuildBar().setValue(35);
        MethodCall.start(Env.classFileList, Env.methodCalls);
        MainForm.getInstance().getBuildBar().setValue(40);
        Env.inheritanceMap = Inheritance.derive(Env.classMap);
        MainForm.getInstance().getBuildBar().setValue(50);
        logger.info("build inheritance");
        LogUtil.log("build inheritance");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> implMap =
                Inheritance.getAllMethodImplementations(Env.inheritanceMap, Env.methodMap);
        DB.saveImpls(implMap);
        MainForm.getInstance().getBuildBar().setValue(60);
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry :
                implMap.entrySet()) {
            MethodReference.Handle k = entry.getKey();
            Set<MethodReference.Handle> v = entry.getValue();
            HashSet<MethodReference.Handle> calls = Env.methodCalls.get(k);
            calls.addAll(v);
        }
        DB.saveMethodCalls(Env.methodCalls);
        MainForm.getInstance().getBuildBar().setValue(70);
        logger.info("build extra inheritance");
        LogUtil.log("build extra inheritance");
        for (ClassFileEntity file : Env.classFileList) {
            try {
                StringClassVisitor dcv = new StringClassVisitor(Env.strMap, Env.classMap, Env.methodMap);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
            } catch (Exception ex) {
                logger.error("string analyze error: {}", ex.toString());
            }
        }
        MainForm.getInstance().getBuildBar().setValue(80);
        DB.saveStrMap(Env.strMap);

        SpringService.start(Env.classFileList, Env.controllers, Env.classMap, Env.methodMap);
        DB.saveSpring(Env.controllers);

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
        config.setGptHost("none");
        config.setGptKey("none");
        MainForm.setConfig(config);
        MainForm.setEngine(new CoreEngine(config));

        if (MainForm.getInstance().getAutoSaveCheckBox().isSelected()) {
            ConfigEngine.saveConfig(config);
            logger.info("auto save finish");
            LogUtil.log("auto save finish");
        }

        MainForm.getInstance().getFileTree().refresh();

        // GC
        Env.classFileList.clear();
        Env.discoveredClasses.clear();
        Env.discoveredMethods.clear();
        Env.methodsInClassMap.clear();
        Env.classMap.clear();
        Env.methodMap.clear();
        Env.methodCalls.clear();
        Env.strMap.clear();
        Env.inheritanceMap.getInheritanceMap().clear();
        Env.inheritanceMap.getSubClassMap().clear();
        System.gc();
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
