/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.gui.util.SyntaxAreaHelper;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchForm {

    private static JFrame frame;
    private static JTextField searchText;
    private static JLabel resultLabel;
    // 模式状态
    private static boolean caseSensitive = false;
    private static boolean regexMode = false;
    // 模式切换按钮
    private static JToggleButton caseBtn;
    private static JToggleButton regexBtn;

    private static String searchTextGlobal = null;
    private static int total = 0;

    public static void start() {
        if (frame != null && frame.isShowing()) {
            frame.toFront();
            searchText.requestFocus();
            return;
        }

        frame = new JFrame("Search");
        frame.setUndecorated(false);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(4, 0));
        root.setBorder(new EmptyBorder(5, 6, 5, 6));

        // ---- 输入框 ----
        searchText = new JTextField(22);
        searchText.putClientProperty("JTextField.placeholderText", "Search...");

        // ---- 模式按钮：Aa（大小写） / .* （正则） ----
        caseBtn = makeToggleBtn("Aa", "Case sensitive");
        regexBtn = makeToggleBtn(".*", "Regular expression");

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        modePanel.setOpaque(false);
        modePanel.add(caseBtn);
        modePanel.add(regexBtn);

        JPanel inputRow = new JPanel(new BorderLayout(3, 0));
        inputRow.setOpaque(false);
        inputRow.add(searchText, BorderLayout.CENTER);
        inputRow.add(modePanel, BorderLayout.EAST);

        // ---- 导航按钮 + 计数 ----
        JButton prevBtn = new JButton("▲");
        JButton nextBtn = new JButton("▼");
        prevBtn.setFont(prevBtn.getFont().deriveFont(10f));
        nextBtn.setFont(nextBtn.getFont().deriveFont(10f));
        prevBtn.setMargin(new Insets(1, 4, 1, 4));
        nextBtn.setMargin(new Insets(1, 4, 1, 4));

        resultLabel = new JLabel("0/0");
        resultLabel.setPreferredSize(new Dimension(58, 20));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(closeBtn.getFont().deriveFont(10f));
        closeBtn.setMargin(new Insets(1, 4, 1, 4));
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(new Color(150, 150, 150));
            }
        });

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        navPanel.setOpaque(false);
        navPanel.add(prevBtn);
        navPanel.add(nextBtn);
        navPanel.add(resultLabel);
        navPanel.add(closeBtn);

        root.add(inputRow, BorderLayout.CENTER);
        root.add(navPanel, BorderLayout.EAST);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
        searchText.requestFocus();

        // ---- 事件 ----
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        };
        searchText.getDocument().addDocumentListener(docListener);

        searchText.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) getPrev();
                    else getNext();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    frame.dispose();
                }
            }
        });

        caseBtn.addActionListener(e -> {
            caseSensitive = caseBtn.isSelected();
            refresh();
        });
        regexBtn.addActionListener(e -> {
            regexMode = regexBtn.isSelected();
            refresh();
        });

        prevBtn.addActionListener(e -> getPrev());
        nextBtn.addActionListener(e -> getNext());
        closeBtn.addActionListener(e -> frame.dispose());
    }

    private static JToggleButton makeToggleBtn(String text, String tooltip) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 11f));
        btn.setMargin(new Insets(1, 4, 1, 4));
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        return btn;
    }

    private static void refresh() {
        String text = searchText.getText();
        if (StringUtil.isNull(text)) {
            resultLabel.setText("0/0");
            total = 0;
            searchTextGlobal = null;
            return;
        }
        searchTextGlobal = text;
        total = SyntaxAreaHelper.addSearchAction(text, caseSensitive, regexMode);
        if (total == 0) {
            resultLabel.setText("0/0");
            resultLabel.setForeground(new Color(200, 80, 80));
        } else {
            resultLabel.setForeground(UIManager.getColor("Label.foreground"));
            int cur = SyntaxAreaHelper.getCurrentIndex();
            resultLabel.setText(String.format("%d/%d", cur + 1, total));
        }
    }

    private static void getNext() {
        if (searchTextGlobal == null || total == 0) return;
        SyntaxAreaHelper.navigate(searchTextGlobal, true, caseSensitive, regexMode);
        int cur = SyntaxAreaHelper.getCurrentIndex();
        resultLabel.setText(String.format("%d/%d", cur + 1, total));
    }

    private static void getPrev() {
        if (searchTextGlobal == null || total == 0) return;
        SyntaxAreaHelper.navigate(searchTextGlobal, false, caseSensitive, regexMode);
        int cur = SyntaxAreaHelper.getCurrentIndex();
        resultLabel.setText(String.format("%d/%d", cur + 1, total));
    }
}
