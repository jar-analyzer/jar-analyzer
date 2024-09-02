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
