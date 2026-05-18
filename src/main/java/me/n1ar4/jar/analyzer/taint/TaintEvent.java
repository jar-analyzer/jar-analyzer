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

/**
 * 污点分析过程中的一个结构化事件。
 * <p>
 * 旧实现把分析过程拼成 {@code StringBuilder text}（最终保存到 {@link TaintResult#getTaintText()}），
 * UI 只能整段显示。引入本结构后：
 * <ul>
 *   <li>UI 可以按事件类型着色 / 过滤 / 展示图标</li>
 *   <li>导出 JSON 时保留语义</li>
 *   <li>统计每条链的"接口透传 / 直传 / sanitizer 命中 / 通用传播"次数</li>
 * </ul>
 * 该类设计为不可变数据载体，沿调用链单向追加。
 */
public final class TaintEvent {

    public enum Type {
        /**
         * 一条 chain 的分析开始
         */
        CHAIN_START,
        /**
         * 链首某个参数作为 source 开始尝试
         */
        SOURCE_TRY,
        /**
         * 进入某个方法的字节码分析
         */
        ENTER_METHOD,
        /**
         * 命中链路下一跳：污点参数被传给 next
         */
        REACH_NEXT,
        /**
         * 接口默认透传：投影 entry 到 next 的 locals
         */
        INTERFACE_PASSTHROUGH,
        /**
         * 命中 sanitizer，污点被清洗
         */
        SANITIZER_HIT,
        /**
         * 通用传播：任意入参带污点 → 返回值染色
         */
        GENERIC_PROPAGATE,
        /**
         * 命中 propagation.json 中的精细规则（含容器/反射/字符串拼接等）
         */
        PROPAGATION_RULE_HIT,
        /**
         * invokedynamic 传播：lambda / 字符串 indify
         */
        INVOKEDYNAMIC_PROPAGATE,
        /**
         * chain 结束（成功）
         */
        CHAIN_PASS,
        /**
         * chain 结束（失败 / 中断）
         */
        CHAIN_FAIL,
        /**
         * 信息或警告（未分类）
         */
        INFO,
        WARN
    }

    private final Type type;
    /**
     * 事件发生时本 chain 内的方法序号（从 0 开始；可为 -1 表示与某一步无关）
     */
    private final int chainIndex;
    /**
     * 当前正在分析的方法 owner（如 com/foo/Bar），可为 null
     */
    private final String owner;
    /**
     * 当前正在分析的方法名
     */
    private final String methodName;
    /**
     * 当前方法描述符
     */
    private final String methodDesc;
    /**
     * 主消息（中文友好）
     */
    private final String message;

    private TaintEvent(Type type, int chainIndex, String owner, String methodName, String methodDesc, String message) {
        this.type = type;
        this.chainIndex = chainIndex;
        this.owner = owner;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.message = message;
    }

    public static TaintEvent of(Type type, String message) {
        return new TaintEvent(type, -1, null, null, null, message);
    }

    public static TaintEvent atMethod(Type type, int chainIndex, String owner,
                                      String methodName, String methodDesc, String message) {
        return new TaintEvent(type, chainIndex, owner, methodName, methodDesc, message);
    }

    public Type getType() {
        return type;
    }

    public int getChainIndex() {
        return chainIndex;
    }

    public String getOwner() {
        return owner;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 友好的单行文本（用于回写到 taintText 兼容旧 UI）
     */
    public String toPlainLine() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(type.name()).append(']');
        if (chainIndex >= 0) {
            sb.append(" #").append(chainIndex);
        }
        if (owner != null) {
            sb.append(' ').append(owner);
            if (methodName != null) {
                sb.append('.').append(methodName);
                if (methodDesc != null) {
                    sb.append(methodDesc);
                }
            }
        }
        if (message != null && !message.isEmpty()) {
            sb.append(" - ").append(message);
        }
        return sb.toString();
    }
}
