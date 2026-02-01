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
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.JarUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LeakResultMouseAdapter extends MouseAdapter {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    public void mouseClicked(MouseEvent evt) {
        JList<?> list = (JList<?>) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            LeakResult res = (LeakResult) list.getModel().getElementAt(index);

            String className = res.getClassName();

            // 2026/02/02 修复静态文件无法打开问题
            boolean resource = false;
            for (String ext : JarUtil.CONFIG_EXTENSIONS) {
                if (className.endsWith(ext)) {
                    resource = true;
                }
            }
            if (resource) {
                String tempPath = className.replace("/", File.separator);
                tempPath = String.format("%s%s%s", Const.tempDir, File.separator, tempPath);
                logger.debug("open resource {} file",tempPath);
                Path resourcePath = Paths.get(tempPath);
                if (Files.exists(resourcePath)) {
                    try {
                        String resourceContent = new String(Files.readAllBytes(resourcePath));
                        MainForm.getCodeArea().setText(resourceContent);
                        // 找 caret position
                        String value = res.getValue();
                        int idx = resourceContent.indexOf(value);
                        if (idx != -1) {
                            MainForm.getCodeArea().setSelectionStart(idx);
                            MainForm.getCodeArea().setSelectionEnd(idx + value.length());
                            // FIX BUG
                            MainForm.getCodeArea().setCaretPosition(idx);
                        }else{
                            MainForm.getCodeArea().setCaretPosition(0);
                        }
                        return;
                    } catch (Exception ex) {
                        MainForm.getCodeArea().setText(ex.getMessage());
                        MainForm.getCodeArea().setCaretPosition(0);
                        return;
                    }
                }
                MainForm.getCodeArea().setText("file not found");
                MainForm.getCodeArea().setCaretPosition(0);
                return;
            }

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

            new Thread(() -> {
                String code = DecompileEngine.decompile(Paths.get(finalClassPath));

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                String value = res.getValue();
                int idx = code.indexOf(value);
                if (idx != -1) {
                    MainForm.getCodeArea().setText(code);
                    MainForm.getCodeArea().setSelectionStart(idx);
                    MainForm.getCodeArea().setSelectionEnd(idx + value.length());
                    // FIX BUG
                    MainForm.getCodeArea().setCaretPosition(idx);
                } else {
                    MainForm.getCodeArea().setText(code);
                    MainForm.getCodeArea().setCaretPosition(0);
                }
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread() {
                @Override
                public void run() {
                    CoreHelper.refreshAllMethods(className);
                    dialog.dispose();
                }
            }.start();

            MainForm.getInstance().getCurClassText().setText(className);
            String jarName = MainForm.getEngine().getJarByClass(className);
            if (StringUtil.isNull(jarName)) {
                jarName = MainForm.getEngine().getJarByClass(className);
            }
            MainForm.getInstance().getCurJarText().setText(jarName);
            MainForm.getInstance().getCurMethodText().setText(null);
            MainForm.setCurMethod(null);

            MainForm.setCurClass(className);

            // 重置所有内容
            MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
        }
    }
}
