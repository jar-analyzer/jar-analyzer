/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.jar.analyzer.ai.AIConfigManager;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagExecutor;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagGraph;
import me.n1ar4.jar.analyzer.ai.workflow.gui.*;
import me.n1ar4.jar.analyzer.ai.workflow.presets.JarAnalyzerSecurityWorkflow;
import me.n1ar4.jar.analyzer.ai.workflow.report.ReportStore;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnReport;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Workflow 主对话框：左侧控制 + 中央画布 + 底部 Tab（Execution Log / Loop Iterations）。
 * <p>
 * 关键 UX：
 * <ul>
 *   <li>左侧 sidebar 固定宽度 280px，文本 wrap 显示，避免 "提示" 被截断</li>
 *   <li>顶部状态条：当前节点 / loop 进度 / 已收集报告 / elapsed</li>
 *   <li>底部 Tab：Execution Log（按节点）+ Loop Iterations（每次 loop 的所有步骤详情）</li>
 *   <li>点击 Loop 节点会自动切到 Loop Iterations 并选中对应迭代</li>
 * </ul>
 */
public final class AIWorkflowDialog {

    private AIWorkflowDialog() {
    }

    public static void open(Component anchor) {
        AIConfig cfg = AIConfigManager.getActive();
        if (!cfg.isReady()) {
            JOptionPane.showMessageDialog(anchor,
                    "AI 尚未配置。请先在 AI 设置面板中创建并启用一个配置。",
                    "AI 未配置", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window owner = anchor == null ? null : SwingUtilities.getWindowAncestor(anchor);
        final JDialog dlg = new JDialog(owner, "Jar Analyzer Workflow",
                JDialog.ModalityType.MODELESS);
        dlg.setSize(new Dimension(1320, 820));
        dlg.setMinimumSize(new Dimension(1100, 700));
        dlg.setLocationRelativeTo(owner);

        // ===== 表单字段 =====
        final JTextField apiField = new JTextField("http://127.0.0.1:10032");
        final JSpinner maxClassesSp = new JSpinner(new SpinnerNumberModel(50, 1, 5000, 10));
        final JSpinner maxItersSp = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        final JTextField modelField = new JTextField(cfg.getModel());
        modelField.setEditable(false);

        final JButton runBtn = new JButton("Run");
        runBtn.setIcon(SvgManager.ElRunIcon);
        final JButton stopBtn = new JButton("Stop");
        stopBtn.setIcon(SvgManager.ElStopIcon);
        stopBtn.setEnabled(false);
        final JButton fitBtn = new JButton("Fit View");
        final JButton viewReportsBtn = new JButton("Reports");
        viewReportsBtn.setIcon(SvgManager.WfReportIcon);
        final JButton closeBtn = new JButton("Close");

        final JPanel sidebar = buildSidebar(apiField, maxClassesSp, maxItersSp, modelField,
                runBtn, stopBtn, fitBtn, viewReportsBtn, closeBtn);

        // ===== 顶部状态栏 =====
        final JLabel currentNodeLbl = new JLabel("idle");
        final JLabel loopProgressLbl = new JLabel("loop: -");
        final JLabel reportsLbl = new JLabel("reports: 0");
        final JLabel elapsedLbl = new JLabel("00:00");
        final JPanel topBar = buildTopBar(currentNodeLbl, loopProgressLbl, reportsLbl, elapsedLbl);

        // ===== 中央画布 =====
        final WorkflowCanvas canvas = new WorkflowCanvas();
        try {
            JarAnalyzerSecurityWorkflow preWf = new JarAnalyzerSecurityWorkflow(
                    cfg, apiField.getText().trim(), new ReportStore(),
                    ((Number) maxClassesSp.getValue()).intValue(),
                    ((Number) maxItersSp.getValue()).intValue());
            WorkflowGraphModel preModel = WorkflowGraphModel.from(preWf.buildGraph());
            JarAnalyzerSecurityWorkflow.applyCompactLayout(preModel);
            canvas.setModel(preModel);
        } catch (Throwable ignored) {
        }

        // ===== 底部 Tab：执行日志 + Loop 迭代 =====
        final JTextArea execLogArea = newConsoleArea();
        JScrollPane execLogScroll = new JScrollPane(execLogArea);

        final DefaultListModel<IterEntry> iterListModel = new DefaultListModel<>();
        final JList<IterEntry> iterList = new JList<>(iterListModel);
        iterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        iterList.setCellRenderer(new IterEntry.Renderer());

        final JTextArea iterDetailArea = newConsoleArea();
        JScrollPane iterDetailScroll = new JScrollPane(iterDetailArea);

        final LoopHistory loopHistory = new LoopHistory();
        iterList.addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) {
                return;
            }
            IterEntry sel = iterList.getSelectedValue();
            if (sel == null) {
                iterDetailArea.setText("");
                return;
            }
            iterDetailArea.setText(renderIterDetail(loopHistory, sel));
            iterDetailArea.setCaretPosition(0);
        });

        JSplitPane iterSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(iterList), iterDetailScroll);
        iterSplit.setDividerLocation(280);
        iterSplit.setResizeWeight(0.0);
        iterSplit.setBorder(null);

        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.addTab("Execution Log", execLogScroll);
        bottomTabs.addTab("Loop Iterations", iterSplit);

        canvas.setNodeClickListener(node -> {
            if ("loop".equals(node.getId())) {
                bottomTabs.setSelectedIndex(1);
            } else {
                showNodeDetail(dlg, node);
            }
        });

        // ===== 拼装主布局 =====
        JPanel center = new JPanel(new BorderLayout());
        center.add(topBar, BorderLayout.NORTH);
        center.add(canvas, BorderLayout.CENTER);

        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, bottomTabs);
        vertical.setResizeWeight(0.72);
        vertical.setDividerSize(6);
        vertical.setBorder(null);

        JSplitPane horizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, vertical);
        horizontal.setDividerLocation(280);
        horizontal.setDividerSize(4);
        horizontal.setBorder(null);

        dlg.setContentPane(horizontal);

        // ===== 行为绑定 =====
        final WorkerHolder workerHolder = new WorkerHolder();

        closeBtn.addActionListener(e -> {
            workerHolder.cancelIfRunning();
            dlg.dispose();
        });

        fitBtn.addActionListener(e -> {
            canvas.zoomToFit();
            canvas.repaint();
        });

        viewReportsBtn.addActionListener(e -> showHistoryReports(dlg));

        stopBtn.addActionListener(e -> {
            workerHolder.cancelIfRunning();
            appendLine(execLogArea, "[STOP] cancellation requested.");
        });

        runBtn.addActionListener(e -> {
            String api = apiField.getText().trim();
            int maxClasses = ((Number) maxClassesSp.getValue()).intValue();
            int maxIters = ((Number) maxItersSp.getValue()).intValue();

            JarAnalyzerSecurityWorkflow wf;
            DagGraph graph;
            try {
                wf = new JarAnalyzerSecurityWorkflow(
                        cfg, api, new ReportStore(), maxClasses, maxIters);
                graph = wf.buildGraph();
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(dlg,
                        "构图失败：" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            WorkflowGraphModel runModel = WorkflowGraphModel.from(graph);
            JarAnalyzerSecurityWorkflow.applyCompactLayout(runModel);
            canvas.setModel(runModel);
            canvas.resetAllStatus();
            execLogArea.setText("");
            iterListModel.clear();
            iterDetailArea.setText("");
            loopHistory.clear();
            currentNodeLbl.setText("starting...");
            loopProgressLbl.setText("loop: 0/0");
            reportsLbl.setText("reports: 0");
            elapsedLbl.setText("00:00");

            appendLine(execLogArea, "[INFO] api=" + api + " maxClasses=" + maxClasses
                    + " maxIters=" + maxIters);

            runBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            final long startMs = System.currentTimeMillis();
            final Timer elapsedTimer = new Timer(500, ev -> {
                long s = (System.currentTimeMillis() - startMs) / 1000;
                elapsedLbl.setText(String.format("%02d:%02d", s / 60, s % 60));
            });
            elapsedTimer.start();
            workerHolder.elapsedTimer = elapsedTimer;

            final DagContext ctx = new DagContext();
            ctx.setProgressListener((nodeId, status, message) -> {
                canvas.setStatus(nodeId, status, message);
                appendLine(execLogArea, "[" + status + "] " + nodeId
                        + (message == null ? "" : " - " + message));
                SwingUtilities.invokeLater(() -> currentNodeLbl.setText(
                        status + " · " + nodeId
                                + (message == null || message.isEmpty() ? "" : " · " + message)));
            });

            final JarAnalyzerSecurityWorkflow finalWf = wf;
            ctx.setLoopListener(event -> {
                loopHistory.onLoopEvent(event);
                final int reports = finalWf.getCollectedReports().size();
                SwingUtilities.invokeLater(() -> {
                    reportsLbl.setText("reports: " + reports);
                    loopProgressLbl.setText("loop: " + (event.getIndex() + 1)
                            + "/" + event.getTotal());
                    // 列表模型增量更新：每次 START 第一次出现该 index 时插入一条
                    refreshIterList(iterListModel, loopHistory, event);
                });
            });
            workerHolder.ctx = ctx;
            final DagGraph finalGraph = graph;

            Thread t = new Thread(() -> {
                try {
                    new DagExecutor(finalGraph).run(ctx);
                    int n = finalWf.getCollectedReports().size();
                    appendLine(execLogArea, "[DONE] " + n + " report(s) collected.");
                } catch (Throwable ex) {
                    appendLine(execLogArea, "[FATAL] " + ex);
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        runBtn.setEnabled(true);
                        stopBtn.setEnabled(false);
                        if (workerHolder.elapsedTimer != null) {
                            workerHolder.elapsedTimer.stop();
                            workerHolder.elapsedTimer = null;
                        }
                        currentNodeLbl.setText("done");
                        workerHolder.ctx = null;
                    });
                }
            }, "ai-workflow");
            t.setDaemon(true);
            workerHolder.thread = t;
            t.start();
        });

        dlg.setVisible(true);
    }

    /**
     * 工作线程 + DagContext + 计时器的容器。
     */
    private static final class WorkerHolder {
        Thread thread;
        DagContext ctx;
        Timer elapsedTimer;

        void cancelIfRunning() {
            if (ctx != null) {
                ctx.cancel();
            }
            if (elapsedTimer != null) {
                elapsedTimer.stop();
            }
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    // ===================== Layout helpers =====================

    private static JPanel buildSidebar(JTextField apiField, JSpinner maxClassesSp,
                                       JSpinner maxItersSp, JTextField modelField,
                                       JButton runBtn, JButton stopBtn, JButton fitBtn,
                                       JButton viewReportsBtn, JButton closeBtn) {
        // 用 GridBagLayout 整体规划，避免 BoxLayout 在窄宽下挤压标签
        JPanel side = new JPanel(new GridBagLayout());
        side.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        side.setPreferredSize(new Dimension(280, 0));
        side.setMinimumSize(new Dimension(260, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(0, 0, 6, 0);

        JLabel title = new JLabel("AI 漏洞扫描工作流");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        gc.gridy = 0;
        side.add(title, gc);

        JLabel subtitle = new JLabel(
                "<html><div style='color:#888; line-height:140%;'>"
                        + "DAG 引擎 · AI workflow"
                        + "</div></html>");
        gc.gridy = 1;
        gc.insets = new Insets(0, 0, 14, 0);
        side.add(subtitle, gc);

        // 配置面板
        JPanel form = buildFormPanel(apiField, maxClassesSp, maxItersSp, modelField);
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 12, 0);
        side.add(form, gc);

        // 操作按钮
        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints bc = new GridBagConstraints();
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1.0;
        bc.insets = new Insets(3, 0, 3, 0);
        bc.gridx = 0;
        bc.gridy = 0;
        buttons.add(runBtn, bc);
        bc.gridy = 1;
        buttons.add(stopBtn, bc);
        bc.gridy = 2;
        buttons.add(fitBtn, bc);
        bc.gridy = 3;
        buttons.add(viewReportsBtn, bc);
        bc.gridy = 4;
        bc.insets = new Insets(18, 0, 3, 0);
        buttons.add(closeBtn, bc);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 12, 0);
        side.add(buttons, gc);

        // 占位空白把提示压到底
        gc.gridy = 4;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        side.add(new JLabel(), gc);

        // 提示语：HTML 自动换行 + 全宽，确保所有文字可见
        JLabel hint = new JLabel(
                "<html><div style='color:#888; line-height:160%; width:230px;'>"
                        + "<b>使用方法</b><br/>"
                        + "1. 滚轮缩放画布<br/>"
                        + "2. 空白处拖动平移<br/>"
                        + "3. 节点可拖动微调位置<br/>"
                        + "4. 点击节点查看详情<br/>"
                        + "5. 点击 Loop 节点切换到迭代日志"
                        + "</div></html>");
        gc.gridy = 5;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        side.add(hint, gc);

        return side;
    }

    private static JPanel buildFormPanel(JTextField apiField, JSpinner maxClassesSp,
                                         JSpinner maxItersSp, JTextField modelField) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("配置"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        form.add(new JLabel("API:"), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        form.add(apiField, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        form.add(new JLabel("Max Classes:"), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        form.add(maxClassesSp, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        form.add(new JLabel("Max Iters:"), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        form.add(maxItersSp, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.weightx = 0;
        form.add(new JLabel("Model:"), gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        form.add(modelField, gc);

        return form;
    }

    private static JPanel buildTopBar(JLabel currentNodeLbl, JLabel loopProgressLbl,
                                      JLabel reportsLbl, JLabel elapsedLbl) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 184, 195)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 24);
        g.anchor = GridBagConstraints.WEST;
        currentNodeLbl.setFont(currentNodeLbl.getFont().deriveFont(Font.PLAIN, 12f));
        loopProgressLbl.setFont(loopProgressLbl.getFont().deriveFont(Font.PLAIN, 12f));
        reportsLbl.setFont(reportsLbl.getFont().deriveFont(Font.BOLD, 12f));
        reportsLbl.setForeground(new Color(224, 49, 49));
        g.gridx = 0;
        left.add(prefix("当前节点：", currentNodeLbl), g);
        g.gridx = 1;
        left.add(prefix("Loop：", loopProgressLbl), g);
        g.gridx = 2;
        left.add(prefix("漏洞：", reportsLbl), g);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(prefix("耗时：", elapsedLbl));

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private static JPanel prefix(String prefix, JLabel valueLbl) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.insets = new Insets(0, 0, 0, 4);
        JLabel pre = new JLabel(prefix);
        pre.setForeground(new Color(120, 120, 130));
        pre.setFont(pre.getFont().deriveFont(Font.PLAIN, 12f));
        p.add(pre, g);
        g.gridx = 1;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(valueLbl, g);
        return p;
    }

    private static JTextArea newConsoleArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setLineWrap(false);
        ta.setBackground(new Color(28, 30, 36));
        ta.setForeground(new Color(220, 222, 230));
        ta.setCaretColor(new Color(220, 222, 230));
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setMargin(new Insets(8, 10, 8, 10));
        return ta;
    }

    // ===================== Iter list rendering =====================

    private static void refreshIterList(DefaultListModel<IterEntry> model,
                                        LoopHistory history,
                                        DagContext.LoopEvent event) {
        if (event == null) {
            return;
        }
        if (event.getPhase() == DagContext.LoopEvent.Phase.START) {
            return;
        }
        // 找到对应 entry，若不存在则新建
        int found = -1;
        for (int i = 0; i < model.size(); i++) {
            IterEntry e = model.get(i);
            if (e.loopId.equals(event.getLoopNodeId()) && e.index == event.getIndex()) {
                found = i;
                break;
            }
        }
        if (found < 0) {
            IterEntry ne = new IterEntry();
            ne.loopId = event.getLoopNodeId();
            ne.index = event.getIndex();
            ne.total = event.getTotal();
            ne.label = event.getLabel();
            ne.lastPhase = event.getPhase();
            ne.lastDetail = event.getDetail();
            model.addElement(ne);
        } else {
            IterEntry e = model.get(found);
            e.lastPhase = event.getPhase();
            e.lastDetail = event.getDetail();
            if (event.getLabel() != null && !event.getLabel().isEmpty()) {
                e.label = event.getLabel();
            }
            // 触发重绘
            model.set(found, e);
        }
    }

    private static String renderIterDetail(LoopHistory history, IterEntry entry) {
        List<DagContext.LoopEvent> events = history.get(entry.loopId, entry.index);
        StringBuilder sb = new StringBuilder();
        sb.append("Loop:  ").append(entry.loopId).append('\n');
        sb.append("Index: ").append(entry.index + 1).append(" / ").append(entry.total).append('\n');
        sb.append("Item:  ").append(entry.label).append('\n');
        sb.append("Phase: ").append(entry.lastPhase).append('\n');
        sb.append("------------------------------------------------------------\n");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT);
        for (DagContext.LoopEvent ev : events) {
            sb.append(sdf.format(new Date(ev.getTimestamp())));
            sb.append("  [").append(ev.getPhase()).append("] ");
            if (ev.getDetail() != null && !ev.getDetail().isEmpty()) {
                sb.append(ev.getDetail());
            } else {
                sb.append(ev.getLabel());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // ===================== Misc =====================

    private static void showNodeDetail(JDialog parent, NodeView node) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID:     ").append(node.getId()).append('\n');
        sb.append("Name:   ").append(node.getTitle()).append('\n');
        sb.append("Kind:   ").append(node.getKind().getLabel()).append('\n');
        sb.append("Status: ").append(node.getStatus()).append('\n');
        if (node.getStatusMessage() != null && !node.getStatusMessage().isEmpty()) {
            sb.append("Msg:    ").append(node.getStatusMessage()).append('\n');
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(420, 220));
        JOptionPane.showMessageDialog(parent, sp,
                "Node: " + node.getId(), JOptionPane.PLAIN_MESSAGE);
    }

    private static void showHistoryReports(JDialog parent) {
        ReportStore s = new ReportStore();
        List<VulnReport> reps = s.loadAll();
        String json = JSON.toJSONString(reps,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue);
        JTextArea ta = new JTextArea(json);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sc = new JScrollPane(ta);
        sc.setPreferredSize(new Dimension(900, 540));
        JPanel panel = new JPanel(new BorderLayout());
        JLabel head = new JLabel("Reports: " + reps.size() + "    Dir: " + s.getBaseDir());
        head.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        panel.add(head, BorderLayout.NORTH);
        panel.add(sc, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, panel,
                "Vulnerability Reports", JOptionPane.PLAIN_MESSAGE);
    }

    private static void appendLine(final JTextArea ta, final String line) {
        final String ts = new SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(new Date());
        SwingUtilities.invokeLater(() -> {
            ta.append(ts + " " + line + "\n");
            ta.setCaretPosition(ta.getDocument().getLength());
        });
    }

    @SuppressWarnings("unused")
    private static NodeKind anchorKind() {
        return NodeKind.GENERIC;
    }

    /**
     * Loop 列表项。
     */
    private static final class IterEntry {
        String loopId;
        int index;
        int total;
        String label = "";
        DagContext.LoopEvent.Phase lastPhase;
        String lastDetail = "";

        @Override
        public String toString() {
            return "#" + (index + 1) + "  " + label;
        }

        static final class Renderer extends javax.swing.DefaultListCellRenderer {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                java.awt.Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof IterEntry && c instanceof JLabel) {
                    IterEntry e = (IterEntry) value;
                    JLabel lbl = (JLabel) c;
                    String phase = e.lastPhase == null ? "-" : e.lastPhase.name();
                    String html = "<html><div style='padding:2px 4px;'>"
                            + "<b>#" + (e.index + 1) + "</b> "
                            + "<span style='color:#888'>[" + phase + "]</span> "
                            + escape(e.label)
                            + "</div></html>";
                    lbl.setText(html);
                }
                return c;
            }

            private static String escape(String s) {
                if (s == null) {
                    return "";
                }
                return s.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
            }
        }
    }
}
