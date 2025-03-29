/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
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
                if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(searchKey)) {
                    int idx;
                    if(LuceneSearchForm.useCaseSensitive()){
                        idx = code.indexOf(searchKey);
                    }else{
                        idx = code.toLowerCase().indexOf(searchKey.toLowerCase());
                    }
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
