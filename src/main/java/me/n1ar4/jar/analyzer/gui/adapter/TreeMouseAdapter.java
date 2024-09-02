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

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeMouseAdapter extends MouseAdapter {
    private final JTree fileTree = MainForm.getInstance().getFileTree();
    private final JPanel masterPanel = MainForm.getInstance().getMasterPanel();

    public void mousePressed(MouseEvent e) {
        int selRow = fileTree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = fileTree.getPathForLocation(e.getX(), e.getY());
        if (selRow != -1) {
            if (e.getClickCount() == 2) {
                if (selPath == null) {
                    JOptionPane.showMessageDialog(masterPanel, "chose error");
                    return;
                }
                DecompileHelper.decompile(selPath);

                // 重置所有内容
                MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
            }
        }
    }
}
