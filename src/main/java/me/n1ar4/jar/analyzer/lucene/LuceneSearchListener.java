/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
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
import java.util.concurrent.locks.ReentrantLock;

public class LuceneSearchListener implements DocumentListener {
    private final JTextArea textField;
    // 给 resultModel 使用的锁
    // 而不是给缓存 缓存本身是 synchronized 的
    private final ReentrantLock lock;
    private final DefaultListModel<LuceneSearchResult> resultModel;
    private final LuceneSearchCache searchCache;

    private void doSearch(String text) {
        resultModel.clear();
        if (text == null || text.isEmpty()) {
            return;
        }

        new Thread(() -> {
            List<LuceneSearchResult> results;

            if (searchCache.containsKey(text)) {
                results = searchCache.get(text);
            } else {
                results = LuceneSearchWrapper.searchFileName(text);
                if (!LuceneSearchForm.useNoLucene()) {
                    results.addAll(LuceneSearchWrapper.searchLucene(text));
                }
                searchCache.put(text, results);
            }

            lock.lock();
            try {
                for (LuceneSearchResult result : results) {
                    if (!result.getFileName().endsWith(".class")) {
                        continue;
                    }
                    resultModel.addElement(result);
                }
            } finally {
                lock.unlock();
            }
        }).start();
    }

    public LuceneSearchListener(JTextArea text, JList<LuceneSearchResult> res) {
        this.textField = text;
        this.lock = new ReentrantLock();
        this.resultModel = new DefaultListModel<>();
        this.searchCache = new LuceneSearchCache();
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