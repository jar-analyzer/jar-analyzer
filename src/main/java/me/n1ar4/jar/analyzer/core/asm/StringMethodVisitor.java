package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringMethodVisitor extends MethodVisitor {
    private final String ownerName;
    private final String methodName;
    private final String methodDesc;
    private final Map<MethodReference.Handle, List<String>> strMap;
    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final Map<MethodReference.Handle, MethodReference> methodMap;

    public StringMethodVisitor(int api, MethodVisitor methodVisitor,
                               String owner, String methodName, String desc,
                               Map<MethodReference.Handle, List<String>> strMap,
                               Map<ClassReference.Handle, ClassReference> classMap,
                               Map<MethodReference.Handle, MethodReference> methodMap) {
        super(api, methodVisitor);
        this.strMap = strMap;
        this.ownerName = owner;
        this.methodName = methodName;
        this.methodDesc = desc;
        this.classMap = classMap;
        this.methodMap = methodMap;
    }

    public static boolean isPrintable(String str) {
        return str.matches("\\A\\p{Print}+\\z");
    }

    @Override
    public void visitLdcInsn(Object o) {
        if (o instanceof String) {
            MethodReference mr = null;
            ClassReference.Handle ch = new ClassReference.Handle(ownerName);
            if (classMap.get(ch) != null) {
                MethodReference m = methodMap.get(new MethodReference.Handle(ch, methodName, methodDesc));
                if (m != null) {
                    mr = m;
                }
            }
            if (mr == null) {
                return;
            }
            String str = (String) o;
            if (str.trim().isEmpty()) {
                return;
            }
            if (!isPrintable(str)) {
                return;
            }
            List<String> mList = strMap.getOrDefault(mr.getHandle(), new ArrayList<>());
            mList.add(str);
            strMap.put(mr.getHandle(), mList);
        }
        super.visitLdcInsn(o);
    }
}
