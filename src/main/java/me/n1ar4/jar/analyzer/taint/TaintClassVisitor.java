/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class TaintClassVisitor extends ClassVisitor {
    private static final Logger logger = LogManager.getLogger();

    private String className;
    private final TaintTransfer entry;
    private final MethodReference.Handle cur;
    private final MethodReference.Handle next;
    private final TaintTransfer exit;
    private boolean iface;
    /**
     * 接口目标方法是否存在方法体（default/static 方法），
     * 有方法体时已按普通方法分析，visitEnd 不再做透传。
     */
    private boolean ifaceTargetHasCode = false;
    private final SanitizerRule rule;
    private final PropagationRuleSet propagation;
    private final TaintEventSink sink;
    private final int chainIndex;

    public TaintClassVisitor(TaintTransfer entry,
                             MethodReference.Handle cur, MethodReference.Handle next,
                             TaintTransfer exit, SanitizerRule rule,
                             PropagationRuleSet propagation,
                             TaintEventSink sink, int chainIndex) {
        super(Const.ASMVersion);
        this.entry = entry;
        this.cur = cur;
        this.next = next;
        this.exit = exit;
        this.rule = rule;
        this.propagation = propagation;
        this.sink = sink;
        this.chainIndex = chainIndex;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.iface = (access & Opcodes.ACC_INTERFACE) != 0;

        // 2026/09/06 修复：接口不再一律透传——Java 8+ 的 default/static 接口方法
        // 有方法体，应当走正常分析；仅当目标方法确实没有 Code 时才在 visitEnd 透传
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(this.cur.getName()) && desc.equals(this.cur.getDesc())) {
            TaintMethodAdapter tma = new TaintMethodAdapter(
                    api, mv, this.className, access, name, desc,
                    entry, next, exit, rule, propagation, sink, chainIndex);
            MethodVisitor inlined =
                    new JSRInlinerAdapter(tma, access, name, desc, signature, exceptions);
            if (this.iface) {
                // 探测接口方法是否有方法体（default/static 有，abstract 没有）：
                // 有 Code 则正常分析，无 Code 由 visitEnd 兜底透传
                return new MethodVisitor(api, inlined) {
                    @Override
                    public void visitCode() {
                        ifaceTargetHasCode = true;
                        super.visitCode();
                    }
                };
            }
            return inlined;
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (this.iface && !ifaceTargetHasCode) {
            ifacePassthrough();
        }
    }

    /**
     * 接口无方法体时的透传：
     * cur 与 next 同名同 desc（override），locals 布局一致，直接按槽位投影。
     * 跳过 cur 的 this（index 0）——接口 → 实现的 receiver 是新对象，污点不可靠。
     * 边界检查按 slot 数（long/double 形参占 2 槽）。
     */
    private void ifacePassthrough() {
        int nextSlotCount = 1; // this
        for (Type t : Type.getArgumentTypes(next.getDesc())) {
            nextSlotCount += t.getSize();
        }
        for (int i = entry.getTaintedLocals().nextSetBit(0);
             i >= 0;
             i = entry.getTaintedLocals().nextSetBit(i + 1)) {
            if (i == 0 || i >= nextSlotCount) {
                continue;
            }
            exit.markLocal(i);
        }
        // 兜底：如果一个都没传到，至少透传"按 entry 第一个参数槽位"，
        // 维持与旧行为一致，避免接口断链。
        if (!exit.hasTaint() && entry.hasTaint()) {
            int first = entry.getTaintedLocals().nextSetBit(0);
            if (first >= 0) {
                int target = Math.max(1, first);
                if (target < nextSlotCount) {
                    exit.markLocal(target);
                }
            }
        }
        logger.info("taint analysis (interface) {} - {} - {} -> {}",
                cur.getClassReference().getName(), cur.getName(), cur.getDesc(), exit);
        sink.emit(TaintEvent.atMethod(TaintEvent.Type.INTERFACE_PASSTHROUGH, chainIndex,
                cur.getClassReference().getName(), cur.getName(), cur.getDesc(),
                "接口透传，出口污点 " + exit));
    }

    public TaintTransfer getExit() {
        return this.exit;
    }
}
