/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class ELAutoCompleteProvider {

    private static final String[] ROOT_COMPLETIONS = {
            "#method",
    };

    private static final String[] METHOD_CHAIN_COMPLETIONS = {
            ".nameContains(\"\")",
            ".nameNotContains(\"\")",
            ".startWith(\"\")",
            ".endWith(\"\")",
            ".nameRegex(\"\")",
            ".classNameContains(\"\")",
            ".classNameNotContains(\"\")",
            ".classNameRegex(\"\")",
            ".returnType(\"\")",
            ".paramTypeMap(0,\"\")",
            ".paramsNum(1)",
            ".isStatic(false)",
            ".isPublic(true)",
            ".isSubClassOf(\"\")",
            ".isSuperClassOf(\"\")",
            ".hasClassAnno(\"\")",
            ".hasAnno(\"\")",
            ".excludeAnno(\"\")",
            ".hasField(\"\")",
            ".containsInvoke(\"\",\"\")",
            ".excludeInvoke(\"\",\"\")",
    };

    private static final Map<String, String> COMPLETION_DOCS = new LinkedHashMap<>();

    static {
        COMPLETION_DOCS.put("#method", "根变量 - SpEL 方法搜索入口");
        COMPLETION_DOCS.put(".nameContains(\"\")", "方法名包含指定字符串");
        COMPLETION_DOCS.put(".nameNotContains(\"\")", "方法名不包含指定字符串");
        COMPLETION_DOCS.put(".startWith(\"\")", "方法名以指定字符串开头");
        COMPLETION_DOCS.put(".endWith(\"\")", "方法名以指定字符串结尾");
        COMPLETION_DOCS.put(".nameRegex(\"\")", "方法名正则匹配");
        COMPLETION_DOCS.put(".classNameContains(\"\")", "类名包含指定字符串");
        COMPLETION_DOCS.put(".classNameNotContains(\"\")", "类名不包含指定字符串");
        COMPLETION_DOCS.put(".classNameRegex(\"\")", "类名正则匹配");
        COMPLETION_DOCS.put(".returnType(\"\")", "返回类型 (全限定名)");
        COMPLETION_DOCS.put(".paramTypeMap(0,\"\")", "指定位置参数类型");
        COMPLETION_DOCS.put(".paramsNum(1)", "参数个数");
        COMPLETION_DOCS.put(".isStatic(false)", "是否静态方法");
        COMPLETION_DOCS.put(".isPublic(true)", "是否 public 方法");
        COMPLETION_DOCS.put(".isSubClassOf(\"\")", "所在类是指定类的子类");
        COMPLETION_DOCS.put(".isSuperClassOf(\"\")", "所在类是指定类的父类");
        COMPLETION_DOCS.put(".hasClassAnno(\"\")", "类上有指定注解");
        COMPLETION_DOCS.put(".hasAnno(\"\")", "方法上有指定注解");
        COMPLETION_DOCS.put(".excludeAnno(\"\")", "排除带指定注解的方法");
        COMPLETION_DOCS.put(".hasField(\"\")", "类中包含指定字段");
        COMPLETION_DOCS.put(".containsInvoke(\"\",\"\")", "方法体内调用了指定方法");
        COMPLETION_DOCS.put(".excludeInvoke(\"\",\"\")", "排除调用了指定方法的方法");
    }

    private final JTextComponent textComponent;
    private JWindow popupWindow;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private boolean inserting = false;

    public ELAutoCompleteProvider(JTextComponent textComponent) {
        this.textComponent = textComponent;
        initPopup();
        installListeners();
    }

    private void initPopup() {
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(textComponent.getFont());
        suggestionList.setVisibleRowCount(10);
        suggestionList.setFocusable(false);

        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                String text = (String) value;
                String doc = COMPLETION_DOCS.get(text);
                if (doc != null) {
                    label.setText("<html><b>" + escapeHtml(text) + "</b> <font color='gray'>— "
                            + escapeHtml(doc) + "</font></html>");
                }
                return label;
            }

            private String escapeHtml(String s) {
                return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    insertSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(420, 250));
        scrollPane.setFocusable(false);

        popupWindow = new JWindow();
        popupWindow.setType(Window.Type.POPUP);
        popupWindow.setFocusableWindowState(false);
        popupWindow.setAlwaysOnTop(true);
        popupWindow.getContentPane().add(scrollPane);
        popupWindow.pack();
    }

    private void hidePopup() {
        if (popupWindow != null && popupWindow.isVisible()) {
            popupWindow.setVisible(false);
        }
    }

    private boolean isPopupVisible() {
        return popupWindow != null && popupWindow.isVisible();
    }

    private void installListeners() {
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPopupVisible()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            moveSelection(1);
                            e.consume();
                            break;
                        case KeyEvent.VK_UP:
                            moveSelection(-1);
                            e.consume();
                            break;
                        case KeyEvent.VK_ENTER:
                        case KeyEvent.VK_TAB:
                            if (suggestionList.getSelectedValue() != null) {
                                insertSelected();
                                e.consume();
                            }
                            break;
                        case KeyEvent.VK_ESCAPE:
                            hidePopup();
                            e.consume();
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_UP
                        || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB
                        || code == KeyEvent.VK_ESCAPE
                        || code == KeyEvent.VK_SHIFT
                        || code == KeyEvent.VK_CONTROL
                        || code == KeyEvent.VK_ALT
                        || code == KeyEvent.VK_LEFT
                        || code == KeyEvent.VK_RIGHT) {
                    return;
                }
                SwingUtilities.invokeLater(() -> updateSuggestions());
            }
        });

        textComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> hidePopup());
            }
        });
    }

    private void moveSelection(int delta) {
        int idx = suggestionList.getSelectedIndex() + delta;
        if (idx >= 0 && idx < listModel.getSize()) {
            suggestionList.setSelectedIndex(idx);
            suggestionList.ensureIndexIsVisible(idx);
        }
    }

    private void insertSelected() {
        String selected = suggestionList.getSelectedValue();
        if (selected == null) {
            return;
        }
        hidePopup();
        inserting = true;
        try {
            int caretPos = textComponent.getCaretPosition();
            String text = textComponent.getText();

            if (selected.startsWith(".")) {
                int tokenStart = findTokenStart(text, caretPos);
                textComponent.getDocument().remove(tokenStart, caretPos - tokenStart);
                textComponent.getDocument().insertString(tokenStart, selected, null);

                int newCaret = tokenStart + selected.length();
                int quotePos = selected.indexOf('"');
                if (quotePos >= 0) {
                    newCaret = tokenStart + quotePos + 1;
                }
                textComponent.setCaretPosition(newCaret);
            } else if (selected.startsWith("#")) {
                int tokenStart = findHashStart(text, caretPos);
                textComponent.getDocument().remove(tokenStart, caretPos - tokenStart);
                textComponent.getDocument().insertString(tokenStart, selected, null);
            }
        } catch (BadLocationException ex) {
            // ignore
        } finally {
            inserting = false;
        }
        textComponent.requestFocusInWindow();
    }

    private void updateSuggestions() {
        if (inserting) {
            return;
        }
        try {
            int caretPos = textComponent.getCaretPosition();
            String text = textComponent.getText();

            List<String> candidates = new ArrayList<>();

            char lastChar = caretPos > 0 ? text.charAt(caretPos - 1) : '\0';

            if (lastChar == '.') {
                candidates.addAll(Arrays.asList(METHOD_CHAIN_COMPLETIONS));
            } else if (lastChar == '#' || (caretPos >= 2 && text.charAt(caretPos - 2) == '#')) {
                int hashStart = findHashStart(text, caretPos);
                String prefix = text.substring(hashStart, caretPos);
                for (String c : ROOT_COMPLETIONS) {
                    if (c.toLowerCase().startsWith(prefix.toLowerCase())) {
                        candidates.add(c);
                    }
                }
            } else {
                int tokenStart = findTokenStart(text, caretPos);
                String prefix = text.substring(tokenStart, caretPos);
                if (prefix.startsWith(".") && prefix.length() > 1) {
                    String lower = prefix.toLowerCase();
                    for (String c : METHOD_CHAIN_COMPLETIONS) {
                        if (c.toLowerCase().startsWith(lower)) {
                            candidates.add(c);
                        }
                    }
                }
            }

            if (candidates.isEmpty()) {
                hidePopup();
                return;
            }

            listModel.clear();
            for (String c : candidates) {
                listModel.addElement(c);
            }
            suggestionList.setSelectedIndex(0);

            Rectangle rect = textComponent.modelToView(caretPos);
            if (rect != null) {
                Point loc = textComponent.getLocationOnScreen();
                int x = loc.x + rect.x;
                int y = loc.y + rect.y + rect.height + 2;

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int popH = popupWindow.getPreferredSize().height;
                int popW = popupWindow.getPreferredSize().width;
                if (y + popH > screenSize.height) {
                    y = loc.y + rect.y - popH - 2;
                }
                if (x + popW > screenSize.width) {
                    x = screenSize.width - popW;
                }

                popupWindow.setLocation(x, y);
                popupWindow.setVisible(true);
            }
        } catch (BadLocationException | IllegalComponentStateException ex) {
            hidePopup();
        }
    }

    private int findTokenStart(String text, int pos) {
        while (pos > 0) {
            char c = text.charAt(pos - 1);
            if (c == '.' || Character.isLetterOrDigit(c) || c == '_') {
                pos--;
                if (c == '.') {
                    break;
                }
            } else {
                break;
            }
        }
        return pos;
    }

    private int findHashStart(String text, int pos) {
        while (pos > 0) {
            char c = text.charAt(pos - 1);
            if (c == '#' || Character.isLetterOrDigit(c) || c == '_') {
                pos--;
                if (c == '#') {
                    break;
                }
            } else {
                break;
            }
        }
        return pos;
    }
}
