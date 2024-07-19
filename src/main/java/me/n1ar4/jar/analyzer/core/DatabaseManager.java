package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.analyze.spring.SpringController;
import me.n1ar4.jar.analyzer.analyze.spring.SpringMapping;
import me.n1ar4.jar.analyzer.core.mapper.*;
import me.n1ar4.jar.analyzer.entity.*;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.jar.analyzer.utils.PartitionUtils;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger();
    public static int PART_SIZE = 100;
    private static final SqlSession session;
    private static final ClassMapper classMapper;
    private static final MemberMapper memberMapper;
    private static final JarMapper jarMapper;
    private static final AnnoMapper annoMapper;
    private static final MethodMapper methodMapper;
    private static final StringMapper stringMapper;
    private static final InterfaceMapper interfaceMapper;
    private static final ClassFileMapper classFileMapper;
    private static final MethodImplMapper methodImplMapper;
    private static final MethodCallMapper methodCallMapper;
    private static final SpringControllerMapper springCMapper;
    private static final SpringMethodMapper springMMapper;

    static {
        logger.info("init database");
        LogUtil.info("init database");
        SqlSessionFactory factory = SqlSessionFactoryUtil.sqlSessionFactory;
        session = factory.openSession(true);
        classMapper = session.getMapper(ClassMapper.class);
        jarMapper = session.getMapper(JarMapper.class);
        annoMapper = session.getMapper(AnnoMapper.class);
        methodMapper = session.getMapper(MethodMapper.class);
        memberMapper = session.getMapper(MemberMapper.class);
        stringMapper = session.getMapper(StringMapper.class);
        classFileMapper = session.getMapper(ClassFileMapper.class);
        interfaceMapper = session.getMapper(InterfaceMapper.class);
        methodCallMapper = session.getMapper(MethodCallMapper.class);
        methodImplMapper = session.getMapper(MethodImplMapper.class);
        springCMapper = session.getMapper(SpringControllerMapper.class);
        springMMapper = session.getMapper(SpringMethodMapper.class);
        InitMapper initMapper = session.getMapper(InitMapper.class);
        initMapper.createJarTable();
        initMapper.createClassTable();
        initMapper.createClassFileTable();
        initMapper.createMemberTable();
        initMapper.createMethodTable();
        initMapper.createAnnoTable();
        initMapper.createInterfaceTable();
        initMapper.createMethodCallTable();
        initMapper.createMethodImplTable();
        initMapper.createStringTable();
        initMapper.createSpringControllerTable();
        initMapper.createSpringMappingTable();
        logger.info("create database finish");
        LogUtil.info("create database finish");
    }

    public static void saveJar(String jarPath) {
        JarEntity en = new JarEntity();
        en.setJarAbsPath(jarPath);
        if (OSUtil.isWindows()) {
            String[] temp = jarPath.split("\\\\");
            en.setJarName(temp[temp.length - 1]);
        } else {
            String[] temp = jarPath.split("/");
            en.setJarName(temp[temp.length - 1]);
        }
        List<JarEntity> js = new ArrayList<>();
        js.add(en);
        int i = jarMapper.insertJar(js);
        if (i != 0) {
            logger.debug("save jar finish");
        }
    }

    public static void saveClassFiles(Set<ClassFileEntity> classFileList) {
        logger.info("total class file: {}", classFileList.size());
        List<ClassFileEntity> list = new ArrayList<>();
        for (ClassFileEntity classFile : classFileList) {
            classFile.setPathStr(classFile.getPath().toAbsolutePath().toString());
            list.add(classFile);
        }
        List<List<ClassFileEntity>> partition = PartitionUtils.partition(list, PART_SIZE);
        for (List<ClassFileEntity> data : partition) {
            int a = classFileMapper.insertClassFile(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save class file finish");
    }

    public static void saveClassInfo(Set<ClassReference> discoveredClasses) {
        logger.info("total class: {}", discoveredClasses.size());
        MainForm.getInstance().getTotalClassVal().setText(String.valueOf(discoveredClasses.size()));
        List<ClassEntity> list = new ArrayList<>();
        for (ClassReference reference : discoveredClasses) {
            ClassEntity classEntity = new ClassEntity();
            classEntity.setJarName(reference.getJar());
            classEntity.setClassName(reference.getName());
            classEntity.setSuperClassName(reference.getSuperClass());
            classEntity.setInterface(reference.isInterface());
            list.add(classEntity);
        }
        List<List<ClassEntity>> partition = PartitionUtils.partition(list, PART_SIZE);
        for (List<ClassEntity> data : partition) {
            int a = classMapper.insertClass(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save class finish");

        List<MemberEntity> mList = new ArrayList<>();
        List<AnnoEntity> aList = new ArrayList<>();
        List<InterfaceEntity> iList = new ArrayList<>();
        for (ClassReference reference : discoveredClasses) {
            for (ClassReference.Member member : reference.getMembers()) {
                MemberEntity memberEntity = new MemberEntity();
                memberEntity.setMemberName(member.getName());
                memberEntity.setModifiers(member.getModifiers());
                memberEntity.setTypeClassName(member.getType().getName());
                memberEntity.setClassName(reference.getName());
                mList.add(memberEntity);
            }
            for (String anno : reference.getAnnotations()) {
                AnnoEntity annoEntity = new AnnoEntity();
                annoEntity.setAnnoName(anno);
                annoEntity.setClassName(reference.getName());
                aList.add(annoEntity);
            }
            for (String inter : reference.getInterfaces()) {
                InterfaceEntity i = new InterfaceEntity();
                i.setClassName(reference.getName());
                i.setInterfaceName(inter);
                iList.add(i);
            }
        }
        List<List<MemberEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);
        for (List<MemberEntity> data : mPartition) {
            int a = memberMapper.insertMember(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save member success");

        saveAnno(aList);
        logger.info("save class anno success");

        List<List<InterfaceEntity>> iPartition = PartitionUtils.partition(iList, PART_SIZE);
        for (List<InterfaceEntity> data : iPartition) {
            int a = interfaceMapper.insertInterface(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save interface success");
    }

    private static void saveAnno(List<AnnoEntity> aList) {
        List<List<AnnoEntity>> aPartition = PartitionUtils.partition(aList, PART_SIZE);
        for (List<AnnoEntity> data : aPartition) {
            int a = annoMapper.insertAnno(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
    }

    public static void saveMethods(Set<MethodReference> discoveredMethods) {
        logger.info("total method: {}", discoveredMethods.size());
        MainForm.getInstance().getTotalMethodVal().setText(String.valueOf(discoveredMethods.size()));
        List<MethodEntity> mList = new ArrayList<>();
        List<AnnoEntity> aList = new ArrayList<>();
        for (MethodReference reference : discoveredMethods) {
            MethodEntity methodEntity = new MethodEntity();
            methodEntity.setMethodName(reference.getName());
            methodEntity.setMethodDesc(reference.getDesc());
            methodEntity.setClassName(reference.getClassReference().getName());
            methodEntity.setStatic(reference.isStatic());
            methodEntity.setAccess(reference.getAccess());
            mList.add(methodEntity);
            for (String anno : reference.getAnnotations()) {
                AnnoEntity annoEntity = new AnnoEntity();
                annoEntity.setAnnoName(anno);
                annoEntity.setMethodName(reference.getName());
                annoEntity.setClassName(reference.getClassReference().getName());
                aList.add(annoEntity);
            }
        }
        List<List<MethodEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);
        for (List<MethodEntity> data : mPartition) {
            int a = methodMapper.insertMethod(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save method success");

        saveAnno(aList);
        logger.info("save method anno success");
    }

    public static void saveMethodCalls(HashMap<MethodReference.Handle,
            HashSet<MethodReference.Handle>> methodCalls) {
        List<MethodCallEntity> mList = new ArrayList<>();
        for (Map.Entry<MethodReference.Handle, HashSet<MethodReference.Handle>> call :
                methodCalls.entrySet()) {
            MethodReference.Handle caller = call.getKey();
            HashSet<MethodReference.Handle> callee = call.getValue();

            for (MethodReference.Handle mh : callee) {
                MethodCallEntity mce = new MethodCallEntity();
                mce.setCallerClassName(caller.getClassReference().getName());
                mce.setCallerMethodName(caller.getName());
                mce.setCallerMethodDesc(caller.getDesc());
                mce.setCalleeClassName(mh.getClassReference().getName());
                mce.setCalleeMethodName(mh.getName());
                mce.setCalleeMethodDesc(mh.getDesc());
                mList.add(mce);
            }
        }

        List<List<MethodCallEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);
        for (List<MethodCallEntity> data : mPartition) {
            int a = methodCallMapper.insertMethodCall(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save method call success");
    }

    public static void saveImpls(Map<MethodReference.Handle, Set<MethodReference.Handle>> implMap) {
        List<MethodImplEntity> mList = new ArrayList<>();
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> call :
                implMap.entrySet()) {
            MethodReference.Handle method = call.getKey();
            Set<MethodReference.Handle> impls = call.getValue();
            for (MethodReference.Handle mh : impls) {
                MethodImplEntity impl = new MethodImplEntity();
                impl.setImplClassName(mh.getClassReference().getName());
                impl.setClassName(method.getClassReference().getName());
                impl.setMethodName(mh.getName());
                impl.setMethodDesc(mh.getDesc());
                mList.add(impl);
            }
        }
        List<List<MethodImplEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);
        for (List<MethodImplEntity> data : mPartition) {
            int a = methodImplMapper.insertMethodImpl(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save method impl success");
    }

    public static void saveStrMap(Map<MethodReference.Handle, List<String>> strMap) {
        List<StringEntity> mList = new ArrayList<>();
        for (Map.Entry<MethodReference.Handle, List<String>> strEntry : strMap.entrySet()) {
            MethodReference.Handle method = strEntry.getKey();
            List<String> strList = strEntry.getValue();
            for (String s : strList) {
                MethodReference mr = AnalyzeEnv.methodMap.get(method);
                ClassReference cr = AnalyzeEnv.classMap.get(mr.getClassReference());
                StringEntity stringEntity = new StringEntity();
                stringEntity.setValue(s);
                stringEntity.setAccess(mr.getAccess());
                stringEntity.setClassName(cr.getName());
                stringEntity.setJarName(cr.getJar());
                stringEntity.setMethodDesc(mr.getDesc());
                stringEntity.setMethodName(mr.getName());
                mList.add(stringEntity);
            }
        }
        List<List<StringEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);
        for (List<StringEntity> data : mPartition) {
            int a = stringMapper.insertString(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        logger.info("save all string success");
    }

    public static void saveSpring(ArrayList<SpringController> controllers) {
        List<SpringControllerEntity> cList = new ArrayList<>();
        List<SpringMethodEntity> mList = new ArrayList<>();
        for (SpringController controller : controllers) {
            SpringControllerEntity ce = new SpringControllerEntity();
            ce.setClassName(controller.getClassName().getName());
            cList.add(ce);
            for (SpringMapping mapping : controller.getMappings()) {
                SpringMethodEntity me = new SpringMethodEntity();
                me.setClassName(controller.getClassName().getName());
                me.setPath(mapping.getPath());
                me.setMethodName(mapping.getMethodName().getName());
                me.setMethodDesc(mapping.getMethodName().getDesc());
                mList.add(me);
            }
        }
        List<List<SpringControllerEntity>> cPartition = PartitionUtils.partition(cList, PART_SIZE);
        for (List<SpringControllerEntity> data : cPartition) {
            int a = springCMapper.insertControllers(data);
            if (a == 0) {
                logger.warn("save error");
            }
        }
        List<List<SpringMethodEntity>> mPartition = PartitionUtils.partition(mList, PART_SIZE);

        for (List<SpringMethodEntity> data : mPartition) {

            // FIX PATH NOT NULL BUG
            List<SpringMethodEntity> newList = new ArrayList<>();
            for (SpringMethodEntity entity : data) {
                if (entity.getPath() == null || entity.getPath().isEmpty()) {
                    entity.setPath("none");
                }
                newList.add(entity);
            }

            int a = springMMapper.insertMappings(newList);
            if (a == 0) {
                logger.warn("save error");
            }
        }

        logger.info("save all spring data success");
    }
}
