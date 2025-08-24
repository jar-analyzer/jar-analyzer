package me.n1ar4.jar.analyzer.taint;


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
