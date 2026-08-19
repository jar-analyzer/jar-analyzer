/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import me.n1ar4.jar.analyzer.starter.Const;

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FileTree extends JTree {

    /**
     * Maps a resolved {@link ClassIconKind} to its SVG icon. We keep
     * this mapping local to the tree because only the file tree needs
     * the per-kind distinction; other call-sites that draw a class icon
     * (search results, favourites...) keep using the generic ClassIcon.
     */
    private static FlatSVGIcon iconFor(ClassIconKind kind) {
        if (kind == null) {
            return SvgManager.ClassIcon;
        }
        switch (kind) {
            case ABSTRACT_CLASS:
                return SvgManager.AbstractClassIcon;
            case INTERFACE:
                return SvgManager.InterfaceIcon;
            case ANNOTATION:
                return SvgManager.AnnotationIcon;
            case ENUM:
                return SvgManager.EnumIcon;
            case RECORD:
                return SvgManager.RecordIcon;
            case EXCEPTION:
                return SvgManager.ExceptionIcon;
            case CLASS:
            case UNKNOWN:
            default:
                return SvgManager.ClassIcon;
        }
    }

    private final DefaultTreeModel savedModel;
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel fileTreeModel;
    private boolean listenersInstalled;

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
                    Object userObj = node.getUserObject();
                    File file = null;
                    String nodeText;
                    try {
                        nodeText = userObj == null ? "" : userObj.toString();
                    } catch (Throwable t) {
                        // userObject.toString() must never break the
                        // paint loop -- a single bad node would otherwise
                        // blank out the whole tree on the EDT.
                        nodeText = "";
                    }
                    if (userObj instanceof FileTreeNode) {
                        file = ((FileTreeNode) userObj).file;
                    }
                    String ext = getFileExtension(nodeText);
                    if (ext != null && "class".equalsIgnoreCase(ext)) {
                        // Strip the ".class" suffix for display, matching
                        // the previous behavior.
                        int dot = nodeText.lastIndexOf('.');
                        if (dot > 0) {
                            setText(nodeText.substring(0, dot));
                        }
                        // Decide which SVG to show. If the kind is not
                        // yet cached, paint the generic ClassIcon now
                        // and ask the resolver to repaint the whole
                        // tree once the answer arrives.
                        ClassIconKind kind = null;
                        if (file != null) {
                            try {
                                kind = ClassKindResolver.getCached(file, FileTree.this::repaint);
                            } catch (Throwable t) {
                                // Resolver is best-effort; never crash
                                // rendering on its account.
                                kind = null;
                            }
                        }
                        setIcon(iconFor(kind));
                    } else if (file != null && file.isFile()) {
                        // Non-class leaf: pick an icon by file-name rules.
                        // Pure pattern matching, no I/O. The try/catch is
                        // defensive: ResourceFileKind is written to be
                        // total, but a buggy future case branch must not
                        // be allowed to abort the EDT paint cycle.
                        try {
                            ResourceFileKind.Kind rk = ResourceFileKind.classify(nodeText);
                            setIcon(ResourceFileKind.iconFor(rk));
                        } catch (Throwable t) {
                            setIcon(SvgManager.FileIcon);
                        }
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

    /**
     * 重建整棵树并保持展开与选中状态不变。
     * 用于配置切换（如 show inner class）这类只需要重新过滤、
     * 不应该丢失用户当前浏览位置的场景。
     */
    public void refreshKeepingExpansion() {
        if (rootNode == null) {
            refresh();
            return;
        }

        Path base = Paths.get(Const.tempDir).normalize();

        // 刷新后的节点是全新对象，TreePath 无法直接复用，
        // 只能按“相对 tempDir 的文件路径”记录再逐层恢复
        Set<Path> expandedRels = new LinkedHashSet<>();
        Enumeration<TreePath> expanded = getExpandedDescendants(new TreePath(rootNode.getPath()));
        while (expanded != null && expanded.hasMoreElements()) {
            Path rel = relOf(expanded.nextElement().getLastPathComponent(), base);
            if (rel != null) {
                expandedRels.add(rel);
            }
        }
        Path selectedRel = null;
        TreePath selection = getSelectionPath();
        if (selection != null) {
            selectedRel = relOf(selection.getLastPathComponent(), base);
        }

        refresh();

        if (rootNode == null) {
            return;
        }
        restoreExpansion(rootNode, base, expandedRels);
        if (selectedRel != null) {
            DefaultMutableTreeNode node = selectedRel.getNameCount() == 0
                    ? rootNode : findNodeByRel(rootNode, selectedRel);
            if (node != null) {
                setSelectionPath(new TreePath(node.getPath()));
            }
        }
    }

    private static Path relOf(Object treeNode, Path base) {
        if (!(treeNode instanceof DefaultMutableTreeNode)) {
            return null;
        }
        Object userObject = ((DefaultMutableTreeNode) treeNode).getUserObject();
        if (!(userObject instanceof FileTreeNode)) {
            return null;
        }
        Path path = ((FileTreeNode) userObject).file.toPath();
        if (!path.startsWith(base)) {
            return null;
        }
        return base.relativize(path);
    }

    private void restoreExpansion(DefaultMutableTreeNode node, Path base, Set<Path> expandedRels) {
        Path rel = relOf(node, base);
        if (rel == null || !expandedRels.contains(rel)) {
            return;
        }
        // 程序化 expandPath 会触发 treeExpanded 监听器完成懒加载，
        // 返回后该节点的真实子节点已就绪，可以继续向下恢复
        expandPath(new TreePath(node.getPath()));
        for (int i = 0; i < node.getChildCount(); i++) {
            restoreExpansion((DefaultMutableTreeNode) node.getChildAt(i), base, expandedRels);
        }
    }

    private DefaultMutableTreeNode findNodeByRel(DefaultMutableTreeNode start, Path targetRel) {
        DefaultMutableTreeNode current = start;
        for (int i = 0; i < targetRel.getNameCount(); i++) {
            String component = targetRel.getName(i).toString();
            DefaultMutableTreeNode next = null;
            for (int c = 0; c < current.getChildCount(); c++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) current.getChildAt(c);
                if (child.getUserObject() instanceof FileTreeNode
                        && ((FileTreeNode) child.getUserObject()).file.getName().equals(component)) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }

    private void initComponents() {
        initRoot();
        setEditable(false);
    }

    private void initListeners() {
        // refresh() 可能被反复调用（搜索定位、配置切换），监听器只能注册一次
        if (listenersInstalled) {
            return;
        }
        listenersInstalled = true;
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

            // 将文件分为目录和普通文件两组
            List<File> directories = new ArrayList<>();
            List<File> regularFiles = new ArrayList<>();

            for (File file : files) {
                TreeFileFilter filter = new TreeFileFilter(file, true, true);
                if (filter.shouldFilter()) {
                    continue;
                }
                if (file.isDirectory()) {
                    directories.add(file);
                } else {
                    regularFiles.add(file);
                }
            }

            // 分别对目录和文件进行排序
            directories.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            regularFiles.sort((o1, o2) -> {
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
                return name1.compareToIgnoreCase(name2);
            });

            // 先添加目录
            for (File dir : directories) {
                FileTreeNode subFile = new FileTreeNode(dir);
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subFile);
                subNode.add(new DefaultMutableTreeNode("fake"));
                node.add(subNode);
            }

            // 再添加文件
            for (File file : regularFiles) {
                FileTreeNode subFile = new FileTreeNode(file);
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subFile);
                node.add(subNode);
            }

            try {
                addSelectionPath(new TreePath(node.getPath()));
            } catch (Exception ignored) {
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
        String originClassName = classname;
        String[] split = originClassName.split("/");

        // CHECK FILE EXIST（先解析真实落盘路径，再重建树）
        Path dir = Paths.get(Const.tempDir);
        Path classPath = dir.resolve(classname + ".class");
        if (!Files.exists(classPath)) {
            classname = "BOOT-INF/classes/" + originClassName;
            classPath = dir.resolve(classname + ".class");
            // 2025/04/09 BUG
            // 处理 WEB-INF 情况左侧文件树无法自动定位的问题
            if (!Files.exists(classPath)) {
                classname = "WEB-INF/classes/" + originClassName;
                classPath = dir.resolve(classname + ".class");
                if (!Files.exists(classPath)) {
                    LogUtil.warn("class not found");
                    return;
                }
            }
            split = classname.split("/");
        }

        // show inner class 关闭时内部类整体隐藏，被跳转的这一个
        // 需要单独放行；跳转普通类时清空，避免残留
        TreeFileFilter.setExceptedInnerClass(
                split[split.length - 1].contains("$") ? classPath : null);

        refresh();

        Enumeration<?> children = rootNode.children();
        FileTree.setFound(false);
        expandPathTarget(children, split);
    }
}
