/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("all")
public class PrevNextAction {
    public static void run() {
        MainForm instance = MainForm.getInstance();
        MainForm.setCurStateIndex(-1);
        MainForm.getStateList().clear();
        instance.getPrevBtn().addActionListener(e -> {
            // 当前是 0 或者上一个是 null 不允许上一步
            if (MainForm.getCurStateIndex() <= 0 ||
                    MainForm.getStateList().get(MainForm.getCurStateIndex() - 1) == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), String.format("<html>" +
                                "<p style='color: red; font-weight: bold;'>You cannot do it</p>" +
                                "<p>Current idx: <span style='color: blue; font-weight: bold;'>%d</span></p>" +
                                "<p>Total length: <span style='color: blue; font-weight: bold;'>%d</span></p>" +
                                "</html>", MainForm.getCurStateIndex(), MainForm.getStateList().size()),
                        "prev next action", JOptionPane.INFORMATION_MESSAGE, IconManager.ausIcon);
            }
            if (MainForm.getCurMethod() == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }

            // 改变指针不改变内容
            MainForm.setCurStateIndex(MainForm.getCurStateIndex() - 1);
            if (MainForm.getCurStateIndex() < 0) {
                MainForm.setCurStateIndex(0);
            }

            // 变更状态
            State prev = MainForm.getStateList().get(MainForm.getCurStateIndex());
            if (prev == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "invalid previous state");
                return;
            }
            instance.getCurJarText().setText(prev.getJarName());
            instance.getCurClassText().setText(prev.getClassName());
            instance.getCurMethodText().setText(prev.getMethodName());
            MethodResult m = new MethodResult();
            Path path = prev.getClassPath();
            m.setJarName(prev.getJarName());
            m.setMethodName(prev.getMethodName());
            m.setMethodDesc(prev.getMethodDesc());
            m.setClassName(prev.getClassName());
            m.setClassPath(path);
            MainForm.setCurMethod(m);

            // DECOMPILE
            String className = m.getClassName();
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
                if (code == null) {
                    return;
                }
                String methodName = m.getMethodName();

                int pos = FinderRunner.find(code, methodName, m.getMethodDesc());

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(pos + 1);
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();

            // REFRESH
            new Thread(() -> {
                CoreHelper.refreshAllMethods(className);
                CoreHelper.refreshCallers(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshCallee(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshHistory(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshImpls(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshSuperImpls(className, m.getMethodName(), m.getMethodDesc());
                dialog.dispose();
            }).start();
        });


        instance.getNextBtn().addActionListener(e -> {
            // 当前是最后一个元素 或 下一个元素是空
            if (MainForm.getCurStateIndex() >= MainForm.getStateList().size() - 1 ||
                    MainForm.getStateList().get(MainForm.getCurStateIndex() + 1) == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), String.format("<html>" +
                                "<p style='color: red; font-weight: bold;'>You cannot do it</p>" +
                                "<p>Current idx: <span style='color: blue; font-weight: bold;'>%d</span></p>" +
                                "<p>Total length: <span style='color: blue; font-weight: bold;'>%d</span></p>" +
                                "</html>", MainForm.getCurStateIndex(), MainForm.getStateList().size()),
                        "prev next action", JOptionPane.INFORMATION_MESSAGE, IconManager.ausIcon);
                return;
            }

            // 改变指针不改变内容
            MainForm.setCurStateIndex(MainForm.getCurStateIndex() + 1);
            if (MainForm.getCurStateIndex() >= MainForm.getStateList().size()) {
                MainForm.setCurStateIndex(MainForm.getStateList().size() - 1);
            }

            State next = MainForm.getStateList().get(MainForm.getCurStateIndex());
            if (next == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "invalid next state");
                return;
            }
            instance.getCurJarText().setText(next.getJarName());
            instance.getCurClassText().setText(next.getClassName());
            instance.getCurMethodText().setText(next.getMethodName());
            MethodResult m = new MethodResult();
            Path path = next.getClassPath();
            m.setJarName(next.getJarName());
            m.setMethodName(next.getMethodName());
            m.setMethodDesc(next.getMethodDesc());
            m.setClassPath(path);
            m.setClassName(next.getClassName());
            MainForm.setCurMethod(m);

            // DECOMPILE
            String className = m.getClassName();
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
                if (code == null) {
                    return;
                }
                String methodName = m.getMethodName();

                int pos = FinderRunner.find(code, methodName, m.getMethodDesc());

                // SET FILE TREE HIGHLIGHT

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(pos + 1);
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();

            // REFRESH
            new Thread(() -> {
                CoreHelper.refreshAllMethods(className);
                CoreHelper.refreshCallers(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshCallee(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshHistory(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshImpls(className, m.getMethodName(), m.getMethodDesc());
                CoreHelper.refreshSuperImpls(className, m.getMethodName(), m.getMethodDesc());
                dialog.dispose();
            }).start();
        });
    }
}
