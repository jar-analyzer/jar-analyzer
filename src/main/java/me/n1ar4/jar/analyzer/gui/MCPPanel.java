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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.mcp.McpConfig;
import me.n1ar4.jar.analyzer.mcp.McpEventListener;
import me.n1ar4.jar.analyzer.mcp.McpServerLauncher;
import me.n1ar4.jar.analyzer.mcp.tools.ToolDefinition;
import me.n1ar4.jar.analyzer.mcp.tools.ToolRegistry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MCP 控制面板
 *
 * <p>不使用 IDEA GUI Designer 的 .form 文件，纯 Java Swing 构建。</p>
 *
 * <p>布局采用项目同款 {@link GridLayoutManager}，与 MainForm 一致以保持视觉风格统一。</p>
 *
 * <p>自身作为一个独立 {@link JPanel}，由 {@code MCPAction.register} 通过
 * {@code tabbedPanel.addTab("MCP", panel)} 加入到 MainForm 的 tabbedPanel 中，
 * 这样不会污染 MainForm.form，IDEA Designer 重新生成时不会破坏。</p>
 */
public class MCPPanel extends JPanel implements McpEventListener {

    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm:ss");

    // ------- top: header label -------
    private final JLabel descLabel = new JLabel();

    // ------- group: server config -------
    private final JTextField bindField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("20032");
    private final JCheckBox sseBox = new JCheckBox("启用 SSE 传输 (GET /sse + POST /message)", true);
    private final JCheckBox streamableBox = new JCheckBox("启用 Streamable HTTP (POST /mcp)", true);
    private final JCheckBox authBox = new JCheckBox("启用 Token 鉴权");
    private final JTextField tokenField = new JTextField("JAR-ANALYZER-MCP-TOKEN");
    private final JCheckBox debugBox = new JCheckBox("DEBUG 日志");

    // ------- group: control -------
    private final JButton startBtn = new JButton("启动 MCP");
    private final JButton stopBtn = new JButton("停止 MCP");
    private final JLabel statusLabel = new JLabel("已停止");
    private final JLabel sseLabel = new JLabel("SSE 会话: 0");
    private final JLabel streamLabel = new JLabel("Streamable: 0");
    private final JLabel reqStatLabel = new JLabel("请求 0 / 失败 0");
    private final AtomicInteger reqOk = new AtomicInteger(0);
    private final AtomicInteger reqFail = new AtomicInteger(0);

    // ------- group: tools table -------
    private final DefaultTableModel toolsModel = new DefaultTableModel(
            new Object[]{"工具", "描述"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable toolsTable = new JTable(toolsModel);

    // ------- group: log -------
    private final JTextArea logArea = new JTextArea();

    public MCPPanel() {
        super(new GridLayoutManager(5, 1, new Insets(8, 8, 8, 8), -1, -1));
        buildHeader();
        buildConfigPanel();
        buildControlPanel();
        buildToolsPanel();
        buildLogPanel();
        bindActions();
        // 提前注册工具，让面板首次出现时即看到
        McpServerLauncher.getInstance().initToolsIfNeeded();
        refreshTools();
        refreshUiState();
    }

    // ------------------------------------------------------------
    // BUILD
    // ------------------------------------------------------------
    private void buildHeader() {
        descLabel.setText("<html>" +
                "<div style='padding:4px 0'>" +
                "<span style='font-size:13pt; font-weight:bold; color:#2E5BFF'>♦ MCP Server (Java)</span>" +
                " <span style='color:#666'>— 内嵌 MCP 服务器，纯 Java 实现，支持 <b>SSE</b> 与 <b>Streamable HTTP</b></span>" +
                "</div>" +
                "<div style='font-size:12pt; color:#666; padding-top:2px'>" +
                "可被任意 MCP Client 接入（如 Claude Desktop、Cursor 等），暴露 jar-analyzer 的全部分析能力" +
                "</div>" +
                "</html>");
        add(descLabel, gc(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED));
    }

    private void buildConfigPanel() {
        JPanel p = new JPanel(new GridLayoutManager(4, 4, new Insets(6, 8, 6, 8), -1, -1));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "配置",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        p.add(new JLabel("Bind"), gc(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(bindField, gc(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED));
        p.add(new JLabel("Port"), gc(0, 2, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(portField, gc(0, 3, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED));

        p.add(sseBox, gc(1, 0, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(streamableBox, gc(1, 2, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));

        p.add(authBox, gc(2, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(new JLabel("Token"), gc(2, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(tokenField, gc(2, 2, 1, 2,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED));

        p.add(debugBox, gc(3, 0, 1, 4,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));

        add(p, gc(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED));
    }

    private void buildControlPanel() {
        JPanel p = new JPanel(new GridLayoutManager(2, 6, new Insets(6, 8, 6, 8), -1, -1));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "控制 / 状态",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));

        startBtn.setBackground(new Color(0x2E5BFF));
        startBtn.setForeground(Color.WHITE);
        stopBtn.setBackground(new Color(0xCC3333));
        stopBtn.setForeground(Color.WHITE);

        p.add(startBtn, gc(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED));
        p.add(stopBtn, gc(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED));

        statusLabel.setForeground(new Color(0xCC3333));
        p.add(new JLabel("状态:"), gc(0, 2, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(statusLabel, gc(0, 3, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED));
        p.add(sseLabel, gc(0, 4, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));
        p.add(streamLabel, gc(0, 5, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));

        p.add(reqStatLabel, gc(1, 0, 1, 6,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));

        add(p, gc(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED));
    }

    private void buildToolsPanel() {
        JPanel p = new JPanel(new GridLayoutManager(1, 1, new Insets(6, 8, 6, 8), -1, -1));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "已注册工具",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));
        toolsTable.setFillsViewportHeight(true);
        toolsTable.setRowHeight(20);
        toolsTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        toolsTable.getColumnModel().getColumn(1).setPreferredWidth(600);
        JScrollPane sp = new JScrollPane(toolsTable);
        p.add(sp, gc(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK));
        add(p, gc(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK));
    }

    private void buildLogPanel() {
        JPanel p = new JPanel(new GridLayoutManager(1, 2, new Insets(6, 8, 6, 8), -1, -1));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "日志",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(logArea);
        sp.setPreferredSize(new Dimension(600, 160));
        p.add(sp, gc(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK));

        JButton clearBtn = new JButton("清空日志");
        clearBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> logArea.setText("")));
        p.add(clearBtn, gc(0, 1, 1, 1,
                GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED));

        add(p, gc(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK));
    }

    private static GridConstraints gc(int row, int col, int rowSpan, int colSpan,
                                      int anchor, int fill,
                                      int hSize, int vSize) {
        return new GridConstraints(row, col, rowSpan, colSpan,
                anchor, fill, hSize, vSize, null, null, null, 0, false);
    }

    // ------------------------------------------------------------
    // BIND
    // ------------------------------------------------------------
    private void bindActions() {
        ActionListener startAl = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStart();
            }
        };
        ActionListener stopAl = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStop();
            }
        };
        startBtn.addActionListener(startAl);
        stopBtn.addActionListener(stopAl);
    }

    private void doStart() {
        try {
            McpConfig cfg = new McpConfig();
            cfg.setBind(bindField.getText().trim());
            cfg.setPort(parseInt(portField.getText().trim(), 20032));
            cfg.setEnableSse(sseBox.isSelected());
            cfg.setEnableStreamable(streamableBox.isSelected());
            cfg.setAuth(authBox.isSelected());
            cfg.setToken(tokenField.getText());
            cfg.setDebug(debugBox.isSelected());
            if (!cfg.isEnableSse() && !cfg.isEnableStreamable()) {
                JOptionPane.showMessageDialog(this,
                        "至少启用一个传输方式（SSE 或 Streamable HTTP）",
                        "MCP", JOptionPane.WARNING_MESSAGE);
                return;
            }
            McpServerLauncher.getInstance().start(cfg, this);
            appendLog("INFO ", "MCP 已启动: http://" + cfg.getBind() + ":" + cfg.getPort());
            if (cfg.isEnableSse()) {
                appendLog("INFO ", "SSE 接入: http://" + cfg.getBind() + ":" + cfg.getPort() + "/sse");
            }
            if (cfg.isEnableStreamable()) {
                appendLog("INFO ", "Streamable: http://" + cfg.getBind() + ":" + cfg.getPort() + "/mcp");
            }
            refreshUiState();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "MCP 启动失败: " + ex.getMessage(),
                    "MCP", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "MCP", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            appendLog("ERROR", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void doStop() {
        try {
            McpServerLauncher.getInstance().stop();
            appendLog("INFO ", "MCP 已停止");
        } catch (Exception ex) {
            appendLog("ERROR", ex.getMessage());
        }
        refreshUiState();
    }

    private void refreshUiState() {
        boolean running = McpServerLauncher.getInstance().isRunning();
        startBtn.setEnabled(!running);
        stopBtn.setEnabled(running);
        bindField.setEnabled(!running);
        portField.setEnabled(!running);
        sseBox.setEnabled(!running);
        streamableBox.setEnabled(!running);
        authBox.setEnabled(!running);
        tokenField.setEnabled(!running);
        debugBox.setEnabled(!running);
        if (running) {
            statusLabel.setText("运行中");
            statusLabel.setForeground(new Color(0x1B7F3A));
        } else {
            statusLabel.setText("已停止");
            statusLabel.setForeground(new Color(0xCC3333));
            sseLabel.setText("SSE 会话: 0");
            streamLabel.setText("Streamable: 0");
        }
    }

    private void refreshTools() {
        toolsModel.setRowCount(0);
        for (ToolDefinition def : ToolRegistry.getInstance().list()) {
            toolsModel.addRow(new Object[]{def.getName(),
                    def.getDescription() == null ? "" : def.getDescription()});
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private void appendLog(String level, String message) {
        String stamp = TIME_FMT.format(new Date());
        final String text = "[" + stamp + "] [" + level + "] " + message + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            // 控制日志长度
            int max = 200000;
            if (logArea.getDocument().getLength() > max) {
                try {
                    logArea.getDocument().remove(0, logArea.getDocument().getLength() - max);
                } catch (Exception ignored) {
                }
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // ------------------------------------------------------------
    // McpEventListener
    // ------------------------------------------------------------
    @Override
    public void onLog(String message) {
        appendLog("INFO ", message);
    }

    @Override
    public void onWarn(String message) {
        appendLog("WARN ", message);
    }

    @Override
    public void onError(String message) {
        appendLog("ERROR", message);
    }

    @Override
    public void onRequest(String transport, String method, boolean ok) {
        if (ok) {
            reqOk.incrementAndGet();
        } else {
            reqFail.incrementAndGet();
        }
        appendLog(ok ? "REQ  " : "REQ! ",
                "[" + transport + "] " + (method == null ? "(no method)" : method)
                        + (ok ? "" : "  -- failed"));
        SwingUtilities.invokeLater(() -> reqStatLabel.setText(
                "请求 " + reqOk.get() + " / 失败 " + reqFail.get()));
    }

    @Override
    public void onConnectionChanged(int sse, int streamable) {
        SwingUtilities.invokeLater(() -> {
            sseLabel.setText("SSE 会话: " + sse);
            streamLabel.setText("Streamable: " + streamable);
        });
    }
}
