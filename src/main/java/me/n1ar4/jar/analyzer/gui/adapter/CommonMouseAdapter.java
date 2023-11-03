package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.core.Finder;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import org.objectweb.asm.Type;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CommonMouseAdapter extends MouseAdapter {
    @SuppressWarnings("all")
    public void mouseClicked(MouseEvent evt) {
        JList<?> list = (JList<?>) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            MethodResult res = (MethodResult) list.getModel().getElementAt(index);

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
                                "need dependency");
                        return;
                    }
                }
            }

            String finalClassPath = classPath;

            new Thread(() -> {
                String code = DecompileEngine.decompile(Paths.get(finalClassPath));
                String methodName = res.getMethodName();
                if (methodName.equals("<init>")) {
                    String[] c = res.getClassName().split("/");
                    methodName = c[c.length - 1];
                }
                int paramNum = Type.getMethodType(
                        res.getMethodDesc()).getArgumentTypes().length;
                int pos = Finder.find(code, methodName, paramNum);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(pos + 1);
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread() {
                @Override
                public void run() {
                    CoreHelper.refreshAllMethods(className);
                    CoreHelper.refreshCallers(className, res.getMethodName(), res.getMethodDesc());
                    CoreHelper.refreshCallee(className, res.getMethodName(), res.getMethodDesc());
                    CoreHelper.refreshHistory(className, res.getMethodName(), res.getMethodDesc());
                    CoreHelper.refreshImpls(className, res.getMethodName(), res.getMethodDesc());
                    CoreHelper.refreshSuperImpls(className, res.getMethodName(), res.getMethodDesc());
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

            MainForm.setPrevState(MainForm.getCurState());
            MainForm.setCurState(newState);
            MainForm.setNextState(null);
        }
    }
}
