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

import com.formdev.flatlaf.FlatLaf;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.dfs.DFSResult;
import me.n1ar4.jar.analyzer.gui.render.ZebraTableCellRenderer;
import me.n1ar4.jar.analyzer.taint.*;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

/**
 * 污点分析结果对话框（重构版）。
 * <p>
 * 相比旧版（仅"表格 + 一段日志文本"）：
 * <ul>
 *   <li>结果表新增"Badge"列（链路标签 I→S→G→R）和事件统计列</li>
 *   <li>结果行按通过/未通过着色，提高可视性</li>
 *   <li>顶部增加过滤器（仅显示通过 / 关键字搜索 owner.method）</li>
 *   <li>详情区改为 Tab：流程视图 / 调用链 / 原始日志</li>
 *   <li>"流程视图"按事件类型展示图标和着色，每一步可读性极高</li>
 *   <li>事件流支持双击 Source/Sink 复制全限定名</li>
 * </ul>
 */
@SuppressWarnings("all")
public class TaintResultDialog extends JFrame {
    private static final Logger logger = LogManager.getLogger();

    private JTable resultTable;
    private DefaultTableModel resultTableModel;
    private TableRowSorter<DefaultTableModel> resultSorter;

    private JTextField filterField;
    private JCheckBox onlyPassedBox;

    private JLabel summaryLabel;
    private JLabel sanitizerCountLabel;

    private JTabbedPane detailTabs;
    private JTree eventTree;
    private DefaultTreeModel eventTreeModel;
    private DefaultMutableTreeNode eventTreeRoot;
    private JTextArea callChainArea;
    private JTextArea rawTextArea;

    private JTable sanitizerTable;
    private DefaultTableModel sanitizerTableModel;
    private TableRowSorter<DefaultTableModel> sanitizerSorter;
    private JTextField sanitizerFilterField;

    private JTable propagationTable;
    private DefaultTableModel propagationTableModel;
    private TableRowSorter<DefaultTableModel> propagationSorter;
    private JTextField propagationFilterField;

    private final List<TaintResult> originalTaintResults;

    public TaintResultDialog(Frame parent, List<TaintResult> taintResults) {
        super("污点分析结果详情");
        this.originalTaintResults = taintResults == null ? new ArrayList<>() : taintResults;

        initializeComponents();
        setupLayout();
        loadData();
        setupEventHandlers();

        setSize(1280, 820);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(900, 600));

        // 与项目其它对话框保持一致：先置顶，避免被主窗口遮挡
        setAlwaysOnTop(true);
        setVisible(true);
    }

    // -------------------- 组件 --------------------

    private void initializeComponents() {
        // 结果表：新增 Badge / 事件数 / 命中 sanitizer 列
        String[] resultColumns = {
                "#", "Source 类", "Source 方法", "Sink 类", "Sink 方法",
                "深度", "结果", "链路标签", "事件数", "S/P/G/D"
        };
        resultTableModel = new DefaultTableModel(resultColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 8) {
                    return Integer.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        resultTable = new JTable(resultTableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getTableHeader().setReorderingAllowed(false);
        resultTable.setAutoCreateRowSorter(false);
        resultTable.setRowHeight(22);
        resultSorter = new TableRowSorter<>(resultTableModel);
        resultTable.setRowSorter(resultSorter);
        resultTable.setDefaultRenderer(Object.class, new TaintRowRenderer());
        resultTable.setDefaultRenderer(Integer.class, new TaintRowRenderer());

        int[] widths = {40, 220, 140, 220, 140, 50, 70, 110, 60, 80};
        for (int i = 0; i < widths.length; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        filterField = new JTextField();
        filterField.setToolTipText("按 Source/Sink 类名 / 方法名 关键字过滤（不区分大小写）");
        onlyPassedBox = new JCheckBox("仅显示通过");

        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        applySummaryColor(summaryLabel, true);

        sanitizerCountLabel = new JLabel(" ");
        sanitizerCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        applySummaryColor(sanitizerCountLabel, false);

        // -------------------- 详情区 --------------------
        eventTreeRoot = new DefaultMutableTreeNode("污点分析事件流");
        eventTreeModel = new DefaultTreeModel(eventTreeRoot);
        eventTree = new JTree(eventTreeModel);
        eventTree.setRootVisible(false);
        eventTree.setShowsRootHandles(true);
        eventTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        eventTree.setCellRenderer(new TaintEventTreeCellRenderer());
        eventTree.setRowHeight(20);

        callChainArea = new JTextArea();
        callChainArea.setEditable(false);
        callChainArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        rawTextArea = new JTextArea();
        rawTextArea.setEditable(false);
        rawTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        detailTabs = new JTabbedPane();
        detailTabs.addTab("流程视图", new JScrollPane(eventTree));
        detailTabs.addTab("调用链", new JScrollPane(callChainArea));
        detailTabs.addTab("原始日志", new JScrollPane(rawTextArea));

        // -------------------- Sanitizer 表 --------------------
        String[] sanitizerColumns = {"类名", "方法名", "方法描述", "参数索引"};
        sanitizerTableModel = new DefaultTableModel(sanitizerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sanitizerTable = new JTable(sanitizerTableModel);
        sanitizerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sanitizerTable.getTableHeader().setReorderingAllowed(false);
        sanitizerTable.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());
        sanitizerSorter = new TableRowSorter<>(sanitizerTableModel);
        sanitizerTable.setRowSorter(sanitizerSorter);
        sanitizerTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        sanitizerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        sanitizerTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        sanitizerTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        sanitizerFilterField = new JTextField();
        sanitizerFilterField.setToolTipText("按类名 / 方法名 / 描述符 关键字过滤");

        // -------------------- Propagation 表 --------------------
        String[] propagationColumns = {"类名", "方法名", "方法描述", "from", "to"};
        propagationTableModel = new DefaultTableModel(propagationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        propagationTable = new JTable(propagationTableModel);
        propagationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        propagationTable.getTableHeader().setReorderingAllowed(false);
        propagationTable.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());
        propagationSorter = new TableRowSorter<>(propagationTableModel);
        propagationTable.setRowSorter(propagationSorter);
        propagationTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        propagationTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        propagationTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        propagationTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        propagationTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        propagationFilterField = new JTextField();
        propagationFilterField.setToolTipText("按类名 / 方法名 / from / to 关键字过滤");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // ---- 顶部：统计 + 过滤 ----
        JPanel topPanel = new JPanel(new BorderLayout(8, 4));
        topPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
        summaryPanel.setBorder(new TitledBorder("统计信息"));
        summaryPanel.add(summaryLabel);
        summaryPanel.add(sanitizerCountLabel);

        JPanel filterPanel = new JPanel(new BorderLayout(6, 0));
        filterPanel.setBorder(new TitledBorder("过滤"));
        filterPanel.add(new JLabel("关键字："), BorderLayout.WEST);
        filterPanel.add(filterField, BorderLayout.CENTER);
        filterPanel.add(onlyPassedBox, BorderLayout.EAST);

        topPanel.add(summaryPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // ---- 中：上结果表 / 下详情+sanitizer ----
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setResizeWeight(0.42);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("污点分析结果"));
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplit.setResizeWeight(0.66);

        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(new TitledBorder("污点分析详情"));
        detailPanel.add(detailTabs, BorderLayout.CENTER);

        JPanel sanitizerPanel = new JPanel(new BorderLayout(4, 4));
        JPanel sanitizerTopPanel = new JPanel(new BorderLayout(4, 0));
        sanitizerTopPanel.add(new JLabel("过滤："), BorderLayout.WEST);
        sanitizerTopPanel.add(sanitizerFilterField, BorderLayout.CENTER);
        sanitizerPanel.add(sanitizerTopPanel, BorderLayout.NORTH);
        sanitizerPanel.add(new JScrollPane(sanitizerTable), BorderLayout.CENTER);

        JPanel propagationPanel = new JPanel(new BorderLayout(4, 4));
        JPanel propagationTopPanel = new JPanel(new BorderLayout(4, 0));
        propagationTopPanel.add(new JLabel("过滤："), BorderLayout.WEST);
        propagationTopPanel.add(propagationFilterField, BorderLayout.CENTER);
        propagationPanel.add(propagationTopPanel, BorderLayout.NORTH);
        propagationPanel.add(new JScrollPane(propagationTable), BorderLayout.CENTER);

        JTabbedPane rulesTabs = new JTabbedPane();
        rulesTabs.addTab("Sanitizer", sanitizerPanel);
        rulesTabs.addTab("Propagation", propagationPanel);
        JPanel rulesContainer = new JPanel(new BorderLayout());
        rulesContainer.setBorder(new TitledBorder("规则库"));
        rulesContainer.add(rulesTabs, BorderLayout.CENTER);

        bottomSplit.setLeftComponent(detailPanel);
        bottomSplit.setRightComponent(rulesContainer);

        mainSplit.setTopComponent(resultPanel);
        mainSplit.setBottomComponent(bottomSplit);

        // ---- 底部按钮 ----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyChainBtn = new JButton("复制调用链");
        JButton exportButton = new JButton("导出结果");
        JButton closeButton = new JButton("关闭");
        copyChainBtn.addActionListener(e -> copyCurrentCallChain());
        exportButton.addActionListener(e -> exportResults());
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(copyChainBtn);
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // -------------------- 数据加载 --------------------

    private void loadData() {
        EnumMap<TaintEvent.Type, Integer> totalCounters = new EnumMap<>(TaintEvent.Type.class);

        for (int i = 0; i < originalTaintResults.size(); i++) {
            TaintResult r = originalTaintResults.get(i);
            DFSResult dfs = r.getDfsResult();

            String sourceClass = "";
            String sourceMethod = "";
            String sinkClass = "";
            String sinkMethod = "";
            int depth = 0;
            if (dfs != null) {
                if (dfs.getSource() != null) {
                    sourceClass = dfs.getSource().getClassReference().getName();
                    sourceMethod = dfs.getSource().getName();
                }
                if (dfs.getSink() != null) {
                    sinkClass = dfs.getSink().getClassReference().getName();
                    sinkMethod = dfs.getSink().getName();
                }
                depth = dfs.getDepth();
            }

            String resultStr = r.isSuccess() ? "通过" : "未通过";

            int sCnt = countOf(r, TaintEvent.Type.SANITIZER_HIT);
            int pCnt = countOf(r, TaintEvent.Type.PROPAGATION_RULE_HIT);
            int gCnt = countOf(r, TaintEvent.Type.GENERIC_PROPAGATE);
            int dCnt = countOf(r, TaintEvent.Type.INVOKEDYNAMIC_PROPAGATE);
            String breakdown = String.format("%d/%d/%d/%d", sCnt, pCnt, gCnt, dCnt);

            String badge = r.getBadge() == null ? "" : r.getBadge();
            int eventsCount = r.getEvents() == null ? 0 : r.getEvents().size();

            resultTableModel.addRow(new Object[]{
                    i + 1, sourceClass, sourceMethod, sinkClass, sinkMethod,
                    depth, resultStr, badge, eventsCount, breakdown
            });

            // 累计统计
            for (TaintEvent ev : r.getEvents()) {
                totalCounters.merge(ev.getType(), 1, Integer::sum);
            }
        }

        long passed = originalTaintResults.stream().filter(TaintResult::isSuccess).count();
        long total = originalTaintResults.size();
        summaryLabel.setText(String.format(
                "总计 %d | 通过 %d | 未通过 %d | 接口透传 %d | 净化 %d | 传播规则 %d | 通用传播 %d | invokedynamic %d",
                total, passed, total - passed,
                totalCounters.getOrDefault(TaintEvent.Type.INTERFACE_PASSTHROUGH, 0),
                totalCounters.getOrDefault(TaintEvent.Type.SANITIZER_HIT, 0),
                totalCounters.getOrDefault(TaintEvent.Type.PROPAGATION_RULE_HIT, 0),
                totalCounters.getOrDefault(TaintEvent.Type.GENERIC_PROPAGATE, 0),
                totalCounters.getOrDefault(TaintEvent.Type.INVOKEDYNAMIC_PROPAGATE, 0)));

        loadRules();
    }

    private int countOf(TaintResult r, TaintEvent.Type type) {
        int n = 0;
        if (r.getEvents() != null) {
            for (TaintEvent ev : r.getEvents()) {
                if (ev.getType() == type) n++;
            }
        }
        return n;
    }

    private void loadRules() {
        int sCount = 0;
        int pCount = 0;
        // ---------- Sanitizer ----------
        try {
            InputStream sin = getClass().getClassLoader().getResourceAsStream("sanitizer.json");
            if (sin == null) {
                logger.warn("sanitizer.json not found");
            } else {
                SanitizerRule rule = SanitizerRule.loadJSON(sin);
                if (rule.getRules() != null) {
                    for (Sanitizer s : rule.getRules()) {
                        sanitizerTableModel.addRow(new Object[]{
                                s.getClassName(), s.getMethodName(),
                                s.getMethodDesc(), s.getParamIndex()
                        });
                    }
                    sCount = rule.getRules().size();
                }
            }
        } catch (Exception e) {
            logger.error("load sanitizer rules failed: {}", e.getMessage());
        }

        // ---------- Propagation ----------
        try {
            InputStream pin = getClass().getClassLoader().getResourceAsStream("propagation.json");
            if (pin == null) {
                logger.warn("propagation.json not found");
            } else {
                PropagationRuleSet rules = PropagationRuleSet.loadJSON(pin);
                if (rules.getRules() != null) {
                    for (PropagationRule pr : rules.getRules()) {
                        propagationTableModel.addRow(new Object[]{
                                pr.getClassName(), pr.getMethodName(),
                                pr.getMethodDesc(), pr.getFrom(), pr.getTo()
                        });
                    }
                    pCount = rules.getRules().size();
                }
            }
        } catch (Exception e) {
            logger.error("load propagation rules failed: {}", e.getMessage());
        }

        sanitizerCountLabel.setText(String.format("规则库：Sanitizer %d 条 | Propagation %d 条",
                sCount, pCount));
    }

    // -------------------- 事件处理 --------------------

    private void setupEventHandlers() {
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = resultTable.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = resultTable.convertRowIndexToModel(viewRow);
            showDetailForRow(modelRow);
        });

        DocumentListener filterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        };
        filterField.getDocument().addDocumentListener(filterListener);
        onlyPassedBox.addActionListener(e -> applyFilter());

        // 规则库表的关键字过滤
        sanitizerFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyRuleFilter(sanitizerSorter, sanitizerFilterField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyRuleFilter(sanitizerSorter, sanitizerFilterField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyRuleFilter(sanitizerSorter, sanitizerFilterField);
            }
        });
        propagationFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyRuleFilter(propagationSorter, propagationFilterField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyRuleFilter(propagationSorter, propagationFilterField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyRuleFilter(propagationSorter, propagationFilterField);
            }
        });
    }

    /**
     * 通用的规则表过滤：把关键字应用到所有列做不区分大小写的 contains 匹配。
     */
    private void applyRuleFilter(TableRowSorter<DefaultTableModel> sorter, JTextField field) {
        String kw = field.getText() == null ? "" : field.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int n = entry.getValueCount();
                for (int i = 0; i < n; i++) {
                    Object v = entry.getValue(i);
                    if (v != null && v.toString().toLowerCase().contains(kw)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void applyFilter() {
        String kw = filterField.getText() == null ? "" : filterField.getText().trim().toLowerCase();
        boolean onlyPassed = onlyPassedBox.isSelected();
        resultSorter.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (onlyPassed) {
                    Object v = entry.getValue(6); // "结果" 列
                    if (!"通过".equals(String.valueOf(v))) return false;
                }
                if (kw.isEmpty()) return true;
                for (int i : new int[]{1, 2, 3, 4}) {
                    Object v = entry.getValue(i);
                    if (v != null && v.toString().toLowerCase().contains(kw)) return true;
                }
                return false;
            }
        });
    }

    private void showDetailForRow(int modelRow) {
        if (modelRow < 0 || modelRow >= originalTaintResults.size()) {
            return;
        }
        TaintResult r = originalTaintResults.get(modelRow);
        DFSResult dfs = r.getDfsResult();

        // ---- 流程视图：树形事件流（按 chainIndex 分组） ----
        rebuildEventTree(r, dfs);

        // ---- 调用链视图 ----
        StringBuilder cc = new StringBuilder();
        cc.append("==================== 基础信息 ====================\n");
        cc.append("序号: ").append(modelRow + 1).append("\n");
        cc.append("结果: ").append(r.isSuccess() ? "通过" : "未通过").append("\n");
        if (r.getBadge() != null && !r.getBadge().isEmpty()) {
            cc.append("链路标签: ").append(r.getBadge()).append("\n");
        }
        if (dfs != null) {
            if (dfs.getSource() != null) {
                cc.append("Source: ")
                        .append(dfs.getSource().getClassReference().getName())
                        .append('.').append(dfs.getSource().getName())
                        .append(dfs.getSource().getDesc()).append('\n');
            }
            if (dfs.getSink() != null) {
                cc.append("Sink:   ")
                        .append(dfs.getSink().getClassReference().getName())
                        .append('.').append(dfs.getSink().getName())
                        .append(dfs.getSink().getDesc()).append('\n');
            }
            cc.append("深度: ").append(dfs.getDepth()).append('\n');
            cc.append("分析模式: ").append(modeOf(dfs.getMode())).append("\n\n");

            cc.append("==================== 调用链 ====================\n");
            List<MethodReference.Handle> ml = dfs.getMethodList();
            if (ml != null) {
                for (int i = 0; i < ml.size(); i++) {
                    MethodReference.Handle h = ml.get(i);
                    cc.append(String.format("[%2d] %s.%s%s%n", i,
                            h.getClassReference().getName(), h.getName(), h.getDesc()));
                }
            }
        }
        callChainArea.setText(cc.toString());
        callChainArea.setCaretPosition(0);

        // ---- 原始日志 ----
        rawTextArea.setText(r.getTaintText() == null ? "" : r.getTaintText());
        rawTextArea.setCaretPosition(0);
    }

    private static String modeOf(int mode) {
        switch (mode) {
            case DFSResult.FROM_SOURCE_TO_SINK:
                return "Source → Sink";
            case DFSResult.FROM_SINK_TO_SOURCE:
                return "Sink → Source";
            case DFSResult.FROM_SOURCE_TO_ALL:
                return "Source → 任意";
            default:
                return "未知";
        }
    }

    private void copyCurrentCallChain() {
        int viewRow = resultTable.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = resultTable.convertRowIndexToModel(viewRow);
        TaintResult r = originalTaintResults.get(modelRow);
        if (r.getDfsResult() == null || r.getDfsResult().getMethodList() == null) return;
        StringBuilder sb = new StringBuilder();
        for (MethodReference.Handle h : r.getDfsResult().getMethodList()) {
            sb.append(h.getClassReference().getName()).append('.')
                    .append(h.getName()).append(h.getDesc()).append('\n');
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(sb.toString()), null);
            JOptionPane.showMessageDialog(this, "调用链已复制到剪贴板", "提示", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "复制失败: " + ex, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportResults() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("导出污点分析结果");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            java.io.File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new java.io.File(file.getAbsolutePath() + ".txt");
            }
            StringBuilder out = new StringBuilder();
            out.append("污点分析结果导出\n");
            out.append("导出时间: ").append(new java.util.Date()).append("\n\n");
            for (int i = 0; i < originalTaintResults.size(); i++) {
                TaintResult r = originalTaintResults.get(i);
                out.append("==================== 结果 ").append(i + 1).append(" ====================\n");
                out.append("分析结果: ").append(r.isSuccess() ? "通过" : "未通过").append('\n');
                if (r.getBadge() != null && !r.getBadge().isEmpty()) {
                    out.append("链路标签: ").append(r.getBadge()).append('\n');
                }
                DFSResult dfs = r.getDfsResult();
                if (dfs != null) {
                    if (dfs.getSource() != null) {
                        out.append("Source: ").append(dfs.getSource().getClassReference().getName())
                                .append('.').append(dfs.getSource().getName()).append('\n');
                    }
                    if (dfs.getSink() != null) {
                        out.append("Sink:   ").append(dfs.getSink().getClassReference().getName())
                                .append('.').append(dfs.getSink().getName()).append('\n');
                    }
                    out.append("深度: ").append(dfs.getDepth()).append('\n');
                }
                if (r.getEvents() != null && !r.getEvents().isEmpty()) {
                    out.append("--- 事件流 ---\n");
                    for (TaintEvent ev : r.getEvents()) {
                        out.append(ev.toPlainLine()).append('\n');
                    }
                } else if (r.getTaintText() != null) {
                    out.append("--- 分析过程 ---\n").append(r.getTaintText()).append('\n');
                }
                out.append('\n');
            }
            java.nio.file.Files.write(file.toPath(), out.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "导出成功: " + file.getAbsolutePath(),
                    "导出完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            logger.error("export failed: {}", ex.getMessage());
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------- 渲染器 --------------------

    private static void applySummaryColor(JLabel l, boolean greenLike) {
        if (FlatLaf.isLafDark()) {
            l.setForeground(greenLike ? new Color(110, 200, 110) : new Color(110, 170, 255));
        } else {
            l.setForeground(greenLike ? new Color(0, 110, 0) : new Color(0, 0, 150));
        }
    }

    /**
     * 行级别着色：success → 浅绿系，fail → 浅红系。
     */
    private final class TaintRowRenderer extends ZebraTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) return c;

            int modelRow = table.convertRowIndexToModel(row);
            Object resultObj = table.getModel().getValueAt(modelRow, 6);
            boolean pass = "通过".equals(String.valueOf(resultObj));

            // 在 LAF 现有 Zebra 背景的基础上轻微叠加色调
            Color base = c.getBackground();
            if (FlatLaf.isLafDark()) {
                c.setBackground(blend(base, pass ? new Color(40, 90, 40) : new Color(110, 50, 50), 0.35f));
            } else {
                c.setBackground(blend(base, pass ? new Color(225, 245, 225) : new Color(252, 226, 226), 0.55f));
            }
            return c;
        }
    }

    private static Color blend(Color a, Color b, float t) {
        if (a == null) return b;
        float u = 1f - t;
        return new Color(
                Math.min(255, Math.round(a.getRed() * u + b.getRed() * t)),
                Math.min(255, Math.round(a.getGreen() * u + b.getGreen() * t)),
                Math.min(255, Math.round(a.getBlue() * u + b.getBlue() * t)));
    }

    // -------------------- 事件树构建 --------------------

    /**
     * 步骤分组节点的 payload：
     * 表示"链路第 N 步对应的方法"，方便双击直接打开该方法所属的反编译源码。
     */
    private static final class StepNodePayload {
        final int stepIndex;
        final String owner;
        final String methodName;
        final String methodDesc;
        final String label;

        StepNodePayload(int stepIndex, String owner, String methodName, String methodDesc, String label) {
            this.stepIndex = stepIndex;
            this.owner = owner;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * 重建事件流树，按 chainIndex 分组：
     * <pre>
     *   - Chain Lifecycle              （chainIndex < 0 的事件，例如 CHAIN_START / PASS / FAIL / 全局 INFO）
     *   - Step #0 owner.method(...)    （来自 dfs.methodList[0]）
     *       - 子事件 ...
     *   - Step #1 owner.method(...)
     *       ...
     * </pre>
     */
    private void rebuildEventTree(TaintResult r, DFSResult dfs) {
        eventTreeRoot.removeAllChildren();

        // 全局/未归属事件分组
        DefaultMutableTreeNode lifecycle = new DefaultMutableTreeNode(
                new StepNodePayload(-1, null, null, null, "链路生命周期"));

        // chainIndex -> 分组节点（按序号 0,1,2... 排列）
        Map<Integer, DefaultMutableTreeNode> stepNodes = new LinkedHashMap<>();
        List<MethodReference.Handle> ml = (dfs == null) ? null : dfs.getMethodList();

        for (TaintEvent ev : r.getEvents()) {
            int idx = ev.getChainIndex();
            if (idx < 0) {
                lifecycle.add(new DefaultMutableTreeNode(ev));
                continue;
            }
            DefaultMutableTreeNode group = stepNodes.get(idx);
            if (group == null) {
                String owner = null, mname = null, mdesc = null;
                if (ml != null && idx < ml.size()) {
                    MethodReference.Handle h = ml.get(idx);
                    owner = h.getClassReference().getName();
                    mname = h.getName();
                    mdesc = h.getDesc();
                }
                String label = "Step #" + idx
                        + (owner == null ? "" : ("  " + shortenOwner(owner)
                        + "." + (mname == null ? "" : mname)));
                StepNodePayload payload = new StepNodePayload(idx, owner, mname, mdesc, label);
                group = new DefaultMutableTreeNode(payload);
                stepNodes.put(idx, group);
            }
            group.add(new DefaultMutableTreeNode(ev));
        }

        // 先放生命周期组（仅当其有子节点）
        if (lifecycle.getChildCount() > 0) {
            eventTreeRoot.add(lifecycle);
        }
        for (DefaultMutableTreeNode group : stepNodes.values()) {
            eventTreeRoot.add(group);
        }

        eventTreeModel.reload(eventTreeRoot);
        // 默认展开所有 step 分组
        for (int i = 0; i < eventTree.getRowCount(); i++) {
            eventTree.expandRow(i);
        }
    }

    private static String shortenOwner(String owner) {
        if (owner == null) return "";
        int idx = owner.lastIndexOf('/');
        if (idx < 0) idx = owner.lastIndexOf('.');
        return idx >= 0 ? owner.substring(idx + 1) : owner;
    }

    /**
     * 事件流树渲染：图标符号 + 着色 + 摘要。
     * 同时处理两种 user object：{@link TaintEvent}（叶节点）与 {@link StepNodePayload}（分组）。
     */
    private final class TaintEventTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                      boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            // 关闭默认 folder/leaf 图标，使用纯文本前缀图标，跨主题更稳定
            setIcon(null);
            setOpenIcon(null);
            setClosedIcon(null);
            setLeafIcon(null);

            Object userObj = (value instanceof DefaultMutableTreeNode)
                    ? ((DefaultMutableTreeNode) value).getUserObject()
                    : null;

            if (userObj instanceof TaintEvent) {
                TaintEvent ev = (TaintEvent) userObj;
                String icon = iconOf(ev.getType());
                StringBuilder sb = new StringBuilder();
                sb.append(icon).append("  [").append(ev.getType().name()).append(']');
                if (ev.getOwner() != null) {
                    sb.append("  ").append(shortenOwner(ev.getOwner()));
                    if (ev.getMethodName() != null) {
                        sb.append('.').append(ev.getMethodName());
                    }
                }
                if (ev.getMessage() != null && !ev.getMessage().isEmpty()) {
                    sb.append("  - ").append(ev.getMessage());
                }
                setText(sb.toString());
                if (!sel) {
                    setForeground(colorOf(ev.getType()));
                }
                setToolTipText(ev.toPlainLine());
            } else if (userObj instanceof StepNodePayload) {
                StepNodePayload payload = (StepNodePayload) userObj;
                setText(payload.label);
                setToolTipText(payload.owner == null ? null
                        : (payload.owner + "." + payload.methodName));
                if (!sel) {
                    setForeground(FlatLaf.isLafDark()
                            ? new Color(220, 220, 220) : new Color(40, 40, 40));
                }
            } else {
                // 不应到达：根节点已被隐藏
                if (value != null) setText(String.valueOf(value));
            }
            return this;
        }

        private String iconOf(TaintEvent.Type t) {
            switch (t) {
                case CHAIN_START:
                    return "▶";
                case SOURCE_TRY:
                    return "·";
                case ENTER_METHOD:
                    return "→";
                case REACH_NEXT:
                    return "✓";
                case INTERFACE_PASSTHROUGH:
                    return "I";
                case SANITIZER_HIT:
                    return "⊘";
                case PROPAGATION_RULE_HIT:
                    return "P";
                case GENERIC_PROPAGATE:
                    return "↳";
                case INVOKEDYNAMIC_PROPAGATE:
                    return "λ";
                case CHAIN_PASS:
                    return "✔";
                case CHAIN_FAIL:
                    return "✗";
                case WARN:
                    return "!";
                case INFO:
                default:
                    return "ℹ";
            }
        }

        private Color colorOf(TaintEvent.Type t) {
            boolean dark = FlatLaf.isLafDark();
            switch (t) {
                case CHAIN_START:
                case CHAIN_PASS:
                case REACH_NEXT:
                    return dark ? new Color(120, 210, 120) : new Color(0, 110, 0);
                case CHAIN_FAIL:
                case WARN:
                    return dark ? new Color(230, 110, 110) : new Color(170, 30, 30);
                case SANITIZER_HIT:
                    return dark ? new Color(220, 180, 110) : new Color(180, 110, 0);
                case PROPAGATION_RULE_HIT:
                    return dark ? new Color(110, 200, 200) : new Color(0, 130, 130);
                case GENERIC_PROPAGATE:
                case INVOKEDYNAMIC_PROPAGATE:
                    return dark ? new Color(120, 170, 230) : new Color(20, 80, 180);
                case INTERFACE_PASSTHROUGH:
                    return dark ? new Color(180, 140, 220) : new Color(120, 30, 170);
                case SOURCE_TRY:
                case ENTER_METHOD:
                case INFO:
                default:
                    return dark ? new Color(190, 190, 190) : new Color(80, 80, 80);
            }
        }
    }

    // -------------------- 静态入口 --------------------

    public static void showTaintResults(Frame parent, List<TaintResult> taintResults) {
        SwingUtilities.invokeLater(() -> new TaintResultDialog(parent, taintResults));
    }
}
