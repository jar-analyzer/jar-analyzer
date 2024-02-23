package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.core.Configuration;
import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.ClassVisitor;

import java.util.ArrayList;
import java.util.List;

public class BaseClassVisitor extends ClassVisitor {
    protected final String targetClassName;
    protected final int api;
    protected List<HookInfo> hooks;

    public BaseClassVisitor(int api, ClassVisitor classVisitor,
                            String targetClassName) {
        super(api, classVisitor);
        this.api = api;
        this.targetClassName = targetClassName;
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if (!name.equals(targetClassName)) {
            return;
        }
        List<HookInfo> h = Configuration.getHookInfoByClassName(name);
        if (h == null || h.isEmpty()) {
            this.hooks = new ArrayList<>();
        } else {
            this.hooks = h;
        }
    }
}
