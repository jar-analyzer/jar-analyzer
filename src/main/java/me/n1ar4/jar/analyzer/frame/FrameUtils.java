package me.n1ar4.jar.analyzer.frame;

import me.n1ar4.jar.analyzer.cfg.InsnText;
import me.n1ar4.jar.analyzer.draw.canvas.Canvas;
import me.n1ar4.jar.analyzer.draw.canvas.TextAlign;
import me.n1ar4.jar.analyzer.draw.canvas.theme.shape.Rectangle;
import me.n1ar4.jar.analyzer.draw.canvas.theme.table.FixedWidthOneLineTable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import java.util.List;
import java.util.function.Function;

public class FrameUtils {
    public static <V extends Value, T> void printGraph(String owner,
                                                       MethodNode mn,
                                                       Analyzer<V> analyzer,
                                                       Function<V, T> func,
                                                       StringBuilder builder) throws AnalyzerException {
        builder.append(mn.name).append(":").append(mn.desc).append("\n");
        int maxLocals = mn.maxLocals;
        int maxStack = mn.maxStack;
        InsnList instructions = mn.instructions;
        int size = instructions.size();
        InsnText insnText = new InsnText();
        Frame<V>[] frames = analyzer.analyze(owner, mn);
        String format = "%03d:    %-36s";
        for (int index = 0; index < size; index++) {
            AbstractInsnNode node = instructions.get(index);
            List<String> nodeLines = insnText.toLines(node);
            Frame<V> f = frames[index];
            printOneFrame(maxLocals, maxStack, f, func, builder);
            String firstLine = String.format(format, index, nodeLines.get(0));
            builder.append(firstLine).append("\n");
            for (int i = 1; i < nodeLines.size(); i++) {
                String item = nodeLines.get(i);
                String line = String.format("%4s    %-36s", "", item);
                builder.append(line);
            }
        }
        builder.append("\n");
    }

    public static <V extends Value, T> void printOneFrame(int maxLocals,
                                                          int maxStack,
                                                          Frame<V> f,
                                                          Function<V, T> func,
                                                          StringBuilder builder) {
        Canvas canvas = new Canvas();
        int padding = 5;
        String local_variable_label = "local variable";
        String operand_stack_label = "operand stack";
        int local_variable_col = 40;
        int frame_width1 = local_variable_col + maxLocals * 7;
        int frame_width2 = local_variable_col + local_variable_label.length() + padding;
        int frame_width = Math.max(frame_width1, frame_width2);
        int frame_height = maxStack * 2 + 4;
        Rectangle rectangle = new Rectangle(frame_width, frame_height);
        canvas.draw(0, 0, rectangle);
        canvas.moveTo(2, padding);
        canvas.drawText(operand_stack_label);
        int stackSize = f.getStackSize();
        String[][] matrix = new String[maxStack][1];
        int index = matrix.length - 1;
        for (int i = 0; i < stackSize; i++) {
            V stackValue = f.getStack(i);
            String str = func.apply(stackValue).toString();
            matrix[index][0] = str;
            int valueSize = stackValue.getSize();
            if (valueSize == 2) {
                matrix[index - 1][0] = "top";
            }
            index -= valueSize;
        }
        FixedWidthOneLineTable stack_table = new FixedWidthOneLineTable(
                matrix, TextAlign.CENTER, 25);
        canvas.draw(3, 5, stack_table);
        canvas.moveTo(maxStack * 2, local_variable_col);
        canvas.drawText(local_variable_label);
        String[][] local_variable_matrix = new String[1][maxLocals];
        for (int i = 0; i < maxLocals; i++) {
            local_variable_matrix[0][i] = "" + i;
        }
        FixedWidthOneLineTable local_table = new FixedWidthOneLineTable(
                local_variable_matrix, TextAlign.CENTER, 3);
        canvas.draw(maxStack * 2 + 1, local_variable_col, local_table);
        canvas.moveTo(1, frame_width + padding);
        canvas.drawText("locals: " + f.getLocals());
        canvas.moveTo(2, frame_width + padding);
        canvas.drawText("stacks: " + stackSize);
        for (int i = 0; i < maxLocals; i++) {
            V localValue = f.getLocal(i);
            String str = func.apply(localValue).toString();
            String line = String.format("%d: %s", i, str);
            canvas.moveTo(4 + i, frame_width + padding);
            canvas.drawText(line);
        }
        builder.append(canvas);
    }
}
