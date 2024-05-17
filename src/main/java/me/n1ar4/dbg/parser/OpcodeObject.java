package me.n1ar4.dbg.parser;

public class OpcodeObject {
    private int opcode;
    private String opcodeStr;
    private int opcodeIndex;
    private String operands;

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public String getOpcodeStr() {
        return opcodeStr;
    }

    public void setOpcodeStr(String opcodeStr) {
        this.opcodeStr = opcodeStr;
    }

    public int getOpcodeIndex() {
        return opcodeIndex;
    }

    public void setOpcodeIndex(int opcodeIndex) {
        this.opcodeIndex = opcodeIndex;
    }

    public String getOperands() {
        return operands;
    }

    public void setOperands(String operands) {
        this.operands = operands;
    }

    @Override
    public String toString() {
        return "OpcodeObject{" +
                "opcode=" + opcode +
                ", opcodeStr='" + opcodeStr + '\'' +
                ", opcodeIndex=" + opcodeIndex +
                ", operands='" + operands + '\'' +
                '}';
    }
}
