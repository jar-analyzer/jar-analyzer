package me.n1ar4.jar.analyzer.el;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MethodELProcessor {
    private final ClassReference.Handle ch;
    private final MethodReference mr;
    private final ConcurrentLinkedQueue<ResObj> searchList;
    private final MethodEL condition;

    public MethodELProcessor(ClassReference.Handle ch, MethodReference mr,
                             ConcurrentLinkedQueue<ResObj> searchList, MethodEL condition) {
        this.ch = ch;
        this.mr = mr;
        this.searchList = searchList;
        this.condition = condition;
    }

    public void process() {
        Set<ClassReference.Handle> subs = MainForm.getEngine().getSuperClasses(ch);
        Set<ClassReference.Handle> supers = MainForm.getEngine().getSubClasses(ch);

        String classCon = condition.getClassNameContains();
        String mnCon = condition.getNameContains();
        String retCon = condition.getReturnType();
        Map<Integer, String> paramMap = condition.getParamTypes();

        String methodAnno = condition.getMethodAnno();
        String classAnno = condition.getClassAnno();

        String isSubOf = condition.getIsSubClassOf();
        String isSuperOf = condition.getIsSuperClassOf();

        String start = condition.getStartWith();
        String endWith = condition.getEndWith();

        String hasField = condition.getField();

        Integer i = condition.getParamsNum();
        Boolean f = condition.isStatic();
        int paramNum = Type.getMethodType(mr.getDesc()).getArgumentTypes().length;
        String ret = Type.getReturnType(mr.getDesc()).getClassName();

        boolean aa = true;
        boolean ab = true;
        boolean ac = true;
        boolean ad = true;
        boolean ae = true;
        boolean af = true;
        boolean ag = true;
        boolean ah = true;
        boolean ai = true;

        boolean sb = true;
        boolean sp = true;

        boolean sw = true;
        boolean ew = true;

        if (classCon != null && !classCon.isEmpty()) {
            aa = ch.getName().contains(classCon);
        }

        if (mnCon != null && !mnCon.isEmpty()) {
            ab = mr.getName().contains(mnCon);
        }

        ClassReference cr = MainForm.getEngine().getClassRef(ch);

        if (classAnno != null && !classAnno.isEmpty()) {
            if (cr.getAnnotations() == null ||
                    cr.getAnnotations().isEmpty()) {
                ag = false;
            } else {
                boolean fc = false;
                for (String a : cr.getAnnotations()) {
                    if (a.contains(classAnno)) {
                        fc = true;
                        break;
                    }
                }
                if (!fc) {
                    ag = false;
                }
            }
        }

        if (methodAnno != null && !methodAnno.isEmpty()) {
            if (mr.getAnnotations() == null || mr.getAnnotations().isEmpty()) {
                ah = false;
            } else {
                boolean fm = false;
                for (String a : mr.getAnnotations()) {
                    if (a.contains(methodAnno)) {
                        fm = true;
                        break;
                    }
                }
                if (!fm) {
                    ah = false;
                }
            }
        }

        if (start != null && !start.isEmpty()) {
            sw = mr.getName().startsWith(start);
        }

        if (endWith != null && !endWith.isEmpty()) {
            ew = mr.getName().endsWith(endWith);
        }

        if (hasField != null && !hasField.isEmpty()) {
            boolean ff = false;
            for (ClassReference.Member m : cr.getMembers()) {
                if (m.getName().contains(hasField)) {
                    ff = true;
                    break;
                }
            }
            if (!ff) {
                ai = false;
            }
        }

        if (i != null) {
            ac = i == paramNum;
        }

        if (retCon != null && !retCon.isEmpty()) {
            ad = ret.equals(retCon);
        }

        if (f != null) {
            ae = f == mr.isStatic();
        }

        if (isSubOf != null && !isSubOf.isEmpty()) {
            isSubOf = isSubOf.replace(".", "/");
            if (subs != null && !subs.isEmpty()) {
                boolean t = false;
                for (ClassReference.Handle h : subs) {
                    if (h.getName().equals(isSubOf)) {
                        t = true;
                        break;
                    }
                }
                if (!t) {
                    sb = false;
                }
            } else {
                sb = false;
            }
        }
        if (isSuperOf != null && !isSuperOf.isEmpty()) {
            isSuperOf = isSuperOf.replace(".", "/");
            if (supers != null && !supers.isEmpty()) {
                boolean t = false;
                for (ClassReference.Handle h : supers) {
                    if (h.getName().equals(isSuperOf)) {
                        t = true;
                        break;
                    }
                }
                if (!t) {
                    sp = false;
                }
            } else {
                sb = false;
            }
        }
        Type[] argTypes = Type.getArgumentTypes(mr.getDesc());
        for (int ix = 0; ix < argTypes.length; ix++) {
            String temp = paramMap.get(ix);
            if (temp == null) {
                continue;
            }
            if (!paramMap.get(ix).equals(argTypes[ix].getClassName())) {
                af = false;
                break;
            }
        }
        if (aa && ab && ac && ad && ae && af && ag && ah && ai && sb && sp && sw && ew) {
            searchList.add(new ResObj(mr.getHandle(), ch.getName()));
        }
    }
}
