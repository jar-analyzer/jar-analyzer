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

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.tree.FileTreeNode;
import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.jar.analyzer.utils.OpenUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TreeRightMenuAdapter extends MouseAdapter {
    private static final String TIPS = "<html>" +
            "super class is missing (need <b>rt.jar</b>)<br>" +
            "maybe super class is <b>java.lang.Object</b> from rt.jar" +
            "</html>";
    private final JTree fileTree = MainForm.getInstance().getFileTree();
    private final JPopupMenu popupMenu;
    private final JPopupMenu dirPopupMenu;


    public TreeRightMenuAdapter() {
        popupMenu = new JPopupMenu();
        JMenuItem decompileItem = new JMenuItem("DECOMPILE");
        decompileItem.setIcon(IconManager.engineIcon);
        JMenuItem superClassItem = new JMenuItem("SUPER CLASS");
        superClassItem.setIcon(IconManager.pubIcon);
        JMenuItem openItem = new JMenuItem("OPEN IN EXPLORER");
        openItem.setIcon(IconManager.fileIcon);
        popupMenu.add(decompileItem);
        popupMenu.add(superClassItem);
        popupMenu.add(openItem);

        dirPopupMenu = new JPopupMenu();
        JMenuItem expandItem = new JMenuItem("展开");
        expandItem.setIcon(IconManager.fileSmallIcon);
        JMenuItem collapseItem = new JMenuItem("折叠");
        collapseItem.setIcon(IconManager.fileSmallIcon);
        JMenuItem expandAllItem = new JMenuItem("展开所有");
        expandAllItem.setIcon(IconManager.fileSmallIcon);
        JMenuItem collapseAllItem = new JMenuItem("折叠所有");
        collapseAllItem.setIcon(IconManager.fileSmallIcon);
        dirPopupMenu.add(expandItem);
        dirPopupMenu.add(collapseItem);
        dirPopupMenu.add(expandAllItem);
        dirPopupMenu.add(collapseAllItem);

        openItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                String sel = selectedPath.toString();
                sel = sel.substring(1, sel.length() - 1);
                String[] selArray = sel.split(",");
                ArrayList<String> pathList = new ArrayList<>();
                for (String s : selArray) {
                    s = s.trim();
                    pathList.add(s);
                }

                String[] path = pathList.toArray(new String[0]);
                String filePath = String.join(File.separator, path);

                OpenUtil.openFileInExplorer(Paths.get(filePath).toAbsolutePath().toString());
            }
        });

        decompileItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                DecompileHelper.decompile(selectedPath);
            }
        });

        superClassItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                String sel = selectedPath.toString();
                sel = sel.substring(1, sel.length() - 1);
                String[] selArray = sel.split(",");
                ArrayList<String> pathList = new ArrayList<>();
                for (String s : selArray) {
                    s = s.trim();
                    pathList.add(s);
                }

                String[] path = pathList.toArray(new String[0]);
                String filePath = String.join(File.separator, path);

                if (!filePath.endsWith(".class")) {
                    return;
                }

                Path thePath = Paths.get(filePath);
                if (!Files.exists(thePath)) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "file not exist");
                    return;
                }

                StringBuilder classNameBuilder = new StringBuilder();
                for (int i = 1; i < path.length; i++) {
                    classNameBuilder.append(path[i]).append("/");
                }
                String className = classNameBuilder.toString();
                int i = className.indexOf("classes");

                if (className.contains("BOOT-INF") || className.contains("WEB-INF")) {
                    className = className.substring(i + 8, className.length() - 7);
                } else {
                    className = className.substring(0, className.length() - 7);
                }

                ClassResult classResult = MainForm.getEngine().getClassByClass(className);
                String absPath = MainForm.getEngine().getAbsPath(classResult.getSuperClassName());

                if (StringUtil.isNull(absPath)) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), TIPS);
                    return;
                }

                Path absPathPath = Paths.get(absPath);
                if (!Files.exists(absPathPath)) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), TIPS);
                    return;
                }

                // LUCENE 索引处理
                if (LuceneSearchForm.getInstance() != null && LuceneSearchForm.usePaLucene()) {
                    IndexPluginsSupport.addIndex(absPathPath.toFile());
                }
                String code = DecompileEngine.decompile(absPathPath);

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(0);

                CoreHelper.refreshAllMethods(className);

                MainForm.getInstance().getCurClassText().setText(className);
                String jarName = MainForm.getEngine().getJarByClass(className);
                MainForm.getInstance().getCurJarText().setText(jarName);
                MainForm.getInstance().getCurMethodText().setText(null);
                MainForm.setCurMethod(null);

                // 重置所有内容
                MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
                MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
            }
        });

        expandItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                fileTree.expandPath(selectedPath);
            }
        });

        collapseItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                fileTree.collapsePath(selectedPath);
            }
        });

        expandAllItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                setExpandedRecursive(selectedPath, true);
            }
        });

        collapseAllItem.addActionListener(e -> {
            TreePath selectedPath = fileTree.getSelectionPath();
            if (selectedPath != null) {
                setExpandedRecursive(selectedPath, false);
            }
        });
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int row = fileTree.getClosestRowForLocation(e.getX(), e.getY());
            if (row < 0) {
                return;
            }
            Rectangle bounds = fileTree.getRowBounds(row);
            if (bounds == null || e.getY() < bounds.y || e.getY() > bounds.y + bounds.height) {
                return;
            }
            TreePath path = fileTree.getPathForRow(row);
            if (path == null || path.getLastPathComponent() == null) {
                return;
            }
            fileTree.setSelectionPath(path);
            if (isClassNode(path)) {
                popupMenu.show(fileTree, e.getX(), e.getY());
            } else if (isDirectoryNode(path)) {
                dirPopupMenu.show(fileTree, e.getX(), e.getY());
            }
        }
    }

    private boolean isClassNode(TreePath path) {
        Object component = path.getLastPathComponent();
        return component != null && component.toString().endsWith(".class");
    }

    private boolean isDirectoryNode(TreePath path) {
        Object component = path.getLastPathComponent();
        if (!(component instanceof DefaultMutableTreeNode)) {
            return false;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) component;
        Object userObject = node.getUserObject();
        if (userObject instanceof FileTreeNode) {
            FileTreeNode fileNode = (FileTreeNode) userObject;
            return fileNode.file != null && fileNode.file.isDirectory();
        }
        return false;
    }

    private void setExpandedRecursive(TreePath parent, boolean expand) {
        if (parent == null) {
            return;
        }
        if (expand) {
            fileTree.expandPath(parent);
        }
        Object component = parent.getLastPathComponent();
        if (component instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) component;
            int count = node.getChildCount();
            for (int i = 0; i < count; i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                setExpandedRecursive(parent.pathByAddingChild(child), expand);
            }
        }
        if (!expand) {
            fileTree.collapsePath(parent);
        }
    }
}
