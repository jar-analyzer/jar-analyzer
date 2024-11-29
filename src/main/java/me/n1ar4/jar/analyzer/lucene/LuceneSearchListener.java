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

import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;

public class LuceneSearchListener implements DocumentListener {
    private final JTextArea textField;
    private final DefaultListModel<LuceneSearchResult> resultModel;

    private void doSearch(String text) {
        resultModel.clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        new Thread(() -> {
            // 直接的类名优先
            List<LuceneSearchResult> results = LuceneSearchWrapper.searchFileName(text);
            // 其次是文件内容
            if (!LuceneSearchForm.useNoLucene()) {
                results.addAll(LuceneSearchWrapper.searchLucene(text));
            }
            for (LuceneSearchResult result : results) {
                resultModel.addElement(result);
            }
        }).start();
    }

    public LuceneSearchListener(JTextArea text, JList<LuceneSearchResult> res) {
        this.textField = text;
        this.resultModel = new DefaultListModel<>();
        res.setModel(resultModel);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        doSearch(textField.getText());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        doSearch(textField.getText());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        doSearch(textField.getText());
    }
}
