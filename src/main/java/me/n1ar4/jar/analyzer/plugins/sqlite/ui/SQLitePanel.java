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

import me.n1ar4.jar.analyzer.gui.render.ZebraTableCellRenderer;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Map;

public class SQLitePanel extends JPanel {

    private final JTextField dbFileField;
    private final JButton connectButton;
    private final JComboBox<String> tablesBox;
    private final JButton runButton;
    private final JButton clearButton;
    private final RSyntaxTextArea sqlArea;
    private final JTable resultTable;
    private final JTextArea errArea;
    private final JComboBox<String> templateBox;
    private final JLabel statusLabel;
    private final JLabel rowCountLabel;

    public SQLitePanel() {
        setLayout(new BorderLayout(0, 4));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        // ======== 顶部面板 (连接 + 模板) ========
        JPanel topPanel = new JPanel(new BorderLayout(4, 4));

        // 连接行
        JPanel connectPanel = new JPanel(new BorderLayout(4, 0));
        connectPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Database",
                TitledBorder.LEFT, TitledBorder.TOP));

        JPanel connectFields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        connectFields.add(new JLabel("DB File:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        dbFileField = new JTextField();
        dbFileField.setEditable(false);
        dbFileField.setColumns(30);
        connectFields.add(dbFileField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(90, 28));
        connectFields.add(connectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        connectFields.add(new JLabel("Table:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        tablesBox = new JComboBox<>();
        connectFields.add(tablesBox, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(Color.GRAY);
        connectFields.add(statusLabel, gbc);

        connectPanel.add(connectFields, BorderLayout.CENTER);
        topPanel.add(connectPanel, BorderLayout.CENTER);

        // 模板行
        JPanel templatePanel = new JPanel(new BorderLayout(4, 0));
        templatePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Quick Query",
                TitledBorder.LEFT, TitledBorder.TOP));

        templateBox = new JComboBox<>();
        templateBox.addItem("-- 选择查询模板 --");
        for (String name : SQLQueryTemplate.getTemplateNames()) {
            templateBox.addItem(name);
        }
        templateBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                onTemplateSelected();
            }
        });
        templatePanel.add(templateBox, BorderLayout.CENTER);
        topPanel.add(templatePanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // ======== 中间：SQL 编辑器 + 按钮 ========
        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));

        // SQL 编辑区
        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SQL",
                TitledBorder.LEFT, TitledBorder.TOP));

        sqlArea = new RSyntaxTextArea(8, 80);
        sqlArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        sqlArea.setCodeFoldingEnabled(true);
        sqlArea.setAntiAliasingEnabled(true);
        sqlArea.setAutoIndentEnabled(true);
        sqlArea.setTabSize(4);

        RTextScrollPane sqlScroll = new RTextScrollPane(sqlArea);
        sqlScroll.setPreferredSize(new Dimension(-1, 180));
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);

        // 提前初始化 errArea 以便 clear 按钮引用
        errArea = new JTextArea(3, 0);
        errArea.setEditable(false);
        errArea.setForeground(new Color(220, 50, 50));
        errArea.setFont(errArea.getFont().deriveFont(12f));

        // 按钮栏
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            sqlArea.setText("");
            errArea.setText("");
        });
        runButton = new JButton("Run SQL");
        runButton.setPreferredSize(new Dimension(100, 28));
        btnPanel.add(clearButton);
        btnPanel.add(runButton);
        sqlPanel.add(btnPanel, BorderLayout.SOUTH);

        centerPanel.add(sqlPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ======== 底部：结果表 + 错误区 ========
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));

        // 结果表
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Result",
                TitledBorder.LEFT, TitledBorder.TOP));

        resultTable = new JTable();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());
        resultTable.setRowHeight(22);

        JScrollPane resultScroll = new JScrollPane(resultTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultScroll.setPreferredSize(new Dimension(-1, 250));
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        rowCountLabel = new JLabel(" Rows: 0");
        rowCountLabel.setFont(rowCountLabel.getFont().deriveFont(Font.PLAIN, 11f));
        resultPanel.add(rowCountLabel, BorderLayout.SOUTH);

        bottomPanel.add(resultPanel, BorderLayout.CENTER);

        // 错误区
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Error",
                TitledBorder.LEFT, TitledBorder.TOP));

        JScrollPane errorScroll = new JScrollPane(errArea);
        errorScroll.setPreferredSize(new Dimension(-1, 60));
        errorPanel.add(errorScroll, BorderLayout.CENTER);

        bottomPanel.add(errorPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // 安装自动补全
        new SQLAutoCompleteProvider(sqlArea);
    }

    private void onTemplateSelected() {
        int idx = templateBox.getSelectedIndex();
        if (idx <= 0) {
            return;
        }
        String name = (String) templateBox.getSelectedItem();
        String template = SQLQueryTemplate.getTemplate(name);
        if (template == null) {
            return;
        }

        // 如果选择了表名，替换 ${table}
        String selectedTable = (String) tablesBox.getSelectedItem();
        if (selectedTable != null && !selectedTable.isEmpty()) {
            template = template.replace("${table}", selectedTable);
        }

        sqlArea.setText(template);
        sqlArea.requestFocusInWindow();
    }

    // ======== Getter methods ========

    public JTextField getDbFileField() {
        return dbFileField;
    }

    public JButton getConnectButton() {
        return connectButton;
    }

    public JComboBox<String> getTablesBox() {
        return tablesBox;
    }

    public JButton getRunButton() {
        return runButton;
    }

    public RSyntaxTextArea getSqlArea() {
        return sqlArea;
    }

    public JTable getResultTable() {
        return resultTable;
    }

    public JTextArea getErrArea() {
        return errArea;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JLabel getRowCountLabel() {
        return rowCountLabel;
    }

    public void setConnected(boolean connected) {
        if (connected) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(new Color(0, 140, 0));
        } else {
            statusLabel.setText("Disconnected");
            statusLabel.setForeground(Color.GRAY);
        }
    }

    public void updateRowCount(int count) {
        rowCountLabel.setText(" Rows: " + count);
    }

    public void showError(String msg) {
        errArea.setText(msg);
    }

    public void clearError() {
        errArea.setText("");
    }

    public void setResultModel(DefaultTableModel model) {
        resultTable.setModel(model);
        updateRowCount(model.getRowCount());
        // 自动调整列宽
        autoResizeColumns();
    }

    private void autoResizeColumns() {
        for (int col = 0; col < resultTable.getColumnCount(); col++) {
            int maxWidth = 60;
            // 表头宽度
            String headerVal = resultTable.getColumnName(col);
            if (headerVal != null) {
                FontMetrics fm = resultTable.getFontMetrics(resultTable.getFont());
                maxWidth = Math.max(maxWidth, fm.stringWidth(headerVal) + 20);
            }
            // 遍历前 50 行数据
            int rowLimit = Math.min(resultTable.getRowCount(), 50);
            for (int row = 0; row < rowLimit; row++) {
                Object val = resultTable.getValueAt(row, col);
                if (val != null) {
                    FontMetrics fm = resultTable.getFontMetrics(resultTable.getFont());
                    int w = fm.stringWidth(val.toString()) + 16;
                    maxWidth = Math.max(maxWidth, w);
                }
            }
            maxWidth = Math.min(maxWidth, 400);
            resultTable.getColumnModel().getColumn(col).setPreferredWidth(maxWidth);
        }
    }
}
