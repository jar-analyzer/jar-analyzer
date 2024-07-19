package me.n1ar4.jar.analyzer.analyze.asm;

import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IdentifyCallEngine {
    private static final Logger logger = LogManager.getLogger();

    public static String run(String absPath, String methodName, String methodDesc) throws Exception {
        Class<?> me = MethodHandles.lookup().lookupClass();
        ClassReader r = new ClassReader(Files.readAllBytes(Paths.get(absPath)));
        ClassNode cn = new ClassNode();
        r.accept(cn, Const.GlobalASMOptions);
        MethodNode toAnalyze = null;
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(methodName) && mn.desc.equals(methodDesc)) {
                toAnalyze = mn;
                break;
            }
        }
        if (toAnalyze == null) {
            logger.error("analyze null");
            LogUtil.info("analyze null");
            return null;
        }

        List<int[]> invocations = new ArrayList<>();
        final InsnList instructions = toAnalyze.instructions;
        IdentifyCall sources = IdentifyCall.getInputs(me.getName().replace('.', '/'), toAnalyze);

        for (int ix = 0, num = instructions.size(); ix < num; ix++) {
            AbstractInsnNode instr = instructions.get(ix);
            if (instr.getType() != AbstractInsnNode.METHOD_INSN) continue;
            IntSummaryStatistics s = sources.getAllInputsOf(instr).stream()
                    .mapToInt(instructions::indexOf).summaryStatistics();
            s.accept(ix);
            invocations.add(new int[]{s.getMin(), s.getMax()});
        }
        return printIt(invocations, instructions);
    }

    private static String printIt(List<int[]> invocations, final InsnList instructions) {
        StringBuilder sb = new StringBuilder();
        List<Level> levels = toTree(invocations);
        Textifier toText = new Textifier();
        TraceMethodVisitor tmv = new TraceMethodVisitor(toText);
        for (int ix = 0, num = instructions.size(); ix < num; ix++) {
            AbstractInsnNode instr = instructions.get(ix);
            boolean line = false;
            level:
            for (Level l : levels) {
                if (ix >= l.lo && ix <= l.hi) {
                    for (int[] b : l.branches) {
                        if (ix < b[0] || ix > b[1]) continue;
                        sb.append(line ?
                                (b[0] == ix ? b[1] == ix ? "─[" : "┬─" : b[1] == ix ? "┴─" : "┼─") :
                                (b[0] == ix ? b[1] == ix ? " [" : "┌─" : b[1] == ix ? "└─" : "│ "));
                        line |= b[0] == ix || b[1] == ix;
                        continue level;
                    }
                }
                sb.append(line ? "──" : "  ");
            }
            instr.accept(tmv);
            sb.append(toText.text.get(0));
            toText.text.clear();
        }
        return sb.toString();
    }

    static List<Level> toTree(List<int[]> list) {
        if (list.isEmpty()) return Collections.emptyList();
        if (list.size() == 1) return Collections.singletonList(new Level(list.get(0)));
        list.sort(Comparator.comparingInt(b -> b[1] - b[0]));
        ArrayList<Level> l = new ArrayList<>();
        insert:
        for (int[] b : list) {
            for (Level level : l) if (level.insert(b)) continue insert;
            l.add(new Level(b));
        }
        if (l.size() > 1) Collections.reverse(l);
        return l;
    }
}
