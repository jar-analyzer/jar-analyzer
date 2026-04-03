/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.sqlite.ui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class SQLAutoCompleteProvider {

    private static final String[] SQL_KEYWORDS = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
            "CREATE", "TABLE", "DROP", "ALTER", "ADD", "COLUMN",
            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON",
            "GROUP", "BY", "ORDER", "ASC", "DESC",
            "HAVING", "LIMIT", "OFFSET", "DISTINCT", "AS",
            "COUNT", "SUM", "AVG", "MIN", "MAX",
            "LIKE", "IN", "BETWEEN", "IS", "NULL",
            "EXISTS", "UNION", "ALL", "CASE", "WHEN", "THEN", "ELSE", "END",
            "PRAGMA", "table_info", "sqlite_master",
            "INTEGER", "TEXT", "REAL", "BLOB", "PRIMARY", "KEY",
            "AUTOINCREMENT", "NOT NULL", "DEFAULT", "CONSTRAINT",
            "IF", "INDEX",
    };

    private static final String[] TABLE_NAMES = {
            "jar_table", "class_table", "class_file_table",
            "member_table", "method_table", "anno_table",
            "interface_table", "method_call_table", "method_impl_table",
            "string_table", "spring_controller_table", "spring_method_table",
            "spring_interceptor_table", "java_web_table",
            "dfs_result_table", "dfs_result_list_table",
            "note_favorite_table", "note_history_table",
    };

    private static final Map<String, String[]> TABLE_COLUMNS = new LinkedHashMap<>();

    static {
        TABLE_COLUMNS.put("jar_table", new String[]{
                "jid", "jar_name", "jar_abs_path"
        });
        TABLE_COLUMNS.put("class_table", new String[]{
                "cid", "jar_id", "jar_name", "version", "access",
                "class_name", "super_class_name", "is_interface"
        });
        TABLE_COLUMNS.put("class_file_table", new String[]{
                "cf_id", "class_name", "path_str", "jar_name", "jar_id"
        });
        TABLE_COLUMNS.put("member_table", new String[]{
                "mid", "member_name", "modifiers", "value", "method_desc",
                "method_signature", "type_class_name", "class_name", "jar_id"
        });
        TABLE_COLUMNS.put("method_table", new String[]{
                "method_id", "method_name", "method_desc", "is_static",
                "class_name", "access", "line_number", "jar_id"
        });
        TABLE_COLUMNS.put("anno_table", new String[]{
                "anno_id", "anno_name", "method_name", "class_name", "visible", "jar_id"
        });
        TABLE_COLUMNS.put("interface_table", new String[]{
                "iid", "interface_name", "class_name", "jar_id"
        });
        TABLE_COLUMNS.put("method_call_table", new String[]{
                "mc_id", "caller_method_name", "caller_class_name",
                "caller_method_desc", "caller_jar_id",
                "callee_method_name", "callee_method_desc",
                "callee_class_name", "callee_jar_id", "op_code"
        });
        TABLE_COLUMNS.put("method_impl_table", new String[]{
                "impl_id", "class_name", "method_name", "method_desc",
                "impl_class_name", "class_jar_id", "impl_class_jar_id"
        });
        TABLE_COLUMNS.put("string_table", new String[]{
                "sid", "value", "access", "method_desc",
                "method_name", "class_name", "jar_name", "jar_id"
        });
        TABLE_COLUMNS.put("spring_controller_table", new String[]{
                "sc_id", "class_name", "jar_id"
        });
        TABLE_COLUMNS.put("spring_method_table", new String[]{
                "sm_id", "class_name", "method_name",
                "method_desc", "restful_type", "path", "jar_id"
        });
        TABLE_COLUMNS.put("spring_interceptor_table", new String[]{
                "si_id", "class_name", "jar_id"
        });
        TABLE_COLUMNS.put("java_web_table", new String[]{
                "jw_id", "type_name", "class_name", "jar_id"
        });
        TABLE_COLUMNS.put("dfs_result_table", new String[]{
                "dr_id", "source_class_name", "source_method_name",
                "source_method_desc", "sink_class_name", "sink_method_name",
                "sink_method_desc", "dfs_depth", "dfs_mode", "dfs_list_uid"
        });
        TABLE_COLUMNS.put("dfs_result_list_table", new String[]{
                "drl_id", "dfs_list_uid", "dfs_list_index",
                "dfs_class_name", "dfs_method_name", "dfs_method_desc"
        });
        TABLE_COLUMNS.put("note_favorite_table", new String[]{
                "nf_id", "class_name", "method_name", "method_desc"
        });
        TABLE_COLUMNS.put("note_history_table", new String[]{
                "nh_id", "class_name", "method_name", "method_desc"
        });
    }

    private final JTextComponent textComponent;
    private JWindow popupWindow;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private boolean inserting = false;

    public SQLAutoCompleteProvider(JTextComponent textComponent) {
        this.textComponent = textComponent;
        initPopup();
        installListeners();
    }

    private void initPopup() {
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(textComponent.getFont());
        suggestionList.setVisibleRowCount(8);
        suggestionList.setFocusable(false);

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    insertSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
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
            int wordStart = findWordStart(text, caretPos);
            textComponent.getDocument().remove(wordStart, caretPos - wordStart);
            textComponent.getDocument().insertString(wordStart, selected, null);
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
            int wordStart = findWordStart(text, caretPos);
            String prefix = text.substring(wordStart, caretPos);

            if (prefix.length() < 1) {
                hidePopup();
                return;
            }

            String context = detectContext(text, wordStart);
            List<String> candidates = getCandidates(prefix, context, text);

            if (candidates.isEmpty()) {
                hidePopup();
                return;
            }

            if (candidates.size() == 1 && candidates.get(0).equalsIgnoreCase(prefix)) {
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
        } catch (BadLocationException ex) {
            hidePopup();
        } catch (IllegalComponentStateException ex) {
            hidePopup();
        }
    }

    private List<String> getCandidates(String prefix, String context, String fullText) {
        List<String> result = new ArrayList<>();
        String upper = prefix.toUpperCase();
        String lower = prefix.toLowerCase();

        if ("column".equals(context)) {
            String tableName = extractTableName(fullText);
            if (tableName != null) {
                String[] cols = TABLE_COLUMNS.get(tableName);
                if (cols != null) {
                    for (String col : cols) {
                        if (col.toLowerCase().startsWith(lower)) {
                            result.add(col);
                        }
                    }
                }
            }
        }

        if ("table".equals(context) || result.isEmpty()) {
            for (String tn : TABLE_NAMES) {
                if (tn.toLowerCase().startsWith(lower)) {
                    result.add(tn);
                }
            }
        }

        if (result.isEmpty() || "keyword".equals(context)) {
            for (String kw : SQL_KEYWORDS) {
                if (kw.toUpperCase().startsWith(upper)) {
                    result.add(kw);
                }
            }
        }

        Set<String> seen = new LinkedHashSet<>();
        List<String> deduped = new ArrayList<>();
        for (String s : result) {
            if (seen.add(s.toLowerCase())) {
                deduped.add(s);
            }
        }

        if (deduped.size() > 20) {
            return deduped.subList(0, 20);
        }
        return deduped;
    }

    private String detectContext(String text, int wordStart) {
        String before = text.substring(0, wordStart).trim().toUpperCase();
        if (before.endsWith("FROM") || before.endsWith("JOIN")
                || before.endsWith("TABLE") || before.endsWith("INTO")
                || before.endsWith("UPDATE")) {
            return "table";
        }
        if (before.endsWith("SELECT") || before.endsWith("WHERE")
                || before.endsWith("AND") || before.endsWith("OR")
                || before.endsWith("BY") || before.endsWith("SET")
                || before.endsWith("ON") || before.endsWith(",")) {
            return "column";
        }
        return "keyword";
    }

    private String extractTableName(String text) {
        String upper = text.toUpperCase();
        int fromIdx = upper.lastIndexOf("FROM");
        if (fromIdx == -1) {
            fromIdx = upper.lastIndexOf("JOIN");
        }
        if (fromIdx == -1) {
            fromIdx = upper.lastIndexOf("UPDATE");
        }
        if (fromIdx == -1) {
            fromIdx = upper.lastIndexOf("INTO");
        }
        if (fromIdx == -1) {
            return null;
        }

        String after = text.substring(fromIdx).trim();
        String[] parts = after.split("\\s+");
        if (parts.length >= 2) {
            String candidate = parts[1].replaceAll("[;,()]", "").trim();
            for (String tn : TABLE_NAMES) {
                if (tn.equalsIgnoreCase(candidate)) {
                    return tn;
                }
            }
        }
        return null;
    }

    private int findWordStart(String text, int pos) {
        while (pos > 0) {
            char c = text.charAt(pos - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                pos--;
            } else {
                break;
            }
        }
        return pos;
    }
}
