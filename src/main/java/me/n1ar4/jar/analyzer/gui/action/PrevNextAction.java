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

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.Type;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrevNextAction {
    public static void run() {
        MainForm instance = MainForm.getInstance();
        MainForm.setPrevState(null);
        MainForm.setNextState(null);
        MainForm.setCurState(null);
        instance.getPrevBtn().addActionListener(e -> {
            if (MainForm.getPrevState() == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "you cannot do it");
                return;
            }

            // SAVE CURRENT STATE
            State cur = new State();
            if (MainForm.getCurMethod() == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }
            cur.setClassName(MainForm.getCurMethod().getClassName());
            cur.setClassPath(MainForm.getCurMethod().getClassPath());
            cur.setJarName(MainForm.getCurMethod().getJarName());
            cur.setMethodName(MainForm.getCurMethod().getMethodName());
            cur.setMethodDesc(MainForm.getCurMethod().getMethodDesc());
            MainForm.setNextState(cur);

            // CHANGE STATE
            State prev = MainForm.getPrevState();
            MainForm.setPrevState(null);
            MainForm.setCurState(prev);
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
                if (methodName.equals("<init>")) {
                    String[] c = m.getClassName().split("/");
                    methodName = c[c.length - 1];
                }
                int paramNum = Type.getMethodType(
                        m.getMethodDesc()).getArgumentTypes().length;
                int pos = FinderRunner.find(code, methodName, paramNum);

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
            if (MainForm.getNextState() == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "you cannot do it");
                return;
            }

            // SAVE CURRENT STATE
            State cur = new State();
            if (MainForm.getCurMethod() == null) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }
            cur.setClassName(MainForm.getCurMethod().getClassName());
            cur.setClassPath(MainForm.getCurMethod().getClassPath());
            cur.setJarName(MainForm.getCurMethod().getJarName());
            cur.setMethodName(MainForm.getCurMethod().getMethodName());
            cur.setMethodDesc(MainForm.getCurMethod().getMethodDesc());
            MainForm.setPrevState(cur);

            // CHANGE STATE
            State next = MainForm.getNextState();
            MainForm.setCurState(next);
            MainForm.setNextState(null);
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
                if (methodName.equals("<init>")) {
                    String[] c = m.getClassName().split("/");
                    methodName = c[c.length - 1];
                }
                int paramNum = Type.getMethodType(
                        m.getMethodDesc()).getArgumentTypes().length;
                int pos = FinderRunner.find(code, methodName, paramNum);

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
