package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.*;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CoreEngine {
    private static final Logger logger = LogManager.getLogger();
    private final SqlSessionFactory factory;

    public CoreEngine(ConfigFile configFile) {
        if (StringUtil.isNull(configFile.getDbPath())) {
            Path dbPath = Paths.get(configFile.getDbPath());
            if (!dbPath.getFileName().toString().equals("jar-analyzer.db") ||
                    !Files.exists(dbPath)) {
                throw new RuntimeException("start engine error");
            }
        }
        factory = SqlSessionFactoryUtil.sqlSessionFactory;
        logger.info("init core engine finish");
    }

    public ArrayList<MethodResult> getMethodsByClass(String className) {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodMapper.selectMethodsByClassName(className));
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        session.close();
        return results;
    }

    public ClassResult getClassByClass(String className) {
        SqlSession session = factory.openSession(true);
        ClassMapper classMapper = session.getMapper(ClassMapper.class);
        ArrayList<ClassResult> results = new ArrayList<>(classMapper.selectClassByClassName(className));
        session.close();
        return results.isEmpty() ? null : results.get(0);
    }

    public String getAbsPath(String className) {
        SqlSession session = factory.openSession(true);
        ClassFileMapper classMapper = session.getMapper(ClassFileMapper.class);
        className = className + ".class";
        String res = classMapper.selectPathByClass(className);
        session.close();
        return res;
    }

    public ArrayList<MethodResult> getCallers(String calleeClass, String calleeMethod, String calleeDesc) {
        SqlSession session = factory.openSession(true);
        MethodCallMapper methodCallMapper = session.getMapper(MethodCallMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodCallMapper.selectCallers(
                calleeMethod, calleeDesc, calleeClass));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getCallersLike(String calleeClass, String calleeMethod, String calleeDesc) {
        SqlSession session = factory.openSession(true);
        MethodCallMapper methodCallMapper = session.getMapper(MethodCallMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodCallMapper.selectCallersLike(
                calleeMethod, calleeDesc, calleeClass));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getCallee(String callerClass, String callerMethod, String callerDesc) {
        SqlSession session = factory.openSession(true);
        MethodCallMapper methodCallMapper = session.getMapper(MethodCallMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodCallMapper.selectCallee(
                callerMethod, callerDesc, callerClass));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getMethod(String className, String methodName, String methodDesc) {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(
                methodMapper.selectMethods(className, methodName, methodDesc));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getMethodLike(String className, String methodName, String methodDesc) {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(
                methodMapper.selectMethodsLike(className, methodName, methodDesc));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getMethodsByStr(String val) {
        SqlSession session = factory.openSession(true);
        StringMapper stringMapper = session.getMapper(StringMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(
                stringMapper.selectMethodByString(val));
        session.close();
        return results;
    }

    public ArrayList<String> getJarsPath() {
        SqlSession session = factory.openSession(true);
        JarMapper jarMapper = session.getMapper(JarMapper.class);
        ArrayList<String> results = new ArrayList<>(
                jarMapper.selectAllJars());
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getImpls(String className,
                                            String methodName,
                                            String methodDesc) {
        SqlSession session = factory.openSession(true);
        MethodImplMapper methodMapper = session.getMapper(MethodImplMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(
                methodMapper.selectImplClassName(className, methodName, methodDesc));
        session.close();
        return results;
    }

    public ArrayList<MethodResult> getSuperImpls(String className, String methodName, String methodDesc) {
        SqlSession session = factory.openSession(true);
        MethodImplMapper methodMapper = session.getMapper(MethodImplMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(
                methodMapper.selectSuperImpls(className, methodName, methodDesc));
        session.close();
        return results;
    }

    public String getJarByClass(String className) {
        SqlSession session = factory.openSession(true);
        ClassMapper classMapper = session.getMapper(ClassMapper.class);
        String result = classMapper.selectJarByClass(className);
        session.close();
        return result;
    }

    public ArrayList<ClassResult> getAllSpringC() {
        SqlSession session = factory.openSession(true);
        SpringControllerMapper springControllerMapper = session.getMapper(SpringControllerMapper.class);
        List<ClassResult> res = springControllerMapper.selectAllSpringC();
        session.close();
        return new ArrayList<>(res);
    }

    public ArrayList<MethodResult> getSpringM(String className) {
        SqlSession session = factory.openSession(true);
        SpringMethodMapper springMethodMapper = session.getMapper(SpringMethodMapper.class);
        List<MethodResult> res = springMethodMapper.selectMappingsByClassName(className);
        session.close();
        return new ArrayList<>(res);
    }

    public ArrayList<String> getStrings(int page) {
        SqlSession session = factory.openSession(true);
        StringMapper stringMapper = session.getMapper(StringMapper.class);
        int offset = (page - 1) * 100;
        List<String> res = stringMapper.selectStrings(offset);
        session.close();
        return new ArrayList<>(res);
    }

    public int getStringCount() {
        SqlSession session = factory.openSession(true);
        StringMapper stringMapper = session.getMapper(StringMapper.class);
        int res = stringMapper.selectCount();
        session.close();
        return res;
    }

    public ArrayList<MethodResult> getMethodsByClassNoJar(String className) {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodMapper.selectMethodsByClassNameNoJar(className));
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        session.close();
        return results;
    }
}
