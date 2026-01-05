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

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.PreviewForm;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CommonMouseAdapter extends MouseAdapter {
    private static final Logger logger = LogManager.getLogger();

    private static JFrame frameIns = null;

    @SuppressWarnings("all")
    public void mouseClicked(MouseEvent evt) {
        if (frameIns != null) {
            frameIns.dispose();
        }
        JList<?> list = (JList<?>) evt.getSource();
        // 左键双击
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            MethodResult res = null;
            try {
                res = (MethodResult) list.getModel().getElementAt(index);
            } catch (Exception ignored) {
            }
            if (res == null) {
                return;
            }

            // FIX BUG 2024/09/18
            // 子类通过 this.method 调用父类的 method
            ClassResult nowClass = MainForm.getEngine().getClassByClass(res.getClassName());
            while (nowClass != null) {
                ArrayList<MethodResult> method = MainForm.getEngine().getMethod(
                        nowClass.getClassName(),
                        res.getMethodName(),
                        res.getMethodDesc());
                if (method.size() > 0) {
                    res = method.get(0);
                    logger.debug("find target method in class: {}", nowClass.getClassName());
                    break;
                }
                nowClass = MainForm.getEngine().getClassByClass(nowClass.getSuperClassName());
            }

            String className = res.getClassName();
            String tempPath = className.replace("/", File.separator);
            String classPath;

            classPath = String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath);
            if (!Files.exists(Paths.get(classPath))) {
                classPath = String.format("%s%sBOOT-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                if (!Files.exists(Paths.get(classPath))) {
                    classPath = String.format("%s%sWEB-INF%sclasses%s%s.class",
                            Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                    if (!Files.exists(Paths.get(classPath))) {
                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                "<html>" +
                                        "<p>need dependency or class file not found</p>" +
                                        "<p>缺少依赖或者文件找不到（考虑加载 rt.jar 并检查你的 JAR 是否合法）</p>" +
                                        "<p>默认以三种方式找类：</p>" +
                                        "<p>1.根据类名直接从根目录找（例如 <strong>com/a/b/Demo</strong> ）</p>" +
                                        "<p>2.从 <strong>BOOT-INF</strong> 找（" +
                                        "例如 <strong>BOOT-INF/classes/com/a/Demo</strong> ）</p>" +
                                        "<p>3.从 <strong>WEB-INF</strong> 找（" +
                                        "例如 <strong>WEB-INF/classes/com/a/Demo</strong> ）<p>" +
                                        "</html>");
                        return;
                    }
                }
            }

            String finalClassPath = classPath;

            MethodResult finalRes = res;
            new Thread(() -> {
                // LUCENE 索引处理
                if (LuceneSearchForm.getInstance() != null && LuceneSearchForm.usePaLucene()) {
                    IndexPluginsSupport.addIndex(Paths.get(finalClassPath).toFile());
                }

                String code = DecompileEngine.decompile(Paths.get(finalClassPath));
                String methodName = finalRes.getMethodName();

                int pos = FinderRunner.find(code, methodName, finalRes.getMethodDesc());

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(pos + 1);
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            MethodResult refreshRes = res;
            new Thread() {
                @Override
                public void run() {
                    CoreHelper.refreshAllMethods(className);
                    CoreHelper.refreshCallers(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshCallee(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshHistory(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    CoreHelper.refreshSuperImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                    dialog.dispose();
                }
            }.start();

            MainForm.getInstance().getCurClassText().setText(className);
            String jarName = res.getJarName();
            if (StringUtil.isNull(jarName)) {
                jarName = MainForm.getEngine().getJarByClass(className);
            }
            MainForm.getInstance().getCurJarText().setText(jarName);
            MainForm.getInstance().getCurMethodText().setText(res.getMethodName());
            res.setClassPath(Paths.get(finalClassPath));
            MainForm.setCurMethod(res);

            State newState = new State();
            newState.setClassPath(Paths.get(finalClassPath));
            newState.setJarName(jarName);
            newState.setClassName(res.getClassName());
            newState.setMethodDesc(res.getMethodDesc());
            newState.setMethodName(res.getMethodName());

            int curSI = MainForm.getCurStateIndex();
            if (curSI == -1) {
                MethodResult next = MainForm.getCurMethod();
                MainForm.getStateList().add(curSI + 1, newState);
                MainForm.setCurStateIndex(curSI + 1);
            } else {
                if (curSI >= MainForm.getStateList().size()) {
                    curSI = MainForm.getStateList().size() - 1;
                }
                State state = MainForm.getStateList().get(curSI);
                if (state != null) {
                    MethodResult next = MainForm.getCurMethod();
                    int a = MainForm.getStateList().size();
                    MainForm.getStateList().add(curSI + 1, newState);
                    int b = MainForm.getStateList().size();
                    // 达到最大容量
                    if (a == b) {
                        MainForm.setCurStateIndex(curSI);
                    } else {
                        MainForm.setCurStateIndex(curSI + 1);
                    }
                } else {
                    logger.warn("current state is null");
                }
            }
        } else if (SwingUtilities.isRightMouseButton(evt)) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem addToFavorite = new JMenuItem("add to favorite");
            popupMenu.add(addToFavorite);
            addToFavorite.addActionListener(e -> {
                MethodResult selectedItem = (MethodResult) list.getSelectedValue();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "SELECTED METHOD IS NULL");
                    return;
                }
                MainForm.getFavData().addElement(selectedItem);
                MainForm.getEngine().addFav(selectedItem);
            });

            JMenuItem copyThis = new JMenuItem("copy this");
            popupMenu.add(copyThis);
            copyThis.addActionListener(e -> {
                MethodResult selectedItem = (MethodResult) list.getSelectedValue();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "SELECTED METHOD IS NULL");
                    return;
                }
                StringSelection stringSelection = new StringSelection(selectedItem.getCopyString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), "COPY OK");
            });

            JMenuItem copyAll = new JMenuItem("copy all");
            popupMenu.add(copyAll);
            copyAll.addActionListener(e -> {
                ListModel<?> all = list.getModel();
                if (all.getSize() == 0) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < all.getSize(); i++) {
                    MethodResult mr = (MethodResult) all.getElementAt(i);
                    sb.append(mr.getCopyString());
                    sb.append("\n");
                }
                StringSelection stringSelection = new StringSelection(sb.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), "COPY OK");
            });

            JMenuItem previewItem = new JMenuItem("预览 / preview");
            popupMenu.add(previewItem);
            previewItem.addActionListener(e -> {
                MethodResult selectedItem = (MethodResult) list.getSelectedValue();
                if (selectedItem == null) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "SELECTED METHOD IS NULL");
                    return;
                }
                String className = selectedItem.getClassName();
                String tempPath = className.replace("/", File.separator);
                String classPath;

                classPath = String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath);
                if (!Files.exists(Paths.get(classPath))) {
                    classPath = String.format("%s%sBOOT-INF%sclasses%s%s.class",
                            Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                    if (!Files.exists(Paths.get(classPath))) {
                        classPath = String.format("%s%sWEB-INF%sclasses%s%s.class",
                                Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                        if (!Files.exists(Paths.get(classPath))) {
                            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                    "<html>" +
                                            "<p>need dependency or class file not found</p>" +
                                            "<p>缺少依赖或者文件找不到（考虑加载 rt.jar 并检查你的 JAR 是否合法）</p>" +
                                            "<p>默认以三种方式找类：</p>" +
                                            "<p>1.根据类名直接从根目录找（例如 <strong>com/a/b/Demo</strong> ）</p>" +
                                            "<p>2.从 <strong>BOOT-INF</strong> 找（" +
                                            "例如 <strong>BOOT-INF/classes/com/a/Demo</strong> ）</p>" +
                                            "<p>3.从 <strong>WEB-INF</strong> 找（" +
                                            "例如 <strong>WEB-INF/classes/com/a/Demo</strong> ）<p>" +
                                            "</html>");
                            return;
                        }
                    }
                }

                String code = DecompileEngine.decompile(Paths.get(classPath));
                String methodName = selectedItem.getMethodName();

                int pos = FinderRunner.find(code, methodName, selectedItem.getMethodDesc());

                if (code == null || code.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "找不到代码无法预览");
                    return;
                }

                frameIns = PreviewForm.start(code, pos);
            });

            JMenuItem clearHis = new JMenuItem("清除历史 / clear history");
            popupMenu.add(clearHis);
            clearHis.addActionListener(e -> {
                MainForm.getEngine().cleanHistory();
                MainForm.getInstance().getHistoryListData().clear();
                MainForm.getInstance().getHistoryList().revalidate();
                MainForm.getInstance().getHistoryList().repaint();
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), "CLEAR OK");
            });

            int index = list.locationToIndex(evt.getPoint());
            list.setSelectedIndex(index);
            popupMenu.show(list, evt.getX(), evt.getY());
        }
    }
}
