package org.vidar.discovery;

import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vidar.data.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

/**
 * @author zhchen
 */
public class MetaDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDiscovery.class);

    private final List<ClassReference> discoveredClasses = new ArrayList<>();
    private final List<MethodReference> discoveredMethods = new ArrayList<>();

    private final List<GraphCall> discoveredCalls = new ArrayList<>();

    public void save() throws IOException {
        //classes.dat数据格式：
        //类名(例：java/lang/String) 父类 接口A,接口B,接口C 是否接口 字段1!字段1access!字段1类型!字段2!字段2access!字段1类型
        DataLoader.saveData(Paths.get("classes.dat"), new ClassReference.Factory(), discoveredClasses);

        //methods.dat数据格式：
        //类名 方法名 方法描述 是否静态方法
        DataLoader.saveData(Paths.get("methods.dat"), new MethodReference.Factory(), discoveredMethods);

        //calls.dat数据格式：
        //call callers
        DataLoader.saveData(Paths.get("calls.dat"), new GraphCall.Factory(), discoveredCalls);

        //形成 类名(ClassReference.Handle)->类(ClassReference) 的映射关系
        Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        //保存classes.dat和methods.dat的同时，对所有的class进行递归整合，得到集合{class:[subclass]}，
        // class为subclass父类、超类或实现的接口类，保存至inheritanceMap.dat
        // 类名 父类或超类接口 父类或超类接口 ...
        InheritanceDeriver.derive(classMap).save();
    }

    public void discover(final ClassResourceEnumerator classResourceEnumerator) throws Exception {
        for (ClassResource classResource : classResourceEnumerator.getAllClasses()) {
            try (InputStream in = classResource.getInputStream()) {
                ClassReader cr = new ClassReader(in);
                try {
                    //使用asm的ClassVisitor、MethodVisitor，利用观察模式去扫描所有的class和method并记录
                    cr.accept(new DiscoveryClassVisitor(), ClassReader.EXPAND_FRAMES);
                } catch (Exception e) {
                    LOGGER.error("Exception analyzing: " + classResource.getName(), e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DiscoveryClassVisitor extends ClassVisitor {

        private String name;
        private String superName;
        private String[] interfaces;
        boolean isInterface;
        private List<ClassReference.Member> members;//类的所有字段
        private ClassReference.Handle classHandle;
        private Set<String> annotations;

        private DiscoveryClassVisitor() throws SQLException {
            super(Opcodes.ASM6);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            this.superName = superName;
            this.interfaces = interfaces;
            this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
            this.members = new ArrayList<>();
            this.classHandle = new ClassReference.Handle(name);//类名
            annotations = new HashSet<>();
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            annotations.add(descriptor);
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                Type type = Type.getType(desc);
                String typeName;
                if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                    typeName = type.getInternalName();
                } else {
                    typeName = type.getDescriptor();
                }
                members.add(new ClassReference.Member(name, access, new ClassReference.Handle(typeName)));
            }
            return super.visitField(access, name, desc, signature, value);
        }

        private String getAccessModifier(int access) {
            if ((access & Opcodes.ACC_PUBLIC) != 0) {
                return "public";
            } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
                return "private";
            } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
                return "protected";
            } else {
                return "default";
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
            String accessModifier = getAccessModifier(access);
            MethodReference methodReference = new MethodReference(
                    classHandle,//类名
                    name,
                    desc,
                    isStatic,
                    accessModifier);
            //找到一个方法，添加到缓存
            discoveredMethods.add(methodReference);
            MethodReference.Handle handle = methodReference.getHandle();
            GraphCall graphCall = new GraphCall(handle);
            discoveredCalls.add(graphCall);
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM6, methodVisitor) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    graphCall.getCallMethods().add(new MethodReference.Handle(new ClassReference.Handle(owner), name, descriptor));
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }

        @Override
        public void visitEnd() {
            ClassReference classReference = new ClassReference(
                    name,
                    superName,
                    interfaces,
                    isInterface,
                    members.toArray(new ClassReference.Member[members.size()]),
                    annotations);//把所有找到的字段封装
            //找到一个方法遍历完成后，添加类到缓存
            discoveredClasses.add(classReference);

            super.visitEnd();
        }

    }
}


