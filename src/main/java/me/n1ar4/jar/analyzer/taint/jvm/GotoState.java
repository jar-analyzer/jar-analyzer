/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint.jvm;


public class GotoState<T> {
    private LocalVariables<T> localVariables;
    private OperandStack<T> operandStack;

    public LocalVariables<T> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(LocalVariables<T> localVariables) {
        this.localVariables = localVariables;
    }

    public OperandStack<T> getOperandStack() {
        return operandStack;
    }

    public void setOperandStack(OperandStack<T> operandStack) {
        this.operandStack = operandStack;
    }
}
