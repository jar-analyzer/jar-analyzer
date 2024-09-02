/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.engine;

import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.*;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MemberEntity;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreEngine {
    private static final Logger logger = LogManager.getLogger();
    private final SqlSessionFactory factory;

    public boolean isEnabled() {
        Path dbPath = Paths.get(Const.dbFile);
        if (!Files.exists(dbPath)) {
            return false;
        }
        Path tempDir = Paths.get(Const.tempDir);
        if (!Files.exists(tempDir)) {
            return false;
        }
        if (!Files.isDirectory(tempDir)) {
            return false;
        }
        try (Stream<Path> stream = Files.list(tempDir)) {
            List<Path> files = stream.collect(Collectors.toList());
            if (files.size() == 1 && "console.dll".equals(files.get(0).getFileName().toString())) {
                return false;
            } else {
                return files.size() > 1;
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public CoreEngine(ConfigFile configFile) {
        if (StringUtil.isNull(configFile.getDbPath())) {
            Path dbPath = Paths.get(configFile.getDbPath());
            if (!dbPath.getFileName().toString().equals("jar-analyzer.db") ||
                    !Files.exists(dbPath)) {
                throw new RuntimeException("start engine error");
            }
        }
        factory = SqlSessionFactoryUtil.sqlSessionFactory;
        // 开启 二级缓存
        // 因为数据库不涉及修改操作 仅查询 不会变化 开二级缓存没有问题
        factory.getConfiguration().setCacheEnabled(true);
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

    public int updateMethod(String className, String methodName, String methodDesc, String newItem) {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        int res = methodMapper.updateMethod(className, methodName, methodDesc, newItem);
        session.close();
        return res;
    }

    public Set<ClassReference.Handle> getSuperClasses(ClassReference.Handle ch) {
        SqlSession session = factory.openSession(true);
        ClassMapper classMapper = session.getMapper(ClassMapper.class);
        List<String> tempRes = classMapper.selectSuperClassesByClassName(ch.getName());
        Set<ClassReference.Handle> set = new HashSet<>();
        for (String temp : tempRes) {
            List<ClassResult> cl = classMapper.selectClassByClassName(temp);
            for (ClassResult cr : cl) {
                set.add(new ClassReference.Handle(cr.getClassName()));
            }
        }
        session.close();
        return set;
    }

    public Set<ClassReference.Handle> getSubClasses(ClassReference.Handle ch) {
        SqlSession session = factory.openSession(true);
        ClassMapper classMapper = session.getMapper(ClassMapper.class);
        List<String> tempRes = classMapper.selectSubClassesByClassName(ch.getName());
        Set<ClassReference.Handle> set = new HashSet<>();
        for (String temp : tempRes) {
            List<ClassResult> cl = classMapper.selectClassByClassName(temp);
            for (ClassResult cr : cl) {
                set.add(new ClassReference.Handle(cr.getClassName()));
            }
        }
        session.close();
        return set;
    }

    public ClassReference getClassRef(ClassReference.Handle ch) {
        SqlSession session = factory.openSession(true);
        ClassMapper classMapper = session.getMapper(ClassMapper.class);
        InterfaceMapper interfaceMapper = session.getMapper(InterfaceMapper.class);
        AnnoMapper annoMapper = session.getMapper(AnnoMapper.class);
        MemberMapper memberMapper = session.getMapper(MemberMapper.class);

        ArrayList<ClassResult> results = new ArrayList<>(classMapper.selectClassByClassName(ch.getName()));
        ClassResult cr = results.get(0);

        ArrayList<String> interfaces = interfaceMapper.selectInterfacesByClass(ch.getName());
        ArrayList<String> anno = annoMapper.selectAnnoByClassName(ch.getName());
        ArrayList<MemberEntity> memberEntities = memberMapper.selectMembersByClass(ch.getName());
        ArrayList<ClassReference.Member> members = new ArrayList<>();
        for (MemberEntity me : memberEntities) {
            ClassReference.Member member = new ClassReference.Member
                    (me.getMemberName(), me.getModifiers(),
                            new ClassReference.Handle(me.getTypeClassName()));
            members.add(member);
        }

        session.close();
        return new ClassReference(
                cr.getClassName(),
                cr.getSuperClassName(),
                interfaces,
                cr.getIsInterfaceInt() == 1,
                members,
                new HashSet<>(anno),
                "none");
    }

    public ArrayList<MethodReference> getAllMethodRef() {
        SqlSession session = factory.openSession(true);
        MethodMapper methodMapper = session.getMapper(MethodMapper.class);
        AnnoMapper annoMapper = session.getMapper(AnnoMapper.class);
        ArrayList<MethodResult> results = new ArrayList<>(methodMapper.selectAllMethods());
        results.sort(Comparator.comparing(MethodResult::getMethodName));
        ArrayList<MethodReference> list = new ArrayList<>();
        for (MethodResult result : results) {
            MethodReference.Handle mh = new MethodReference.Handle(
                    new ClassReference.Handle(result.getClassName()),
                    result.getMethodName(),
                    result.getMethodDesc());
            ArrayList<String> ma = annoMapper.selectAnnoByClassAndMethod(
                    mh.getClassReference().getName(), mh.getName());
            MethodReference mr = new MethodReference(mh.getClassReference(),
                    mh.getName(), mh.getDesc(),
                    result.getIsStaticInt() == 1,
                    new HashSet<>(ma), result.getAccessInt(), result.getLineNumber());
            list.add(mr);
        }
        session.close();
        return list;
    }
}
