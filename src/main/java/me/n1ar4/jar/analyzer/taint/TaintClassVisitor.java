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
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

public class TaintClassVisitor extends ClassVisitor {
    private static final Logger logger = LogManager.getLogger();

    private String className;
    private final int paramsNum;
    private final MethodReference.Handle cur;
    private final MethodReference.Handle next;
    private final AtomicInteger pass;
    private boolean iface;
    private final SanitizerRule rule;
    private final StringBuilder text;

    public TaintClassVisitor(int i,
                             MethodReference.Handle cur, MethodReference.Handle next,
                             AtomicInteger pass, SanitizerRule rule, StringBuilder text) {
        super(Const.ASMVersion);
        this.paramsNum = i;
        this.cur = cur;
        this.next = next;
        this.pass = pass;
        this.rule = rule;
        this.text = text;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.iface = (access & Opcodes.ACC_INTERFACE) != 0;

        // 现在的接口是直接按照实现记录 call 的
        // 所以直接 iface -> impl 参数完全对应 即可污点分析
        if (this.iface) {
            pass.set(paramsNum);
            logger.info("污点分析进行中 {} - {} - {}", cur.getClassReference().getName(), cur.getName(), cur.getDesc());
            text.append(String.format("污点分析进行中 %s - %s - %s", cur.getClassReference().getName(), cur.getName(), cur.getDesc()));
            text.append("\n");
            logger.info("发现接口类型污点 - 直接传递 - 第 {} 个参数", paramsNum);
            text.append(String.format("发现接口类型污点 - 直接传递 - 第 %d 个参数", paramsNum));
            text.append("\n");
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (this.iface) {
            return mv;
        }
        if (name.equals(this.cur.getName()) && desc.equals(this.cur.getDesc())) {
            TaintMethodAdapter tma = new TaintMethodAdapter(
                    api, mv, this.className, access, name, desc, this.paramsNum, next, pass, rule, text);
            return new JSRInlinerAdapter(tma, access, name, desc, signature, exceptions);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    public AtomicInteger getPass() {
        return this.pass;
    }
}
