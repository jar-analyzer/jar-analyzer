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

package me.n1ar4.jar.analyzer.gui.tree;

import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class FileTree extends JTree {
    private static ImageIcon classIcon;

    static {
        try {
            classIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                    FileTree.class.getClassLoader().getResourceAsStream("img/class.png"))));
        } catch (Exception ignored) {
        }
    }

    private final DefaultTreeModel savedModel;
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel fileTreeModel;

    public FileTree() {
        savedModel = (DefaultTreeModel) this.getModel();
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value,
                    boolean sel, boolean expanded, boolean leaf,
                    int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (leaf && value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    String nodeText = node.getUserObject().toString();
                    String fileExtension = getFileExtension(nodeText);
                    if (fileExtension != null && fileExtension.equalsIgnoreCase("class")) {
                        setText(nodeText.split("\\.")[0]);
                        setIcon(classIcon);
                    }
                }
                return this;
            }

            private String getFileExtension(String fileName) {
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    return fileName.substring(dotIndex + 1);
                }
                return null;
            }
        };
        this.setCellRenderer(renderer);

        setModel(null);
    }

    public void refresh() {
        setModel(savedModel);
        fileTreeModel = (DefaultTreeModel) treeModel;
        initComponents();
        initListeners();
        repaint();
    }

    private void initComponents() {
        initRoot();
        setEditable(false);
    }

    private void initListeners() {
        // 2024-07-31 删除 addTreeSelectionListener
        // 不需要提供自动的滚动功能 影响正常使用
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                clearSelection();
                TreePath path = event.getPath();
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                treeNode.removeAllChildren();
                populateSubTree(treeNode);
                fileTreeModel.nodeStructureChanged(treeNode);
            }
        });
    }

    private void initRoot() {
        File[] roots;
        roots = new File[]{new File(Const.tempDir)};
        rootNode = new DefaultMutableTreeNode(new FileTreeNode(roots[0]));
        populateSubTree(rootNode);
        if (fileTreeModel != null && rootNode != null) {
            fileTreeModel.setRoot(rootNode);
        }
    }

    private void populateSubTree(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof FileTreeNode) {
            FileTreeNode fileTreeNode = (FileTreeNode) userObject;
            File[] files = fileTreeNode.file.listFiles();
            if (files == null) {
                return;
            }

            List<File> fileList = Arrays.asList(files);
            fileList.sort((o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                boolean isClassFile1 = name1.endsWith(".class");
                boolean isClassFile2 = name2.endsWith(".class");
                if (isClassFile1 && !isClassFile2) {
                    return 1;
                }
                if (!isClassFile1 && isClassFile2) {
                    return -1;
                }
                return name1.compareTo(name2);
            });

            for (File file : fileList) {

                TreeFileFilter filter = new TreeFileFilter(file, true, true);
                if (filter.shouldFilter()) {
                    continue;
                }

                FileTreeNode subFile = new FileTreeNode(file);
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subFile);
                if (file.isDirectory()) {
                    subNode.add(new DefaultMutableTreeNode("fake"));
                }
                node.add(subNode);

                try {
                    addSelectionPath(new TreePath(node.getPath()));
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static volatile boolean found = false;

    public static void setFound(boolean found) {
        FileTree.found = found;
    }

    public static boolean isFound() {
        return found;
    }

    private void expandPathTarget(Enumeration<?> parent, String[] split) {
        if (found) {
            return;
        }
        while (parent.hasMoreElements()) {
            DefaultMutableTreeNode children = (DefaultMutableTreeNode) parent.nextElement();
            for (int i = 0; i < split.length - 1; i++) {
                if (children.toString().equals(split[i])) {
                    if (!found) {
                        expandPath(new TreePath(children.getPath()));
                    }
                    if (split.length - 2 == i) {
                        Enumeration<?> children2 = children.children();
                        while (children2.hasMoreElements()) {
                            DefaultMutableTreeNode end = (DefaultMutableTreeNode) children2.nextElement();
                            String var0 = "";
                            if (split[split.length - 1].contains("$")) {
                                var0 = StrUtil.subBefore(split[split.length - 1], "$", false);
                            }
                            if (end.toString().equals(split[split.length - 1] + ".class") ||
                                    (StrUtil.isNotEmpty(var0) && end.toString().equals(var0 + ".class"))) {
                                TreePath tempPath = new TreePath(end.getPath());
                                setSelectionPath(tempPath);
                                scrollPathToVisible(tempPath);
                                found = true;
                                return;
                            }
                        }
                    }
                    expandPathTarget(children.children(), split);
                }
            }
        }
    }

    public void searchPathTarget(String classname) {
        refresh();
        String[] split = classname.split("/");

        // CHECK FILE EXIST
        Path dir = Paths.get(Const.tempDir);
        Path classPath = dir.resolve(classname + ".class");
        if (!Files.exists(classPath)) {
            classname = "BOOT-INF/classes/" + classname;
            classPath = dir.resolve(classname + ".class");
            if (!Files.exists(classPath)) {
                LogUtil.warn("class not found");
                return;
            }
            split = classname.split("/");
        }

        Enumeration<?> children = rootNode.children();
        FileTree.setFound(false);
        expandPathTarget(children, split);
    }
}
