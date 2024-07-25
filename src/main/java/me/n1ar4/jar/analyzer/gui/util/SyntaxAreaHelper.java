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
    private static RSyntaxTextArea codeArea = null;
    private static int currentIndex = 0;
    private static ArrayList<Integer> searchResults = null;

    public static void buildJava(JPanel codePanel) {
        RSyntaxTextArea rArea = new RSyntaxTextArea(300, 300);
        // 不要使用其他字体
        // 默认字体支持中文 其他的不一定

        rArea.addCaretListener(e -> {
            String selectedText = rArea.getSelectedText();
            if (selectedText == null || selectedText.isEmpty()) {
                Highlighter highlighter = rArea.getHighlighter();
                highlighter.removeAllHighlights();
                return;
            }
            Highlighter highlighter = rArea.getHighlighter();
            highlighter.removeAllHighlights();

            String text = rArea.getText();
            int index = 0;

            while ((index = text.indexOf(selectedText, index)) >= 0) {
                try {
                    highlighter.addHighlight(index, index + selectedText.length(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                    index += selectedText.length();
                } catch (BadLocationException ignored) {
                }
            }
        });

        codeArea = rArea;
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(codeArea);
        codePanel.add(sp, new GridConstraints());
        MainForm.setCodeArea(codeArea);
    }

    public static int addSearchAction(String text) {
        searchResults = new ArrayList<>();
        currentIndex = 0;
        String content = codeArea.getText();

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
        if (searchResults == null || codeArea == null) {
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
        int index = searchResults.get(currentIndex);
        try {
            codeArea.setCaretPosition(index);
            Highlighter highlighter = codeArea.getHighlighter();
            Highlighter.HighlightPainter painter =
                    new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
            highlighter.removeAllHighlights();
            highlighter.addHighlight(index, index + text.length(), painter);
        } catch (BadLocationException ex) {
            logger.error("bad location: {}", ex.toString());
        }
    }

    public static void buildJavaOpcode(JPanel codePanel) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(30, 100);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(false);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        codePanel.add(sp, new GridConstraints());
        OpcodeForm.setCodeArea(textArea);
    }

    public static JTextArea buildSQL(JPanel codePanel) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(10, 80);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        codePanel.add(sp, new GridConstraints());
        return textArea;
    }
}
