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
                                "need dependency");
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
                                "need dependency");
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
