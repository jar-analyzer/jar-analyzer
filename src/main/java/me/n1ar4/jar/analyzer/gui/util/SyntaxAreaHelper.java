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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // 记录每次匹配的结束位置（用于高亮正确长度）
    private static ArrayList<Integer> searchResultEnds = null;

    public static int addSearchAction(String text) {
        return addSearchAction(text, false, false);
    }

    /**
     * @param text          搜索文本或正则表达式
     * @param caseSensitive 是否大小写敏感
     * @param regex         是否正则模式
     */
    public static int addSearchAction(String text, boolean caseSensitive, boolean regex) {
        searchResults = new ArrayList<>();
        searchResultEnds = new ArrayList<>();
        currentIndex = 0;
        RSyntaxTextArea currentArea = (RSyntaxTextArea) MainForm.getCodeArea();
        if (currentArea == null) return 0;
        String content = currentArea.getText();
        try {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            String pattern = regex ? text : Pattern.quote(text);
            Matcher matcher = Pattern.compile(pattern, flags).matcher(content);
            while (matcher.find()) {
                searchResults.add(matcher.start());
                searchResultEnds.add(matcher.end());
            }
        } catch (Exception ignored) {
            // 正则非法时不报错，返回 0 结果
        }
        currentIndex = 0;
        return searchResults.size();
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void navigate(String text, boolean forward) {
        navigate(text, forward, false, false);
    }

    public static void navigate(String text, boolean forward, boolean caseSensitive, boolean regex) {
        if (searchResults == null || MainForm.getCodeArea() == null) return;
        if (searchResults.isEmpty()) return;
        if (forward) {
            currentIndex = (currentIndex + 1) % searchResults.size();
        } else {
            currentIndex = (currentIndex - 1 + searchResults.size()) % searchResults.size();
        }
        highlightResult();
    }

    private static void highlightResult() {
        if (searchResults == null || searchResults.isEmpty()) return;
        RSyntaxTextArea currentArea = (RSyntaxTextArea) MainForm.getCodeArea();
        if (currentArea == null) return;
        int start = searchResults.get(currentIndex);
        int end = (searchResultEnds != null && currentIndex < searchResultEnds.size())
                ? searchResultEnds.get(currentIndex) : start;
        try {
            currentArea.setCaretPosition(start);
            Highlighter highlighter = currentArea.getHighlighter();
            highlighter.removeAllHighlights();
            highlighter.addHighlight(start, end,
                    new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
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
