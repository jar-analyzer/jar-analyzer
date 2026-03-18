/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import com.intellij.uiDesigner.core.GridConstraints;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.OpcodeForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;

public class SyntaxAreaHelper {
    private static final Logger logger = LogManager.getLogger();
    private static int currentIndex = 0;
    private static ArrayList<Integer> searchResults = null;
    private static CodeTabPanel codeTabPanel = null;

    public static void buildJava(JPanel codePanel) {
        // 创建多标签页代码编辑器面板
        codeTabPanel = new CodeTabPanel();
        codePanel.add(codeTabPanel, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        // MainForm.codeArea 初始指向欢迎 Tab 的编辑器
        RSyntaxTextArea activeArea = codeTabPanel.getActiveCodeArea();
        if (activeArea != null) {
            MainForm.setCodeArea(activeArea);
        }

        MainForm.setCodeTabPanel(codeTabPanel);
    }

    /**
     * 获取 CodeTabPanel 实例
     */
    public static CodeTabPanel getCodeTabPanel() {
        return codeTabPanel;
    }

    public static int findWordStart(String text, int position) {
        while (position > 0 && Character.isLetterOrDigit(text.charAt(position - 1))) {
            position--;
        }
        return position;
    }

    public static int findWordEnd(String text, int position) {
        while (position < text.length() && Character.isLetterOrDigit(text.charAt(position))) {
            position++;
        }
        return position;
    }

    public static int addSearchAction(String text) {
        searchResults = new ArrayList<>();
        currentIndex = 0;
        RSyntaxTextArea currentArea = (RSyntaxTextArea) MainForm.getCodeArea();
        if (currentArea == null) return 0;
        String content = currentArea.getText();

        int index = content.indexOf(text);
        while (index >= 0) {
            searchResults.add(index);
            index = content.indexOf(text, index + 1);
        }
        currentIndex = 0;
        return searchResults.size();
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void navigate(String text, boolean forward) {
        if (searchResults == null || MainForm.getCodeArea() == null) {
            return;
        }
        if (searchResults.isEmpty()) {
            return;
        }
        if (forward) {
            currentIndex = (currentIndex + 1) % searchResults.size();
        } else {
            currentIndex = (currentIndex - 1 + searchResults.size()) % searchResults.size();
        }
        highlightResult(text);
    }

    private static void highlightResult(String text) {
        if (searchResults.isEmpty()) return;
        RSyntaxTextArea currentArea = (RSyntaxTextArea) MainForm.getCodeArea();
        if (currentArea == null) return;
        int index = searchResults.get(currentIndex);
        try {
            currentArea.setCaretPosition(index);
            Highlighter highlighter = currentArea.getHighlighter();
            Highlighter.HighlightPainter painter =
                    new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
            highlighter.removeAllHighlights();
            highlighter.addHighlight(index, index + text.length(), painter);
        } catch (BadLocationException ex) {
            logger.error("bad location: {}", ex.toString());
        }
    }

    public static void buildJavaOpcode(JPanel codePanel) {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(false);

        textArea.setFont(textArea.getFont().deriveFont(MainForm.FONT_SIZE));

        RTextScrollPane sp = new RTextScrollPane(textArea);
        codePanel.add(sp, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        OpcodeForm.setCodeArea(textArea);
    }

    public static JTextArea buildSQL(JPanel codePanel) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(10, 80);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);

        textArea.setFont(textArea.getFont().deriveFont(MainForm.FONT_SIZE));

        RTextScrollPane sp = new RTextScrollPane(textArea);
        codePanel.add(sp, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        return textArea;
    }
}
