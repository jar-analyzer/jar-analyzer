package me.n1ar4.jar.analyzer.cfg;

import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoreCFGAnalyzer<V extends Value> extends CFGAnalyzer<V> {

    public CoreCFGAnalyzer(Interpreter<V> interpreter) {
        super(interpreter);
    }

    @Override
    public InsnBlock[] getBlocks() {

        InsnBlock[] blocks = super.getBlocks();


        if (blocks == null || blocks.length < 1) {
            return blocks;
        }


        Set<InsnBlock> newBlockSet = new HashSet<>();
        int length = blocks.length;
        for (int i = 0; i < length; i++) {
            InsnBlock currentBlock = blocks[i];
            List<InsnBlock> nextBlockList = currentBlock.nextBlockList;
            List<InsnBlock> jumpBlockList = currentBlock.jumpBlockList;

            boolean hasNext = false;
            boolean hasJump = false;

            if (!nextBlockList.isEmpty()) {
                hasNext = true;
            }

            if (!jumpBlockList.isEmpty()) {
                hasJump = true;
            }

            if (!hasNext && (i + 1) < length) {
                newBlockSet.add(blocks[i + 1]);
            }

            if (hasJump) {
                newBlockSet.addAll(jumpBlockList);

                if (hasNext) {
                    newBlockSet.add(blocks[i + 1]);
                }
            }
        }


        List<InsnBlock> resultList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            InsnBlock currentBlock = blocks[i];

            if (i == 0) {
                resultList.add(currentBlock);
            }
            else if (newBlockSet.contains(currentBlock)) {
                resultList.add(currentBlock);
            }
            else {
                int size = resultList.size();
                InsnBlock lastBlock = resultList.get(size - 1);
                lastBlock.lines.addAll(currentBlock.lines);
                lastBlock.jumpBlockList.clear();
                lastBlock.jumpBlockList.addAll(currentBlock.jumpBlockList);
                lastBlock.nextBlockList.clear();
                lastBlock.nextBlockList.addAll(currentBlock.nextBlockList);
            }
        }

        return resultList.toArray(new InsnBlock[0]);
    }
}
