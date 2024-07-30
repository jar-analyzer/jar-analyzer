package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import javax.swing.tree.TreePath;
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

    @SuppressWarnings("all")
    public TreeRightMenuAdapter() {
        popupMenu = new JPopupMenu();
        JMenuItem decompileItem = new JMenuItem("Decompile");
        JMenuItem superClassItem = new JMenuItem("Super Class");
        popupMenu.add(decompileItem);
        popupMenu.add(superClassItem);

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

                if (className.contains("BOOT-INF")) {
                    className = className.substring(i + 8, className.length() - 7);
                } else if (className.contains("WEB-INF")) {
                    className = className.substring(i + 7, className.length() - 7);
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
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            int row = fileTree.getRowForLocation(e.getX(), e.getY());
            TreePath path = fileTree.getPathForRow(row);
            if (path == null || path.getLastPathComponent() == null) {
                return;
            }
            if (!path.getLastPathComponent().toString().endsWith(".class")) {
                return;
            }
            fileTree.setSelectionPath(path);
            if (row >= 0) {
                popupMenu.show(fileTree, e.getX(), e.getY());
            }
        }
    }
}
