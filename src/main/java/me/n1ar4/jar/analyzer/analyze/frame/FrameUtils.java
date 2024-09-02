/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.analyze.frame;

import me.n1ar4.jar.analyzer.analyze.cfg.InsnText;
import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.TextAlign;
import me.n1ar4.jar.analyzer.utils.theme.shape.Rectangle;
import me.n1ar4.jar.analyzer.utils.theme.table.FixedWidthOneLineTable;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FrameUtils {
    private static final Logger logger = LogManager.getLogger();
    private static final String START = "{";
    private static final String STOP = "}";
    private static final String EMPTY = "{}";
    private static final String SEPARATOR = "|";

    public static <V extends Value, T> void printFrames(String owner,
                                                        MethodNode mn,
                                                        Analyzer<V> analyzer,
                                                        Function<V, T> func,
                                                        StringBuilder builder) throws AnalyzerException {
        builder.append(mn.name)
                .append(":")
                .append(mn.desc)
                .append("\t")
                .append("local variable array")
                .append("\t")
                .append("operand stack (bottom - top)")
                .append("\n");
        InsnList instructions = mn.instructions;
        int size = instructions.size();
        InsnText insnText = new InsnText();
        Frame<V>[] frames = analyzer.analyze(owner, mn);
        String format = "%03d:    %-36s    %s";
        for (int index = 0; index < size; index++) {
            AbstractInsnNode node = instructions.get(index);
            List<String> nodeLines = insnText.toLines(node);
            Frame<V> f = frames[index];
            String frameLine = FrameUtils.getFrameLine(f, func);
            String firstLine = String.format(format, index, nodeLines.get(0), frameLine);
            builder.append(firstLine).append("\n");
            for (int i = 1; i < nodeLines.size(); i++) {
                String item = nodeLines.get(i);
                String line = String.format("%4s    %-36s", "", item);
                builder.append(line).append("\n");
            }
        }
        builder.append("\n").append("\n");
    }

    public static <V extends Value, T> String getFrameLine(Frame<V> f, Function<V, T> func) {
        if (f == null) {
            return toLine(null, null);
        }

        List<Object> localList = new ArrayList<>();
        for (int i = 0; i < f.getLocals(); ++i) {
            V localValue = f.getLocal(i);
            if (func == null) {
                localList.add(localValue);
            } else {
                T item = func.apply(localValue);
                if (item instanceof String) {
                    if (((String) item).startsWith(".@")) {
                        localList.add("null");
                        continue;
                    }
                }
                localList.add(item);
            }
        }

        List<Object> stackList = new ArrayList<>();
        for (int j = 0; j < f.getStackSize(); ++j) {
            V stackValue = f.getStack(j);
            if (func == null) {
                stackList.add(stackValue);
            } else {
                T item = func.apply(stackValue);
                stackList.add(item);
            }
        }

        return toLine(localList, stackList);
    }

    public static <T> String toLine(List<T> localList, List<T> stackList) {
        String locals_str = toLine(localList);
        String stack_str = toLine(stackList);
        return String.format("%s %s %s", locals_str, SEPARATOR, stack_str);
    }

    private static <T> String toLine(List<T> list) {
        if (list == null || list.isEmpty()) return EMPTY;
        int size = list.size();

        StringBuilder sb = new StringBuilder();
        sb.append(START);
        for (int i = 0; i < size - 1; i++) {
            T item = list.get(i);
            sb.append(item).append(", ");
        }
        sb.append(list.get(size - 1));
        sb.append(STOP);
        return sb.toString();
    }

    public static <V extends Value, T> void printGraph(String owner,
                                                       MethodNode mn,
                                                       Analyzer<V> analyzer,
                                                       Function<V, T> func,
                                                       StringBuilder builder) {
        try {
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
        } catch (Exception ex) {
            logger.error("analyze error: {}", ex.getMessage());
        }
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
