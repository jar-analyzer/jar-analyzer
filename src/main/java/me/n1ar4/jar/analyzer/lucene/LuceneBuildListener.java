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

import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LuceneBuildListener implements ActionListener {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void actionPerformed(ActionEvent e) {
        JDialog dialog = ProcessDialog.createProgressDialog(LuceneSearchForm.getInstance().getRootPanel());
        new Thread(() -> dialog.setVisible(true)).start();
        new Thread(() -> {
            try {
                boolean ok = IndexPluginsSupport.initIndex();
                if (!ok) {
                    JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                            "create lucene index error");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(LuceneSearchForm.getInstance().getRootPanel(),
                            "create lucene index finish");
                    dialog.dispose();
                }
            } catch (Exception ex) {
                logger.error("lucene build error: {}", ex.toString());
                dialog.dispose();
            }
        }).start();
    }
}
