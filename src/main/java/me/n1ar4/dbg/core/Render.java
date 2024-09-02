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

package me.n1ar4.dbg.core;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import me.n1ar4.dbg.gui.MainForm;
import me.n1ar4.dbg.gui.TableManager;
import me.n1ar4.dbg.parser.MethodObject;
import me.n1ar4.dbg.parser.OpcodeObject;
import me.n1ar4.dbg.utils.ASMUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.List;

public class Render {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    private static String randOperands(String operands) {
        if (operands == null || operands.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<font style=\"font-weight: bold;\">");
        sb.append(operands);
        sb.append(" (");
        int unsignedInt = Integer.parseInt(operands, 16);
        int signedInt = unsignedInt > 0x7FFF ? unsignedInt - 0x10000 : unsignedInt;
        sb.append(signedInt);
        sb.append(") ");
        sb.append("</font>");
        sb.append("</html>");
        return sb.toString();
    }

    @SuppressWarnings("all")
    private static String rendOpcode(String opcodeStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<font style=\"color: blue; font-weight: bold;\">");
        sb.append(opcodeStr);
        sb.append("</font>");
        sb.append("</html>");
        return sb.toString();
    }

    public static void refreshMethodModel(MethodObject object, long l) {
        JTable bytecodeTable = MainForm.getInstance().getBytecodeTable();
        String[] columnNames = {"null", "null", "null", "null"};
        Object[][] data = new Object[object.getOpcodes().size() + 1][];
        data[0] = new Object[]{null, "Method", "JVM Opcode", "Operands"};

        MainForm.getInstance().getCurMethodText().setText(
                ASMUtil.convertMethodDesc(
                        object.getMethodName(),
                        object.getMethodDec()));
        MainForm.getInstance().getCurClassText().setText(
                ASMUtil.renderClass(object.getClassName()));

        int debugRow = 0;
        for (int i = 1; i < data.length; i++) {
            OpcodeObject op = object.getOpcodes().get(i - 1);
            if (op.getOpcodeIndex() == l) {
                debugRow = i;
            }
            data[i] = new Object[]{null,
                    String.format("%08x", op.getOpcodeIndex()),
                    rendOpcode(op.getOpcodeStr()),
                    randOperands(op.getOperands())};
            if (op.getOpcodeIndex() == l) {
                if (op.getOpcodeStr().equals("GOTO")) {
                    int unsignedInt = Integer.parseInt(op.getOperands(), 16);
                    int signedInt = unsignedInt > 0x7FFF ? unsignedInt - 0x10000 : unsignedInt;
                    long target = l + signedInt;
                    logger.info("goto location: {}", target);
                    TableManager.addJump(target);
                }
            }
        }

        if (debugRow == 0) {
            logger.error("current debug line error");
            return;
        }
        TableManager.addHighlight(debugRow);
        TableManager.setCur(l);

        for (int i = 1; i < data.length; i++) {
            OpcodeObject op = object.getOpcodes().get(i - 1);
            if (op.getOpcodeIndex() == TableManager.getJumpLocation()) {
                TableManager.addJumpRow(i);
                break;
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // SET DATA
        bytecodeTable.setModel(model);

        // SET FIRST DEBUG COLUMN
        TableColumn column = bytecodeTable.getColumnModel().getColumn(0);
        column.setMinWidth(30);
        column.setMaxWidth(30);
        column.setPreferredWidth(30);

        bytecodeTable.repaint();
    }

    public static void refreshFrames(List<StackFrame> frames) {
        JTable frameTable = MainForm.getInstance().getThreadStackTable();
        String[] columnNames = {"thread name", "stack method"};
        Object[][] data = new Object[frames.size()][];
        int i = 0;
        for (StackFrame frame : frames) {
            Location object = frame.location();
            data[i] = new Object[]{
                    frame.thread().name(),
                    ASMUtil.convertMethodDescWithClass(
                            object.declaringType().name(),
                            object.method().name(),
                            object.method().signature())};
            i++;
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        frameTable.setModel(model);
        TableColumn column = frameTable.getColumnModel().getColumn(0);
        column.setMinWidth(100);
        column.setMaxWidth(100);
        column.setPreferredWidth(100);
        frameTable.repaint();
    }

    public static void refreshVariables(StackFrame frame, List<LocalVariable> localVariables) {
        JTable varTable = MainForm.getInstance().getLocalVariablesTable();
        String[] columnNames = {"variable name", "variable type", "variable value"};
        Object[][] data = new Object[localVariables.size()][];
        int i = 0;
        for (LocalVariable var : localVariables) {
            Value value = frame.getValue(var);
            data[i] = new Object[]{var.name(), var.signature(), value.toString()};
            i++;
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        varTable.setModel(model);
        varTable.repaint();
    }
}
