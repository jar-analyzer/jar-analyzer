package me.n1ar4.jar.analyzer.analyze.cfg;

import java.util.ArrayList;
import java.util.List;

public class InsnBlock {

    public final List<String> lines = new ArrayList<>();


    public final List<InsnBlock> nextBlockList = new ArrayList<>();
    public final List<InsnBlock> jumpBlockList = new ArrayList<>();

    public void addLines(List<String> list) {
        lines.addAll(list);
    }

    public void addNext(InsnBlock item) {
        if (!nextBlockList.contains(item)) {
            nextBlockList.add(item);
        }
    }

    public void addJump(InsnBlock item) {
        if (!jumpBlockList.contains(item)) {
            jumpBlockList.add(item);
        }
    }

}
