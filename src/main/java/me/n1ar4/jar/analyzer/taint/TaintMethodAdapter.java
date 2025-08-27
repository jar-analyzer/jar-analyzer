/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.taint.jvm.JVMRuntimeAdapter;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class TaintMethodAdapter extends JVMRuntimeAdapter<String> {
    private static final Logger logger = LogManager.getLogger();

    private final String owner;
    private final int access;
    private final String name;
    private final String desc;
    private final int paramsNum;

    private final MethodReference.Handle next;
    private final AtomicInteger pass;
    private final SanitizerRule rule;

    public TaintMethodAdapter(final int api, final MethodVisitor mv, final String owner,
                              int access, String name, String desc, int paramsNum,
                              MethodReference.Handle next, AtomicInteger pass, SanitizerRule rule) {
        super(api, mv, owner, access, name, desc);
        this.owner = owner;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.paramsNum = paramsNum;
        this.next = next;
        this.pass = pass;
        this.rule = rule;
        logger.info("污点分析进行中 {} - {} - {}", this.owner, this.name, this.desc);
    }

    @Override
    public void visitCode() {
        // 改造成设置污点为第n个参数
        super.visitCode();
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            // 非 STATIC 第 0 是 THIS
            localVariables.set(paramsNum + 1, "TAINT");
        } else {
            localVariables.set(paramsNum, "TAINT");
        }
    }

    @Override
    @SuppressWarnings("all")
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        // 简单的污点分析
        // 我认为所有的方法都应该传播污点
        // 除非遇到 Sanitizer 白名单 否则认为没问题
        String nextClass = next.getClassReference().getName().replace(".", "/");
        // 找到下个方法
        if (owner.equals(nextClass) && name.equals(next.getName()) && desc.equals(next.getDesc())) {
            List<Set<String>> stack = this.operandStack.getList();

            // 计算方法参数数量
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            int argCount = argumentTypes.length;

            // 如果是非静态方法 还需要考虑 this 引用
            if (opcode != Opcodes.INVOKESTATIC) {
                argCount++; // 包含 this 引用
            }

            // 检查 stack 是否有足够的元素
            if (stack.size() >= argCount) {
                // 从栈顶开始检查参数（栈顶是最后一个参数）
                for (int i = 0; i < argCount; i++) {
                    int stackIndex = stack.size() - 1 - i; // 从栈顶往下
                    Set<String> item = stack.get(stackIndex);
                    if (item.contains("TAINT")) {
                        // 计算实际的参数位置
                        int paramIndex;
                        if (opcode == Opcodes.INVOKESTATIC) {
                            // 静态方法：参数从0开始
                            paramIndex = argCount - 1 - i;
                        } else {
                            // 非静态方法：0 是 this 参数从 1 开始
                            if (i == argCount - 1) {
                                paramIndex = 0; // this 引用
                            } else {
                                paramIndex = argCount - 1 - i;
                                // 处理 0
                                paramIndex--;
                            }
                        }
                        // 记录数据流
                        pass.set(paramIndex);
                        logger.info("发现方法调用类型污点 - 方法调用传播 - 接口第 {} 个参数", paramIndex);
                    }
                }
            }
        } else {
            List<Sanitizer> rules = this.rule.getRules();
            boolean match = false;
            for (Sanitizer rule : rules) {
                if (owner.equals(rule.getClassName()) &&
                        name.equals(rule.getMethodName()) &&
                        desc.equals(rule.getMethodDesc())) {
                    match = true;
                    break;
                }
            }
            if (match) {
                // 命中 sanitizer 不传递
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                pass.set(TaintAnalyzer.TAINT_FAIL);
                logger.info("污点命中 净化器 规则 - {} - {} - {}", owner, name, desc);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
