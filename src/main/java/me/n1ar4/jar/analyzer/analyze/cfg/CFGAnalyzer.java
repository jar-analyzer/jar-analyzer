/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.List;

public class CFGAnalyzer<V extends Value> extends Analyzer<V> {
    private AbstractInsnNode[] nodeArray;
    public InsnBlock[] blocks;

    public CFGAnalyzer(Interpreter<V> interpreter) {
        super(interpreter);
    }

    @Override
    public Frame<V>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        nodeArray = method.instructions.toArray();
        int length = nodeArray.length;
        blocks = new InsnBlock[length];
        InsnText insnText = new InsnText();
        for (int i = 0; i < length; i++) {
            blocks[i] = getBlock(i);
            AbstractInsnNode node = nodeArray[i];
            List<String> lines = insnText.toLines(node);
            blocks[i].addLines(lines);
        }

        return super.analyze(owner, method);
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {

        AbstractInsnNode insnNode = nodeArray[insnIndex];
        int insnOpcode = insnNode.getOpcode();
        int insnType = insnNode.getType();

        if (insnType == AbstractInsnNode.JUMP_INSN) {
            if ((insnIndex + 1) == successorIndex) {
                addNext(insnIndex, successorIndex);
            } else {
                addJump(insnIndex, successorIndex);
            }
        } else if (insnOpcode == LOOKUPSWITCH) {
            addJump(insnIndex, successorIndex);
        } else if (insnOpcode == TABLESWITCH) {
            addJump(insnIndex, successorIndex);
        } else if (insnOpcode == RET) {
            addJump(insnIndex, successorIndex);
        } else if (insnOpcode == ATHROW || (insnOpcode >= IRETURN && insnOpcode <= RETURN)) {
            assert false : "should not be here";
            removeNextAndJump(insnIndex);
        } else {
            addNext(insnIndex, successorIndex);
        }


        super.newControlFlowEdge(insnIndex, successorIndex);
    }

    private void addNext(int fromIndex, int toIndex) {
        InsnBlock currentBlock = getBlock(fromIndex);
        InsnBlock nextBlock = getBlock(toIndex);
        currentBlock.addNext(nextBlock);
    }

    private void addJump(int fromIndex, int toIndex) {
        InsnBlock currentBlock = getBlock(fromIndex);
        InsnBlock nextBlock = getBlock(toIndex);
        currentBlock.addJump(nextBlock);
    }

    private void removeNextAndJump(int insnIndex) {
        InsnBlock currentBlock = getBlock(insnIndex);
        currentBlock.nextBlockList.clear();
        currentBlock.jumpBlockList.clear();
    }

    private InsnBlock getBlock(int insnIndex) {
        InsnBlock block = blocks[insnIndex];
        if (block == null) {
            block = new InsnBlock();
            blocks[insnIndex] = block;
        }
        return block;
    }

    public InsnBlock[] getBlocks() {
        return blocks;
    }

}
