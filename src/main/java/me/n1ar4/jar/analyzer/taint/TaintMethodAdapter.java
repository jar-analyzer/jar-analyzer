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
import me.n1ar4.jar.analyzer.taint.jvm.JVMRuntimeAdapter;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 方法体内的污点解释器。
 * <p>
 * 关键约定（locals 索引语义，与 JVM 第一段 locals 一致）：
 * <ul>
 *   <li>非 static 方法：index 0 = this，1..N = arg0..arg(N-1)</li>
 *   <li>static 方法：index 0..N-1 = arg0..arg(N-1)</li>
 * </ul>
 * 该约定贯穿 source 的初始注入、跨方法 transfer.markLocal、调用前栈位置回算，
 * 避免历史代码中 paramIndex 与 this 索引交错带来的反复 +1/-1 修正。
 */
public class TaintMethodAdapter extends JVMRuntimeAdapter<String> {
    private static final Logger logger = LogManager.getLogger();

    private final String owner;
    private final int access;
    private final String name;
    private final String desc;

    /**
     * 调用方传入的入口污点（locals 索引集合）。
     * 在 visitCode 中据此初始化 localVariables。
     */
    private final TaintTransfer entry;

    private final MethodReference.Handle next;
    /**
     * 出口污点：本方法分析完后，传给链路下一对 (cur, next) 的载荷。
     */
    private final TaintTransfer exit;
    private final SanitizerRule rule;
    private final PropagationRuleSet propagation;
    private final TaintEventSink sink;
    /**
     * chain 内序号，用于事件归属
     */
    private final int chainIndex;

    public TaintMethodAdapter(final int api, final MethodVisitor mv, final String owner,
                              int access, String name, String desc,
                              TaintTransfer entry, MethodReference.Handle next,
                              TaintTransfer exit, SanitizerRule rule,
                              PropagationRuleSet propagation,
                              TaintEventSink sink, int chainIndex) {
        super(api, mv, owner, access, name, desc);
        this.owner = owner;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.entry = entry;
        this.next = next;
        this.exit = exit;
        this.rule = rule;
        this.propagation = propagation;
        this.sink = sink;
        this.chainIndex = chainIndex;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        // 按 entry 中的 locals 索引集合染色。
        // 注意：JVMRuntimeAdapter.visitCode 已经按 desc 把 this+args 的槽位
        //      初始化为 N 个空 Set，这里仅做染色。
        for (int idx = entry.getTaintedLocals().nextSetBit(0);
             idx >= 0;
             idx = entry.getTaintedLocals().nextSetBit(idx + 1)) {
            if (idx < localVariables.size()) {
                localVariables.set(idx, TaintAnalyzer.TAINT);
            }
        }
        logger.info("taint analysis in progress {} - {} - {} - entry: {}",
                this.owner, this.name, this.desc, entry);
        sink.emit(TaintEvent.atMethod(TaintEvent.Type.ENTER_METHOD, chainIndex,
                this.owner, this.name, this.desc,
                "进入方法分析，入口污点 " + entry));
    }

    /**
     * 见 {@link TaintIndexUtil#localIndexToStackOffsetFromTop(int, String, int)}。
     */
    private static int localIndexToStackOffsetFromTop(int invokeOpcode, String calleeDesc, int localIndex) {
        return TaintIndexUtil.localIndexToStackOffsetFromTop(invokeOpcode, calleeDesc, localIndex);
    }

    /**
     * 见 {@link TaintIndexUtil#stackOffsetFromTopToCalleeLocalIndex(int, String, int)}。
     */
    private static int stackOffsetFromTopToCalleeLocalIndex(int invokeOpcode, String calleeDesc, int stackOffsetFromTop) {
        return TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(invokeOpcode, calleeDesc, stackOffsetFromTop);
    }

    @Override
    @SuppressWarnings("all")
    public void visitMethodInsn(int opcode, String calleeOwner, String calleeName, String calleeDesc, boolean itf) {
        // 当前栈快照（仅用于读取参数污点；实际栈深度由父类 super.visitMethodInsn 维护）
        List<Set<String>> stack = this.operandStack.getList();

        Type[] argumentTypes = Type.getArgumentTypes(calleeDesc);
        int argCount = argumentTypes.length + (opcode == Opcodes.INVOKESTATIC ? 0 : 1);

        String nextClass = next.getClassReference().getName().replace(".", "/");
        boolean isNextCall = calleeOwner.equals(nextClass)
                && calleeName.equals(next.getName())
                && calleeDesc.equals(next.getDesc());

        // ============== Case 1: 命中链路下一跳 ==============
        if (isNextCall) {
            boolean anyMarked = false;
            if (stack.size() >= argCount) {
                for (int stackOffset = 0; stackOffset < argCount; stackOffset++) {
                    int stackIdx = stack.size() - 1 - stackOffset;
                    Set<String> slot = stack.get(stackIdx);
                    if (slot != null && slot.contains(TaintAnalyzer.TAINT)) {
                        int calleeLocalIdx = stackOffsetFromTopToCalleeLocalIndex(opcode, calleeDesc, stackOffset);
                        if (calleeLocalIdx >= 0) {
                            exit.markLocal(calleeLocalIdx);
                            anyMarked = true;
                            logger.info("taint propagated to next call - callee localIndex {}", calleeLocalIdx);
                            sink.emit(TaintEvent.atMethod(TaintEvent.Type.REACH_NEXT, chainIndex,
                                    calleeOwner, calleeName, calleeDesc,
                                    "污点传播至下一调用 - locals 槽 " + calleeLocalIdx + "（this=0）"));
                        }
                    }
                }
            }
            if (!anyMarked) {
                // 命中下一跳但参数未被污染（例如污点流向了 this 但 next 是 static 调用等）
                sink.emit(TaintEvent.atMethod(TaintEvent.Type.INFO, chainIndex,
                        calleeOwner, calleeName, calleeDesc,
                        "命中下一跳调用，但未发现污点参数"));
            }
            super.visitMethodInsn(opcode, calleeOwner, calleeName, calleeDesc, itf);
            return;
        }

        // ============== Case 2: 检查 sanitizer ==============
        if (this.rule != null && this.rule.getRules() != null) {
            for (Sanitizer s : this.rule.getRules()) {
                if (!calleeOwner.equals(s.getClassName())
                        || !calleeName.equals(s.getMethodName())
                        || !calleeDesc.equals(s.getMethodDesc())) {
                    continue;
                }

                if (s.getParamIndex() == Sanitizer.ALL_PARAMS) {
                    // ALL_PARAMS：对入参中只要存在污点都视为净化
                    if (anyArgTainted(stack, argCount)) {
                        super.visitMethodInsn(opcode, calleeOwner, calleeName, calleeDesc, itf);
                        // 不让返回值带污点
                        scrubReturnValue(calleeDesc);
                        logger.info("sanitizer matched (ALL_PARAMS) - {} - {} - {}",
                                calleeOwner, calleeName, calleeDesc);
                        sink.emit(TaintEvent.atMethod(TaintEvent.Type.SANITIZER_HIT, chainIndex,
                                calleeOwner, calleeName, calleeDesc,
                                "命中净化器规则（全部参数）"));
                        return;
                    }
                } else {
                    // 注意：sanitizer.json 中 paramIndex 历史语义按"locals 索引"约定
                    //   非 static: 0 = this, 1..N = arg
                    //   static:   0..N-1 = arg
                    int targetStackOffset = localIndexToStackOffsetFromTop(opcode, calleeDesc, s.getParamIndex());
                    if (targetStackOffset >= 0 && stack.size() > targetStackOffset) {
                        int targetStackIdx = stack.size() - 1 - targetStackOffset;
                        Set<String> targetSlot = stack.get(targetStackIdx);
                        if (targetSlot != null && targetSlot.contains(TaintAnalyzer.TAINT)) {
                            super.visitMethodInsn(opcode, calleeOwner, calleeName, calleeDesc, itf);
                            scrubReturnValue(calleeDesc);
                            logger.info("sanitizer matched - {} - {} - {} - localIndex: {}",
                                    calleeOwner, calleeName, calleeDesc, s.getParamIndex());
                            sink.emit(TaintEvent.atMethod(TaintEvent.Type.SANITIZER_HIT, chainIndex,
                                    calleeOwner, calleeName, calleeDesc,
                                    "命中净化器规则 - locals 索引 " + s.getParamIndex()));
                            return;
                        }
                    }
                }
            }
        }

        // ============== Case 3: propagation 精细规则 ==============
        // 在 sanitizer 之后、通用传播之前；命中后跳过 case 4。
        // 用于覆盖 StringBuilder/String/容器/Map/反射 等"对象内传播"场景，
        // 弥补当前实现没有真正字段建模导致的链路漏报。
        if (propagation != null) {
            for (PropagationRule pr : propagation.getRules()) {
                if (!matchesPropagation(pr, calleeOwner, calleeName, calleeDesc)) {
                    continue;
                }
                if (!propagationFromMatched(pr, opcode, calleeDesc, stack, argCount)) {
                    continue;
                }
                // 该规则触发：先正常调用栈维护
                super.visitMethodInsn(opcode, calleeOwner, calleeName, calleeDesc, itf);
                // 应用 to 操作（写回 this 槽 / 染色返回值）
                applyPropagationTo(pr, opcode, calleeDesc);
                sink.emit(TaintEvent.atMethod(TaintEvent.Type.PROPAGATION_RULE_HIT, chainIndex,
                        calleeOwner, calleeName, calleeDesc,
                        "命中传播规则 from=" + pr.getFrom() + " to=" + pr.getTo()));
                return;
            }
        }

        // ============== Case 4: 通用污点传播 ==============
        // 任意入参带污点 + 有返回值 -> 返回值染色
        boolean propagate = anyArgTainted(stack, argCount);
        super.visitMethodInsn(opcode, calleeOwner, calleeName, calleeDesc, itf);
        if (propagate) {
            taintReturnValue(calleeDesc);
            // 仅当有返回值时才记录为"通用传播"，避免日志噪音
            Type rt = Type.getReturnType(calleeDesc);
            if (rt.getSort() != Type.VOID) {
                sink.emit(TaintEvent.atMethod(TaintEvent.Type.GENERIC_PROPAGATE, chainIndex,
                        calleeOwner, calleeName, calleeDesc,
                        "通用传播：参数带污点 → 返回值染色"));
            }
        }
    }

    // -------- propagation 规则辅助方法 --------

    private boolean matchesPropagation(PropagationRule pr, String calleeOwner,
                                       String calleeName, String calleeDesc) {
        if (pr == null) return false;
        // className 暂不支持通配（防止误伤），必须精确匹配
        if (pr.getClassName() == null || !pr.getClassName().equals(calleeOwner)) return false;
        // methodName 支持 "*" 通配匹配该 owner 下所有方法（用于 Apache StringUtils 等纯工具类）
        String mn = pr.getMethodName();
        if (mn == null) return false;
        if (!"*".equals(mn) && !mn.equals(calleeName)) return false;
        // methodDesc 支持 "*" 通配匹配所有重载
        String d = pr.getMethodDesc();
        if (d == null) return false;
        return PropagationRule.DESC_ANY.equals(d) || d.equals(calleeDesc);
    }

    /**
     * 检查"调用前"栈是否满足 from 条件。
     */
    private boolean propagationFromMatched(PropagationRule pr, int opcode, String calleeDesc,
                                           List<Set<String>> stack, int argCount) {
        String from = pr.getFrom();
        if (from == null || from.trim().isEmpty()) {
            from = PropagationRule.FROM_ANY;
        }
        for (String token : from.split(",")) {
            token = token.trim();
            if (token.isEmpty()) continue;
            if (PropagationRule.FROM_ANY.equals(token)) {
                if (anyArgTainted(stack, argCount)) return true;
            } else if (PropagationRule.FROM_THIS.equals(token)) {
                if (opcode == Opcodes.INVOKESTATIC) continue;
                // this 在栈底，距离顶 argCount-1
                int idx = stack.size() - 1 - (argCount - 1);
                if (idx >= 0 && idx < stack.size()) {
                    Set<String> slot = stack.get(idx);
                    if (slot != null && slot.contains(TaintAnalyzer.TAINT)) return true;
                }
            } else {
                // 数字：locals 索引
                try {
                    int localIdx = Integer.parseInt(token);
                    int off = TaintIndexUtil.localIndexToStackOffsetFromTop(opcode, calleeDesc, localIdx);
                    if (off < 0) continue;
                    int idx = stack.size() - 1 - off;
                    if (idx >= 0 && idx < stack.size()) {
                        Set<String> slot = stack.get(idx);
                        if (slot != null && slot.contains(TaintAnalyzer.TAINT)) return true;
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return false;
    }

    /**
     * 在父类 visitMethodInsn 之后应用 to 操作。
     * 注意：栈状态此时已被父类更新（参数已 pop、返回值已 push）。
     */
    private void applyPropagationTo(PropagationRule pr, int opcode, String calleeDesc) {
        String to = pr.getTo();
        if (to == null || to.trim().isEmpty()) {
            return;
        }
        boolean wantThis = false;
        boolean wantRet = false;
        List<Integer> writeBackIndices = new java.util.ArrayList<>();
        for (String t : to.split(",")) {
            t = t.trim();
            if (t.isEmpty()) continue;
            if (PropagationRule.TO_RET.equals(t)) {
                wantRet = true;
            } else if (PropagationRule.TO_THIS.equals(t)) {
                wantThis = true;
            } else {
                try {
                    writeBackIndices.add(Integer.parseInt(t));
                } catch (NumberFormatException ignore) {
                }
            }
        }

        // 先处理 ret：父类已把返回值 push 到栈顶
        Type rt = Type.getReturnType(calleeDesc);
        if (wantRet && rt.getSort() != Type.VOID) {
            taintReturnValue(calleeDesc);
        }

        // 处理 this：受 invoke 类型限制；非 static 才有 this
        // 注：此时 this 已被 pop。我们采用副作用语义——本步无法把污点写回到调用者本地变量，
        // 但通过让"返回值带污点"近似补偿（很多链式 API 都返回 this）。
        // 这里保留一个空操作，避免在父类 visit 之后再去翻栈造成不一致；
        // 调用方会通过下一次方法访问（例如 .toString()) 触发对应 this->ret 规则继续传播。
        if (wantThis) {
            // 保守：若有返回值，与 wantRet 等效（链式 API：StringBuilder.append() 返回 this）
            if (rt.getSort() != Type.VOID && !wantRet) {
                taintReturnValue(calleeDesc);
            }
        }

        // 写回到入参槽：父类已 pop，无法再访问；忽略（等同于通用传播）
        // 该能力暂未启用（极少用），如未来需要，可在父类调用前 snapshot 并恢复。
        // 故 writeBackIndices 仅用于占位，不做实际处理。
        if (!writeBackIndices.isEmpty()) {
            // 不实现：见上注释
        }
    }

    /**
     * 处理 invokedynamic：bootstrap method 的 captured args（栈上）若有污点，
     * 则返回值（通常是 functional interface 实例，例如 lambda）也带污点。
     * <p>
     * 父类已正确维护栈深度（pop captured args, push return），这里只在父类调用后
     * 把"栈顶返回值 slot"按需染色。
     */
    @Override
    @SuppressWarnings("all")
    public void visitInvokeDynamicInsn(String idyName, String idyDesc, Handle bsm, Object... bsmArgs) {
        // 计算 captured args 数量（按 slot 计数）以便从栈上窥探
        Type[] capturedTypes = Type.getArgumentTypes(idyDesc);
        int slotCount = 0;
        for (Type t : capturedTypes) {
            slotCount += t.getSize();
        }

        List<Set<String>> stack = this.operandStack.getList();
        boolean tainted = false;
        if (stack.size() >= slotCount) {
            for (int i = 0; i < slotCount; i++) {
                int idx = stack.size() - 1 - i;
                Set<String> slot = stack.get(idx);
                if (slot != null && slot.contains(TaintAnalyzer.TAINT)) {
                    tainted = true;
                    break;
                }
            }
        }

        super.visitInvokeDynamicInsn(idyName, idyDesc, bsm, bsmArgs);

        if (tainted) {
            taintReturnValue(idyDesc);
            logger.debug("taint propagated through invokedynamic ({}{})", idyName, idyDesc);
            sink.emit(TaintEvent.atMethod(TaintEvent.Type.INVOKEDYNAMIC_PROPAGATE, chainIndex,
                    bsm == null ? null : bsm.getOwner(), idyName, idyDesc,
                    "invokedynamic 捕获参数带污点 → 返回值染色（lambda/字符串拼接等）"));
        }
    }

    /**
     * 检查"调用前"栈顶 N 个参数槽是否有任意一个带污点。
     */
    private boolean anyArgTainted(List<Set<String>> stack, int argCount) {
        if (stack.size() < argCount) {
            return false;
        }
        for (int i = 0; i < argCount; i++) {
            Set<String> slot = stack.get(stack.size() - 1 - i);
            if (slot != null && slot.contains(TaintAnalyzer.TAINT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把刚刚调用过的方法的返回值（位于栈顶）染色。
     * 必须在 super.visitMethodInsn / super.visitInvokeDynamicInsn 之后调用。
     */
    private void taintReturnValue(String calleeDesc) {
        Type returnType = Type.getReturnType(calleeDesc);
        int retSize = returnType.getSize();
        if (returnType.getSort() == Type.VOID || retSize <= 0) {
            return;
        }
        List<Set<String>> stack = this.operandStack.getList();
        if (stack.size() < retSize) {
            return;
        }
        for (int j = 0; j < retSize; j++) {
            int topIdx = stack.size() - retSize + j;
            Set<String> taintSet = new HashSet<>();
            taintSet.add(TaintAnalyzer.TAINT);
            stack.set(topIdx, taintSet);
        }
    }

    /**
     * 把刚刚调用过的方法的返回值（位于栈顶）清洗为干净值。
     */
    private void scrubReturnValue(String calleeDesc) {
        Type returnType = Type.getReturnType(calleeDesc);
        int retSize = returnType.getSize();
        if (returnType.getSort() == Type.VOID || retSize <= 0) {
            return;
        }
        List<Set<String>> stack = this.operandStack.getList();
        if (stack.size() < retSize) {
            return;
        }
        for (int j = 0; j < retSize; j++) {
            int topIdx = stack.size() - retSize + j;
            stack.set(topIdx, new HashSet<>());
        }
    }
}
