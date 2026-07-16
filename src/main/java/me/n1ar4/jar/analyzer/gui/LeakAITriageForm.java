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

import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.entity.LeakTriageEntry;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 研判结果查看面板（独立 JFrame，不嵌入主 GUI，避免撑高主窗口）
 * <p>
 * 表格列：序号 / 类型 / 值 / 类名 / AI 判定 / 原因
 * 支持："仅显示未通过" 过滤
 */
public class LeakAITriageForm {
    private JPanel rootPanel;
    private JLabel summaryLabel;
    private JScrollPane tableScroll;
    private JTable resultTable;
    private JPanel bottomPanel;
    private JCheckBox onlyFailedBox;
    private JButton closeBtn;

    private final List<LeakTriageEntry> entries;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public LeakAITriageForm(List<LeakTriageEntry> entries) {
        this.entries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        initTable();
        initSummary();
        initListeners();
    }

    private void initSummary() {
        int total = entries.size();
        int pass = 0;
        int fail = 0;
        int err = 0;
        for (LeakTriageEntry e : entries) {
            if (e.isFailed()) {
                err++;
                continue;
            }
            if (e.isSensitive()) {
                pass++;
            } else {
                fail++;
            }
        }
        summaryLabel.setText(String.format(
                "<html>共 <b>%d</b> 条 &nbsp; 通过 <font color='green'><b>%d</b></font> &nbsp; " +
                        "未通过 <font color='red'><b>%d</b></font> &nbsp; " +
                        "调用失败/未研判 <font color='orange'><b>%d</b></font></html>",
                total, pass, fail, err));
    }

    private void initTable() {
        String[] columns = {"#", "类型", "值", "类名/路径", "AI 判定", "原因"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (int i = 0; i < entries.size(); i++) {
            LeakTriageEntry e = entries.get(i);
            LeakResult r = e.getResult();
            String verdict;
            if (e.isFailed()) {
                verdict = "未研判";
            } else if (e.isSensitive()) {
                verdict = "通过";
            } else {
                verdict = "未通过";
            }
            model.addRow(new Object[]{
                    i + 1,
                    r.getTypeName(),
                    r.getValue(),
                    r.getClassName(),
                    verdict,
                    e.getReason()
            });
        }
        resultTable.setModel(model);
        resultTable.setRowHeight(22);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(280);

        // 判定列着色
        DefaultTableCellRenderer verdictRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String v = String.valueOf(value);
                if (!isSelected) {
                    if ("通过".equals(v)) {
                        c.setForeground(new Color(0x1B7F3A));
                    } else if ("未通过".equals(v)) {
                        c.setForeground(new Color(0xC0392B));
                    } else {
                        c.setForeground(new Color(0xCC8400));
                    }
                }
                return c;
            }
        };
        resultTable.getColumnModel().getColumn(4).setCellRenderer(verdictRenderer);

        sorter = new TableRowSorter<>(model);
        resultTable.setRowSorter(sorter);
    }

    private void initListeners() {
        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(rootPanel);
            if (w != null) {
                w.dispose();
            }
        });
        onlyFailedBox.addActionListener(e -> {
            if (onlyFailedBox.isSelected()) {
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        Object v = entry.getValue(4);
                        return "未通过".equals(String.valueOf(v));
                    }
                });
            } else {
                sorter.setRowFilter(null);
            }
        });
    }

    /**
     * 弹出独立窗口展示
     */
    public static JFrame start(List<LeakTriageEntry> entries) {
        JFrame frame = new JFrame("AI 研判面板");
        LeakAITriageForm form = new LeakAITriageForm(entries);
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 3, 1, new Insets(6, 6, 6, 6), -1, -1);
        summaryLabel = new JLabel();
        summaryLabel.setText("AI 研判结果");
        SwingLayout.add(rootPanel, summaryLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        tableScroll = new JScrollPane();
        SwingLayout.add(rootPanel, tableScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(700, 380), null, null, 0);
        resultTable = new JTable();
        tableScroll.setViewportView(resultTable);
        bottomPanel = new JPanel();
        SwingLayout.configureGrid(bottomPanel, 1, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(rootPanel, bottomPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        onlyFailedBox = new JCheckBox();
        onlyFailedBox.setText("仅显示未通过");
        SwingLayout.add(bottomPanel, onlyFailedBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(bottomPanel, spacer1, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        closeBtn = new JButton();
        closeBtn.setText("关闭");
        SwingLayout.add(bottomPanel, closeBtn, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
