package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ControllerMouseAdapter extends MouseAdapter {
    @SuppressWarnings("all")
    public void mouseClicked(MouseEvent evt) {
        JList<?> list = (JList<?>) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            ClassResult res = (ClassResult) list.getModel().getElementAt(index);

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

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(0);
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

            CoreHelper.refreshSpringM(className);

            MainForm.getInstance().getCurClassText().setText(className);
            String jarName = res.getJarName();
            if (StringUtil.isNull(jarName)) {
                jarName = MainForm.getEngine().getJarByClass(className);
            }
            MainForm.getInstance().getCurJarText().setText(jarName);
            MainForm.getInstance().getCurMethodText().setText(null);
            MainForm.setCurMethod(null);

            // 重置所有内容
            MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
        }
    }
}
