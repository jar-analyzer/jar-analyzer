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
import me.n1ar4.jar.analyzer.ai.workflow.agent.AgentTurn;
import me.n1ar4.jar.analyzer.ai.workflow.agent.TokenUsage;
import me.n1ar4.jar.analyzer.ai.workflow.core.*;
import me.n1ar4.jar.analyzer.ai.workflow.gui.*;
import me.n1ar4.jar.analyzer.ai.workflow.presets.JarAnalyzerSecurityWorkflow;
import me.n1ar4.jar.analyzer.ai.workflow.report.ReportStore;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnReport;
import me.n1ar4.jar.analyzer.ai.workflow.report.VulnReportHtmlRenderer;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import me.n1ar4.jar.analyzer.utils.OpenUtil;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

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
        final JLabel tokensLbl = new JLabel("tokens: 0");
        final JLabel elapsedLbl = new JLabel("00:00");
        final JPanel topBar = buildTopBar(currentNodeLbl, loopProgressLbl,
                reportsLbl, tokensLbl, elapsedLbl);

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

        // 持有当前运行实例 + 工作线程 + 计时器；提前声明以便节点点击回调读取
        final WorkerHolder workerHolder = new WorkerHolder();
        // 4 个数据采集 HTTP 节点完成后产生的结果（nodeId -> 数据列表）。
        // 仅持有"已成功完成"节点的快照，供节点点击时分页展示。
        final Map<String, List<Object>> collectedData =
                Collections.synchronizedMap(new LinkedHashMap<String, List<Object>>());

        canvas.setNodeClickListener(node -> {
            String id = node.getId();
            if ("loop".equals(id)) {
                bottomTabs.setSelectedIndex(1);
            } else if ("aiAgent".equals(id)) {
                showAgentConversation(dlg, workerHolder.wf);
            } else if (isCollectorNode(id)) {
                // 优先取已缓存数据；若没有（progress listener 时序问题或 dialog 重开），
                // 直接从当前 DagContext 兜底读取一次。这样只要节点已变绿就能看到。
                List<Object> items = collectedData.get(id);
                if ((items == null || items.isEmpty()) && workerHolder.ctx != null) {
                    NodeResult r = workerHolder.ctx.getOutput(id);
                    if (r != null && r.getData() != null) {
                        items = toItemList(r.getData());
                        collectedData.put(id, items);
                    }
                }
                showCollectedData(dlg, node, items);
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
            collectedData.clear();
            currentNodeLbl.setText("starting...");
            loopProgressLbl.setText("loop: 0/0");
            reportsLbl.setText("reports: 0");
            tokensLbl.setText("0");
            elapsedLbl.setText("00:00");

            appendLine(execLogArea, "[INFO] api=" + api + " maxClasses=" + maxClasses
                    + " maxIters=" + maxIters);

            runBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            final long startMs = System.currentTimeMillis();
            final JarAnalyzerSecurityWorkflow finalWf = wf;
            workerHolder.wf = finalWf;

            final Timer elapsedTimer = new Timer(500, ev -> {
                long s = (System.currentTimeMillis() - startMs) / 1000;
                elapsedLbl.setText(String.format("%02d:%02d", s / 60, s % 60));
                // 同步实时刷新 token 标签
                TokenUsage usage = finalWf.getTokenUsage();
                tokensLbl.setText(formatTokenLabel(usage, finalWf.getTokenCallCount()));
                tokensLbl.setToolTipText(formatTokenTooltip(usage, finalWf.getTokenCallCount()));
            });
            elapsedTimer.start();
            workerHolder.elapsedTimer = elapsedTimer;

            final DagContext ctx = new DagContext();
            ctx.setProgressListener((nodeId, status, message) -> {
                canvas.setStatus(nodeId, status, message);
                appendLine(execLogArea, "[" + status + "] " + nodeId
                        + (message == null ? "" : " - " + message));
                // 节点成功时，对 4 个采集类 HTTP 节点抓一份输出快照，供后续点击查看
                if (status == NodeStatus.SUCCESS && isCollectorNode(nodeId)) {
                    NodeResult r = ctx.getOutput(nodeId);
                    if (r != null) {
                        collectedData.put(nodeId, toItemList(r.getData()));
                    }
                }
                SwingUtilities.invokeLater(() -> currentNodeLbl.setText(
                        status + " · " + nodeId
                                + (message == null || message.isEmpty() ? "" : " · " + message)));
            });
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
                        // 注意：故意不把 workerHolder.ctx 置 null。运行结束后用户仍会
                        // 点击节点查看采集数据 / AI Agent 对话，需要保留 ctx 让 fallback
                        // 能读到 nodeOutputs。下次点击 Run 会重新创建一个 ctx 覆盖它。
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
        JarAnalyzerSecurityWorkflow wf;

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

        // 简短提示：避免在窄宽 / 大字体下溢出；详细文案走"查看说明"按钮
        JPanel hintBox = new JPanel(new BorderLayout(0, 6));
        hintBox.setOpaque(false);
        JLabel hint = new JLabel(
                "<html><div style='color:#888; line-height:150%;'>"
                        + "鼠标悬停字段可见配置含义；点击节点查看详情。"
                        + "</div></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        JButton helpBtn = new JButton("查看完整说明 / 使用方法");
        helpBtn.setFont(helpBtn.getFont().deriveFont(11f));
        helpBtn.addActionListener(ev -> showHelpDialog(SwingUtilities.getWindowAncestor(side)));
        hintBox.add(hint, BorderLayout.CENTER);
        hintBox.add(helpBtn, BorderLayout.SOUTH);
        gc.gridy = 5;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        side.add(hintBox, gc);

        return side;
    }

    /**
     * 弹出"使用说明 / 配置详解"窗口。文案放这里集中管理，避免污染 sidebar 布局。
     */
    private static void showHelpDialog(Window owner) {
        String html = "<html><body style='font-family:\"Segoe UI\",\"Microsoft YaHei\",sans-serif; "
                + "font-size:12px; color:#222; padding:8px;'>"
                + "<h3 style='margin:0 0 6px 0; color:#0366d6;'>配置说明</h3>"
                + "<p><b style='color:#0366d6;'>API</b><br/>"
                + "Jar-Analyzer 后端服务地址（一般是本地 <code>http://127.0.0.1:10032</code>），"
                + "用于让 Workflow 调用类/方法/反编译相关接口。</p>"
                + "<p><b style='color:#0366d6;'>Max Classes</b><br/>"
                + "单次扫描最多分析的<u>入口类</u>数量（Servlet / Filter / Listener / Spring Controller 总和）。"
                + "数值越大，覆盖越全；但每多一个类都会触发一次完整 AI Agent 调用，<b>token 消耗</b>与<b>耗时</b>会同步增长。"
                + "建议先用较小值（如 50）做一次试跑，确认效果后再放大。</p>"
                + "<p><b style='color:#0366d6;'>Max Iters</b><br/>"
                + "AI Agent 在<u>单个类</u>上的最大对话轮数（含工具调用循环）。"
                + "轮数越多，模型可以追溯越深的调用链；但每多一轮就是一次新的 LLM 请求，token 消耗增加。"
                + "建议保持在 5–15 之间。</p>"
                + "<p><b style='color:#0366d6;'>Model</b><br/>"
                + "只读，等于当前激活的 AI 配置中选择的模型；如需切换请在 AI 设置面板修改。</p>"
                + "<h3 style='margin:14px 0 6px 0; color:#28a745;'>使用方法</h3>"
                + "<ol style='margin:0; padding-left:20px;'>"
                + "<li>滚轮缩放画布</li>"
                + "<li>空白处拖动平移</li>"
                + "<li>节点可拖动微调位置</li>"
                + "<li>点击节点查看详情</li>"
                + "<li>点击 Loop 节点切换到 Loop Iterations 日志</li>"
                + "<li>点击 4 个采集节点（Servlet/Filter/Listener/Controller）查看抓取数据（分页 20/页）</li>"
                + "<li>点击 AI Agent 节点查看每轮 prompt/response（高亮渲染）</li>"
                + "<li>点击 Reports 按钮查看已收集的漏洞报告</li>"
                + "</ol>"
                + "<h3 style='margin:14px 0 6px 0; color:#d73a49;'>注意事项</h3>"
                + "<ul style='margin:0; padding-left:20px;'>"
                + "<li>Workflow 通过 OpenAI 兼容协议解析 <code>usage</code> 字段统计 token；"
                + "若服务端不返回 usage，顶栏 Tokens 计数会保持为 0。</li>"
                + "</ul>"
                + "</body></html>";
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(pane);
        sp.setPreferredSize(new Dimension(560, 540));
        JOptionPane.showMessageDialog(owner, sp,
                "Workflow 使用说明", JOptionPane.PLAIN_MESSAGE);
    }

    private static JPanel buildFormPanel(JTextField apiField, JSpinner maxClassesSp,
                                         JSpinner maxItersSp, JTextField modelField) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("配置"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // 给两个 spinner 加 tooltip，鼠标悬停可见配置含义
        final String tipMaxClasses =
                "<html>单次扫描最多分析的入口类（Servlet/Filter/Listener/Controller）数量。<br/>"
                        + "数值越大，覆盖越全，但 token 消耗与耗时会显著增加。</html>";
        final String tipMaxIters =
                "<html>AI Agent 在单个类上的最大对话轮数（含工具调用）。<br/>"
                        + "数值越大，分析越深入；但每多一轮都会增加一次 LLM 请求与 token 消耗。</html>";
        maxClassesSp.setToolTipText(tipMaxClasses);
        maxItersSp.setToolTipText(tipMaxIters);

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
        JLabel lblMc = new JLabel("Max Classes:");
        lblMc.setToolTipText(tipMaxClasses);
        form.add(lblMc, gc);
        gc.gridx = 1;
        gc.weightx = 1.0;
        form.add(maxClassesSp, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 0;
        JLabel lblMi = new JLabel("Max Iters:");
        lblMi.setToolTipText(tipMaxIters);
        form.add(lblMi, gc);
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
                                      JLabel reportsLbl, JLabel tokensLbl, JLabel elapsedLbl) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 184, 195)),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        // 用 FlowLayout 自动横向排列，溢出时自动换行；不再用 GridBag 一行硬挤
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 2));
        left.setOpaque(false);
        currentNodeLbl.setFont(currentNodeLbl.getFont().deriveFont(Font.PLAIN, 12f));
        loopProgressLbl.setFont(loopProgressLbl.getFont().deriveFont(Font.PLAIN, 12f));
        reportsLbl.setFont(reportsLbl.getFont().deriveFont(Font.BOLD, 12f));
        reportsLbl.setForeground(new Color(224, 49, 49));
        tokensLbl.setFont(tokensLbl.getFont().deriveFont(Font.BOLD, 12f));
        tokensLbl.setForeground(new Color(45, 105, 200));
        // 给 token 标签一个最小宽度，避免在 fontMetrics 计算时被裁切
        tokensLbl.setHorizontalAlignment(SwingConstants.LEFT);
        left.add(prefix("当前节点：", currentNodeLbl));
        left.add(prefix("Loop：", loopProgressLbl));
        left.add(prefix("漏洞：", reportsLbl));
        left.add(prefix("Tokens：", tokensLbl));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
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
        final List<VulnReport> reps = s.loadAll();
        String json = JSON.toJSONString(reps,
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue);

        // 用 JEditorPane + HTML 高亮渲染（key/string/number 着色，自动换行）
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setText(HtmlSyntaxRenderer.renderJson(json));
        pane.setCaretPosition(0);
        pane.setBackground(Color.WHITE);
        JScrollPane sc = new JScrollPane(pane);
        sc.setPreferredSize(new Dimension(900, 540));

        JPanel panel = new JPanel(new BorderLayout());

        // 顶部：信息 + 操作按钮
        JPanel head = new JPanel(new BorderLayout());
        head.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        JLabel info = new JLabel("Reports: " + reps.size() + "    Dir: " + s.getBaseDir());
        head.add(info, BorderLayout.WEST);

        JButton genHtmlBtn = new JButton("生成 HTML 报告");
        genHtmlBtn.setIcon(SvgManager.WfReportIcon);
        genHtmlBtn.addActionListener(e -> generateAndOpenHtmlReport(parent, reps));

        JButton copyBtn = new JButton("复制 JSON");
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection ss =
                    new java.awt.datatransfer.StringSelection(json);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(ss, ss);
        });

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnBox.add(copyBtn);
        btnBox.add(genHtmlBtn);
        head.add(btnBox, BorderLayout.EAST);

        panel.add(head, BorderLayout.NORTH);
        panel.add(sc, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, panel,
                "Vulnerability Reports", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 生成完整的 HTML 漏洞报告（自包含、无第三方依赖），并用系统默认浏览器打开。
     */
    private static void generateAndOpenHtmlReport(Component parent, List<VulnReport> reps) {
        String file = VulnReportHtmlRenderer.renderToFile(reps);
        if (file == null) {
            JOptionPane.showMessageDialog(parent,
                    "生成 HTML 报告失败，请查看日志。",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String absPath = Paths.get(file).toAbsolutePath().toString();
        OpenUtil.open(absPath);
    }

    /**
     * 展示 AI Agent 每一轮发送的 prompt 与收到的 response。
     * <p>
     * 左侧列出所有轮次（按 className · round），右侧显示选中轮次的 prompt / response 详情。
     */
    private static void showAgentConversation(JDialog parent, JarAnalyzerSecurityWorkflow wf) {
        java.util.List<AgentTurn> turns = (wf == null)
                ? java.util.Collections.<AgentTurn>emptyList()
                : wf.getAgentTurns();
        if (turns.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "暂无 AI Agent 对话记录。\n"
                            + "请先点击 Run 运行工作流；AI Agent 执行后，"
                            + "这里会按轮次展示每次发送的 Prompt 与收到的 Response。",
                    "AI Agent 对话", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final DefaultListModel<AgentTurn> model = new DefaultListModel<>();
        for (AgentTurn t : turns) {
            model.addElement(t);
        }
        final JList<AgentTurn> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 高亮渲染：用 JEditorPane(text/html) 替代纯 JTextArea
        final JEditorPane detail = new JEditorPane();
        detail.setContentType("text/html");
        detail.setEditable(false);
        detail.setBackground(Color.WHITE);

        final JButton copyBtn = new JButton("复制本轮原文");
        copyBtn.setEnabled(false);

        list.addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) {
                return;
            }
            AgentTurn sel = list.getSelectedValue();
            if (sel == null) {
                detail.setText("");
                copyBtn.setEnabled(false);
                return;
            }
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                    .format(new Date(sel.getTimestamp()));
            String html = HtmlSyntaxRenderer.renderAgentTurn(
                    sel.getLabel(), sel.getRound() + 1, time,
                    sel.getPrompt(), sel.getResponse());
            detail.setText(html);
            detail.setCaretPosition(0);
            copyBtn.setEnabled(true);
            copyBtn.putClientProperty("turn", sel);
        });
        copyBtn.addActionListener(ev -> {
            Object o = copyBtn.getClientProperty("turn");
            if (!(o instanceof AgentTurn)) {
                return;
            }
            AgentTurn sel = (AgentTurn) o;
            String text = "===== PROMPT =====\n" + sel.getPrompt()
                    + "\n\n===== RESPONSE =====\n" + sel.getResponse();
            java.awt.datatransfer.StringSelection ss =
                    new java.awt.datatransfer.StringSelection(text);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(ss, ss);
        });

        JScrollPane detailScroll = new JScrollPane(detail);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(list), detailScroll);
        split.setDividerLocation(240);
        split.setResizeWeight(0.0);
        split.setPreferredSize(new Dimension(1080, 620));

        JPanel panel = new JPanel(new BorderLayout());
        JPanel head = new JPanel(new BorderLayout());
        head.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
        JLabel info = new JLabel("AI Agent 共 " + turns.size()
                + " 轮交互（点击左侧条目查看每轮 Prompt / Response）");
        head.add(info, BorderLayout.WEST);
        JPanel hr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        hr.add(copyBtn);
        head.add(hr, BorderLayout.EAST);
        panel.add(head, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
        JOptionPane.showMessageDialog(parent, panel,
                "AI Agent 对话 - 每轮 Prompt / Response", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * @deprecated 现已改用 {@link HtmlSyntaxRenderer#renderAgentTurn(String, int, String, String, String)}
     * 保留以避免外部调用方编译失败。
     */
    @Deprecated
    @SuppressWarnings("unused")
    private static String renderAgentTurn(AgentTurn t) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                .format(new Date(t.getTimestamp()));
        StringBuilder sb = new StringBuilder();
        sb.append("Class : ").append(t.getLabel().isEmpty() ? "-" : t.getLabel()).append('\n');
        sb.append("Round : ").append(t.getRound() + 1).append('\n');
        sb.append("Time  : ").append(time).append('\n');
        sb.append("\n========================= PROMPT (发送) =========================\n");
        sb.append(t.getPrompt());
        sb.append("\n========================= RESPONSE (返回) =======================\n");
        sb.append(t.getResponse());
        sb.append('\n');
        return sb.toString();
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

    // ===================== Collector 节点数据展示（分页） =====================

    /**
     * 4 个采集类 HTTP 节点 + 它们的合并节点。点击后会以表格分页方式展示所有抓取到的条目。
     */
    private static boolean isCollectorNode(String nodeId) {
        return "getServlets".equals(nodeId)
                || "getFilters".equals(nodeId)
                || "getListeners".equals(nodeId)
                || "getSpringC".equals(nodeId)
                || "merge".equals(nodeId);
    }

    private static String collectorTitle(String nodeId) {
        switch (nodeId) {
            case "getServlets":
                return "Servlet 列表";
            case "getFilters":
                return "Filter 列表";
            case "getListeners":
                return "Listener 列表";
            case "getSpringC":
                return "Spring Controller 列表";
            case "merge":
                return "全部入口类（合并后）";
            default:
                return nodeId;
        }
    }

    /**
     * 把 HTTP 节点产出的数据规范化为条目列表：JSON 数组 -> List；单个对象 -> 单元素列表；其它 -> 单字符串列表。
     */
    @SuppressWarnings("unchecked")
    private static List<Object> toItemList(Object data) {
        if (data == null) {
            return new ArrayList<>();
        }
        if (data instanceof List) {
            return new ArrayList<>((List<Object>) data);
        }
        List<Object> single = new ArrayList<>();
        single.add(data);
        return single;
    }

    /**
     * 弹出表格 + 分页 dialog，每页 20 条。
     */
    private static void showCollectedData(JDialog parent, NodeView node, List<Object> items) {
        final List<Object> data = items == null
                ? new ArrayList<>() : new ArrayList<>(items);
        final String title = collectorTitle(node.getId()) + "（共 " + data.size() + " 条）";

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "该节点尚未抓取到任何数据。\n" +
                            "请等待节点完成（状态 SUCCESS）后再点击查看。",
                    title, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 推断列：如果是 List<Map>，取所有 key 的并集作为列；否则用单列 "value"
        final List<String> columns = inferColumns(data);
        final int pageSize = 20;
        final int totalPages = Math.max(1, (data.size() + pageSize - 1) / pageSize);

        final int[] currentPage = new int[]{0};
        final PagedTableModel model = new PagedTableModel(data, columns, pageSize);

        final JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        // 列宽自适应（简单版）
        for (int i = 0; i < table.getColumnCount(); i++) {
            int w = i == 0 ? 60 : 220;
            table.getColumnModel().getColumn(i).setPreferredWidth(w);
        }

        final JLabel pageLbl = new JLabel();
        final JButton prev = new JButton("上一页");
        final JButton next = new JButton("下一页");
        final JButton first = new JButton("首页");
        final JButton last = new JButton("末页");
        Runnable refreshBar = () -> {
            pageLbl.setText("第 " + (currentPage[0] + 1) + " / " + totalPages + " 页"
                    + "    每页 " + pageSize + " 条 · 共 " + data.size() + " 条");
            prev.setEnabled(currentPage[0] > 0);
            first.setEnabled(currentPage[0] > 0);
            next.setEnabled(currentPage[0] < totalPages - 1);
            last.setEnabled(currentPage[0] < totalPages - 1);
        };
        prev.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                model.setPage(currentPage[0]);
                refreshBar.run();
            }
        });
        next.addActionListener(e -> {
            if (currentPage[0] < totalPages - 1) {
                currentPage[0]++;
                model.setPage(currentPage[0]);
                refreshBar.run();
            }
        });
        first.addActionListener(e -> {
            currentPage[0] = 0;
            model.setPage(0);
            refreshBar.run();
        });
        last.addActionListener(e -> {
            currentPage[0] = totalPages - 1;
            model.setPage(currentPage[0]);
            refreshBar.run();
        });
        refreshBar.run();

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 4));
        bar.add(pageLbl, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btns.add(first);
        btns.add(prev);
        btns.add(next);
        btns.add(last);
        bar.add(btns, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(bar, BorderLayout.SOUTH);
        root.setPreferredSize(new Dimension(960, 520));

        JOptionPane.showMessageDialog(parent, root, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * 推断列：扫描前若干条 Map item 的所有 key 并集（保持出现顺序）。
     */
    @SuppressWarnings("unchecked")
    private static List<String> inferColumns(List<Object> data) {
        List<String> cols = new ArrayList<>();
        cols.add("#");
        Set<String> seen = new TreeSet<>();
        boolean anyMap = false;
        int sample = Math.min(data.size(), 50);
        // 第一遍：保持 LinkedHashMap 的 key 顺序
        List<String> orderedKeys = new ArrayList<>();
        for (int i = 0; i < sample; i++) {
            Object o = data.get(i);
            if (o instanceof Map) {
                anyMap = true;
                for (Object k : ((Map<?, ?>) o).keySet()) {
                    String key = String.valueOf(k);
                    if (seen.add(key)) {
                        orderedKeys.add(key);
                    }
                }
            }
        }
        if (anyMap) {
            cols.addAll(orderedKeys);
        } else {
            cols.add("value");
        }
        return cols;
    }

    /**
     * 表格模型：以 page * pageSize 为偏移渲染当前页数据。
     */
    private static final class PagedTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final List<Object> data;
        private final List<String> columns;
        private final int pageSize;
        private int page;

        PagedTableModel(List<Object> data, List<String> columns, int pageSize) {
            this.data = data;
            this.columns = columns;
            this.pageSize = pageSize;
            this.page = 0;
        }

        void setPage(int p) {
            this.page = Math.max(0, p);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            int from = page * pageSize;
            int to = Math.min(data.size(), from + pageSize);
            return Math.max(0, to - from);
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int absRow = page * pageSize + rowIndex;
            if (absRow < 0 || absRow >= data.size()) {
                return "";
            }
            if (columnIndex == 0) {
                return absRow + 1;
            }
            String col = columns.get(columnIndex);
            Object item = data.get(absRow);
            if (item instanceof Map) {
                Object v = ((Map<?, ?>) item).get(col);
                return v == null ? "" : String.valueOf(v);
            }
            // 非 Map 时只有 1 列 "value"
            return String.valueOf(item);
        }
    }

    // ===================== Token 标签格式化 =====================

    private static String formatTokenLabel(TokenUsage usage, long calls) {
        if (usage == null) {
            return "0";
        }
        // 顶栏显示精简版（避免横向溢出）；详细信息走 tooltip
        return formatNumber(usage.getTotalTokens())
                + " (" + calls + " calls)";
    }

    private static String formatTokenTooltip(TokenUsage usage, long calls) {
        if (usage == null) {
            return "tokens 0";
        }
        return "<html>"
                + "<b>累计 token 消耗（实时）</b><br/>"
                + "Prompt（输入）：" + formatNumber(usage.getPromptTokens()) + "<br/>"
                + "Completion（输出）：" + formatNumber(usage.getCompletionTokens()) + "<br/>"
                + "Total（合计）：" + formatNumber(usage.getTotalTokens()) + "<br/>"
                + "Chat 调用次数：" + calls + "<br/>"
                + "<i>数据来源：服务端响应中的 usage 字段</i>"
                + "</html>";
    }

    private static String formatNumber(long n) {
        return String.format(Locale.ROOT, "%,d", n);
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
