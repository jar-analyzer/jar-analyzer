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

package me.n1ar4.jar.analyzer.lucene;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;

public class LuceneMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent evt) {
        JList<?> list = (JList<?>) evt.getSource();
        if (evt.getClickCount() == 2) {
            int index = list.locationToIndex(evt.getPoint());
            LuceneSearchResult res = null;
            try {
                res = (LuceneSearchResult) list.getModel().getElementAt(index);
            } catch (Exception ignored) {
            }
            if (res == null) {
                return;
            }

            String content;
            if (res.getType() == LuceneSearchResult.TYPE_CONTENT) {
                content = res.getContentStr();
            } else {
                content = null;
            }
            String searchKey = res.getSearchKey().trim().replace("\n", "\r\n");

            String finalClassPath = res.getAbsPathStr();
            String suffix = finalClassPath.split(Const.tempDir)[1];
            int i = suffix.indexOf("classes");
            if (suffix.contains("BOOT-INF") || suffix.contains("WEB-INF")) {
                suffix = suffix.substring(i + 8, suffix.length() - 6);
            } else {
                suffix = suffix.substring(1, suffix.length() - 6);
            }
            String className = suffix.replace("\\", "/");

            new Thread(() -> {
                String code = DecompileEngine.decompile(Paths.get(finalClassPath));

                // SET FILE TREE HIGHLIGHT
                SearchInputListener.getFileTree().searchPathTarget(className);

                MainForm.getCodeArea().setText(code);
                MainForm.getCodeArea().setCaretPosition(0);

                // 对于 Content 部分搜索的高亮展示
                if (code != null && content != null) {
                    int idx = code.indexOf(searchKey);
                    if (idx != -1) {
                        MainForm.getCodeArea().setSelectionStart(idx);
                        MainForm.getCodeArea().setSelectionEnd(idx + searchKey.length());
                    }
                }
            }).start();

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread(() -> {
                CoreHelper.refreshAllMethods(className);
                dialog.dispose();
            }).start();

            CoreHelper.refreshSpringM(className);

            MainForm.getInstance().getCurClassText().setText(className);
            String jarName = MainForm.getEngine().getJarByClass(className);
            MainForm.getInstance().getCurJarText().setText(jarName);
            MainForm.getInstance().getCurMethodText().setText(null);
            MainForm.setCurMethod(null);

            MainForm.getInstance().getMethodImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getSuperImplList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCalleeList().setModel(new DefaultListModel<>());
            MainForm.getInstance().getCallerList().setModel(new DefaultListModel<>());
        }
    }
}
