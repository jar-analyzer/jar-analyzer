/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LuceneBuildListener implements ActionListener {
    public static volatile boolean usePass = false;
    public static volatile boolean once = false;

    @Override
    public void actionPerformed(ActionEvent e) {
        JDialog dialog = ProcessDialog.createProgressDialog(LuceneSearchForm.getInstance().getRootPanel());
        new Thread(() -> dialog.setVisible(true)).start();
        new Thread(() -> {
            if (usePass) {
                dialog.dispose();
                JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                        "请在启动时构建（无法在开启被动构建后主动构建）");
                return;
            }
            // 二次加载会出问题
            if (once) {
                dialog.dispose();
                JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                        "一次运行只允许主动加载一次");
                return;
            }
            try {
                boolean ok = IndexPluginsSupport.initIndex();
                if (!ok) {
                    JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                            "create lucene index error");
                    dialog.dispose();
                } else {
                    once = true;

                    // FIX 2024/11/19
                    // 主动构建结束后不应该再次尝试被动方式
                    IndexPluginsSupport.setUseActive(true);

                    JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                            "create lucene index finish");
                    dialog.dispose();
                }
            } catch (Exception ex) {
                dialog.dispose();
                JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                        "请在启动时构建（无法在开启被动构建后主动构建）");
            }
        }).start();
    }
}
