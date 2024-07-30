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
