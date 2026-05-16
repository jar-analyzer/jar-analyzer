/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeMouseAdapter extends MouseAdapter {
    private final JTree fileTree = MainForm.getInstance().getFileTree();
    private final JPanel masterPanel = MainForm.getInstance().getMasterPanel();

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() != 2) {
            return;
        }
        // Resolve the row by Y only, so a double-click anywhere on the
        // row -- including the empty space to the right of the label --
        // counts as activating that node, not just the label rectangle
        // (which is what getRowForLocation requires).
        int row = rowAtY(e.getY());
        if (row < 0) {
            return;
        }
        TreePath selPath = fileTree.getPathForRow(row);
        if (selPath == null) {
            JOptionPane.showMessageDialog(masterPanel, "chose error");
            return;
        }
        // Sync the selection so the user gets visual feedback on the
        // exact row the click was attributed to (matches IDE behavior).
        fileTree.setSelectionPath(selPath);

        // DecompileHelper is a no-op for directories / non-class /
        // non-config files, so we don't need to filter here.
        DecompileHelper.decompile(selPath);

        MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
        MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
        MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
        MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
    }

    /**
     * Maps a Y pixel coordinate to a tree row, requiring the click to
     * fall within the row's vertical bounds. Returns -1 when the click
     * is below the last row (i.e. dead space below the tree), so we
     * don't accidentally trigger a decompile by clicking blank area.
     */
    private int rowAtY(int y) {
        int row = fileTree.getClosestRowForLocation(0, y);
        if (row < 0) {
            return -1;
        }
        Rectangle bounds = fileTree.getRowBounds(row);
        if (bounds == null) {
            return -1;
        }
        if (y < bounds.y || y >= bounds.y + bounds.height) {
            return -1;
        }
        return row;
    }
}
