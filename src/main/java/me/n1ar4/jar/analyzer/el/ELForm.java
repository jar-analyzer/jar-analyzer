/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import me.n1ar4.jar.analyzer.el.ui.ELPanel;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.exporter.SearchResultCsvExporter;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Set;

public class ELForm {
    private static final Logger logger = LogManager.getLogger();
    public JPanel elPanel;
    private final ELPanel panel;
    private static ELForm elInstance;

    public static void setVal(int val) {
        SwingUtilities.invokeLater(() -> elInstance.panel.getProgressBar().setValue(val));
    }

    public static String removeComments(String code) {
        return code.replaceAll("(?m)^\\s*//.*$", "");
    }

    public ELForm() {
        panel = new ELPanel();
        elPanel = panel;

        JTextArea jTextArea = panel.getCodeArea();
        JButton checkButton = panel.getCheckButton();
        JButton searchButton = panel.getSearchButton();
        JButton stopBtn = panel.getStopBtn();
        JButton exportBtn = panel.getExportBtn();
        JLabel msgLabel = panel.getMsgLabel();

        checkButton.addActionListener(e -> {
            try {
                ExpressionParser parser = new SpelExpressionParser();
                String spel = jTextArea.getText();
                spel = removeComments(spel);
                parser.parseExpression(spel);
                JOptionPane.showMessageDialog(jTextArea, "解析通过，正确的表达式");
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(jTextArea, "解析异常，错误的表达式");
            }
        });

        searchButton.addActionListener(e -> new Thread(() -> {
            logger.info("start el process");

            if (MainForm.getEngine() == null) {
                logger.warn("engine is null");
                ELForm.setVal(0);
                return;
            }
            if (!MainForm.getEngine().isEnabled()) {
                logger.warn("engine is not enabled");
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "engine is not enabled");
                ELForm.setVal(0);
                return;
            }

            searchButton.setEnabled(false);
            ELForm.setVal(0);

            ExpressionParser parser = new SpelExpressionParser();
            String spel = jTextArea.getText();
            spel = removeComments(spel);

            Object value;
            try {
                MethodEL m = new MethodEL();
                Expression exp = parser.parseExpression(spel);
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                ctx.setVariable("method", m);
                value = exp.getValue(ctx);
            } catch (Exception ex) {
                ELForm.setVal(100);
                searchButton.setEnabled(true);
                JOptionPane.showMessageDialog(jTextArea, "语法错误");
                return;
            }

            ELForm.setVal(3);
            logger.info("parse el success");

            if (value instanceof MethodEL) {
                ELSearchEngine engine = new ELSearchEngine(value, msgLabel, searchButton, stopBtn, jTextArea);
                engine.run();
                return;
            } else {
                JOptionPane.showMessageDialog(jTextArea, "错误的表达式");
            }
            ELForm.setVal(100);
            searchButton.setEnabled(true);
            logger.info("el process finish");
        }).start());

        elInstance = this;

        exportBtn.addActionListener(e -> {
            if (MainForm.getInstance() == null) {
                return;
            }
            JList<MethodResult> searchList = MainForm.getInstance().getSearchList();
            if (searchList == null || searchList.getModel().getSize() == 0) {
                JOptionPane.showMessageDialog(elPanel, "当前没有搜索结果可以导出");
                return;
            }
            java.util.List<MethodResult> resultList = new ArrayList<>();
            for (int i = 0; i < searchList.getModel().getSize(); i++) {
                resultList.add(searchList.getModel().getElementAt(i));
            }
            SearchResultCsvExporter exporter = new SearchResultCsvExporter(resultList);
            boolean success = exporter.doExport();
            if (success) {
                JOptionPane.showMessageDialog(elPanel,
                        "导出成功: " + exporter.getFileName());
            } else {
                JOptionPane.showMessageDialog(elPanel, "导出失败");
            }
        });
    }
}
