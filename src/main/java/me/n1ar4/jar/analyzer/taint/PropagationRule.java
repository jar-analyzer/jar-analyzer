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

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 单条污点传播规则。
 * <p>
 * 用于补足"污点流入对象/容器后再流出"等场景，例如：
 * <ul>
 *   <li>{@code StringBuilder.append(arg) -> this}：append 后 this 也带污点（且返回值=this）</li>
 *   <li>{@code Map.put(k, v) -> this}：put 进去的 value 让 map 也带污点</li>
 *   <li>{@code Method.invoke(obj, args) -> ret}：args 中任意污点都能流向返回值</li>
 * </ul>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code className} / {@code methodName} / {@code methodDesc}：与
 *       {@link Sanitizer} 一致。{@code methodDesc == "*"} 表示对该 owner.method
 *       的所有重载都生效（描述符通配）。</li>
 *   <li>{@code from}：触发条件，源参数（locals 索引），多值用 "," 分隔。
 *       特殊值：
 *       <ul>
 *         <li>{@code "any"}：任意入参带污点都触发（默认）</li>
 *         <li>{@code "this"}：仅当 receiver（this）带污点</li>
 *         <li>整数：locals 索引（语义同 {@link Sanitizer}：非 static 时 0=this，1..N=arg）</li>
 *       </ul>
 *   </li>
 *   <li>{@code to}：传播目标，多值用 "," 分隔。可选值：
 *       <ul>
 *         <li>{@code "ret"}：返回值染色</li>
 *         <li>{@code "this"}：receiver 也染色（写回到调用前栈底位置；仅非 static 有效）</li>
 *         <li>整数：将污点写回到对应入参槽（罕见，例如 {@code Collections.addAll(coll, args)} 把 args 染色到 coll）</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public class PropagationRule {

    /**
     * 源条件特殊值：任意入参带污点都触发
     */
    public static final String FROM_ANY = "any";
    /**
     * 源条件特殊值：仅 this 带污点
     */
    public static final String FROM_THIS = "this";

    /**
     * 目标特殊值：返回值染色
     */
    public static final String TO_RET = "ret";
    /**
     * 目标特殊值：this 染色
     */
    public static final String TO_THIS = "this";

    /**
     * 描述符通配，"*" 表示匹配该 owner.method 的所有重载
     */
    public static final String DESC_ANY = "*";

    @JSONField
    private String className;
    @JSONField
    private String methodName;
    @JSONField
    private String methodDesc;
    @JSONField
    private String from;
    @JSONField
    private String to;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "PropagationRule{" + className + '.' + methodName + methodDesc
                + ", from=" + from + ", to=" + to + '}';
    }
}
