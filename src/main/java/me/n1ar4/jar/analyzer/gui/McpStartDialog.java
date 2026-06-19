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

import me.n1ar4.jar.analyzer.mcp.McpStartStage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * MCP 启动进度对话框
 *
 * <p>展示 MCP 启动各阶段的进度，启动完成后自动消失</p>
 *
 * <p>使用方式：</p>
 * <pre>
 *   McpStartDialog dlg = new McpStartDialog(parent);
 *   dlg.showAsync();   // 非阻塞展示
 *   // 在另一个线程执行启动，通过 dlg.onStage(...) 同步进度
 *   dlg.markDone();    // 启动完成后调用，对话框 1s 后自动关闭
 *   // 或者
 *   dlg.markFailed(msg);
 * </pre>
 */
public class McpStartDialog extends JDialog {

    private static final Color BG_HEADER = new Color(0xF7F9FF);
    private static final Color C_PRIMARY = new Color(0x2E5BFF);
    private static final Color C_SUCCESS = new Color(0x1B7F3A);
    private static final Color C_FAIL = new Color(0xCC3333);
    private static final Color C_DIM = new Color(0x9AA0A6);
    private static final Color C_TEXT = new Color(0x222222);

    private final JLabel headerLabel = new JLabel();
    private final JLabel subLabel = new JLabel();
    private final JProgressBar bar = new JProgressBar(0, McpStartStage.values().length);
    private final Map<McpStartStage, JLabel> stageLabels = new EnumMap<>(McpStartStage.class);

    private volatile McpStartStage current = null;
    private volatile boolean failed = false;

    public McpStartDialog(Window owner) {
        super(owner, "MCP 启动中", ModalityType.MODELESS);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        buildUi();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // -- header --
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(14, 18, 12, 18));
        headerLabel.setText("正在启动 MCP 服务器…");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 15f));
        headerLabel.setForeground(C_PRIMARY);
        subLabel.setText("请稍候，正在执行启动流程");
        subLabel.setForeground(C_DIM);
        subLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        JPanel headerInner = new JPanel(new GridLayout(2, 1));
        headerInner.setOpaque(false);
        headerInner.add(headerLabel);
        headerInner.add(subLabel);
        header.add(headerInner, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // -- center: stages --
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(14, 22, 4, 22));

        for (McpStartStage stage : McpStartStage.values()) {
            JLabel l = new JLabel(itemText(stage, false, false, false));
            l.setForeground(C_DIM);
            l.setBorder(new EmptyBorder(3, 0, 3, 0));
            stageLabels.put(stage, l);
            center.add(l);
        }
        root.add(center, BorderLayout.CENTER);

        // -- bottom: progress bar --
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(8, 22, 16, 22));
        bar.setStringPainted(true);
        bar.setString("0 / " + McpStartStage.values().length);
        bar.setForeground(C_PRIMARY);
        bottom.add(bar, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(440, 320));
    }

    private String itemText(McpStartStage stage, boolean active, boolean done, boolean failed) {
        String marker;
        String color;
        if (done) {
            marker = "✓";
            color = String.format("#%02x%02x%02x", C_SUCCESS.getRed(), C_SUCCESS.getGreen(), C_SUCCESS.getBlue());
        } else if (active && failed) {
            marker = "✗";
            color = String.format("#%02x%02x%02x", C_FAIL.getRed(), C_FAIL.getGreen(), C_FAIL.getBlue());
        } else if (active) {
            marker = "▸";
            color = String.format("#%02x%02x%02x", C_PRIMARY.getRed(), C_PRIMARY.getGreen(), C_PRIMARY.getBlue());
        } else {
            marker = "○";
            color = String.format("#%02x%02x%02x", C_DIM.getRed(), C_DIM.getGreen(), C_DIM.getBlue());
        }
        return "<html><span style='color:" + color + "; font-weight:" + (active ? "bold" : "normal") + ";'>"
                + marker + "&nbsp;&nbsp;" + stage.getDescription() + "</span></html>";
    }

    /**
     * 异步显示窗口（不阻塞调用线程）
     */
    public void showAsync() {
        if (SwingUtilities.isEventDispatchThread()) {
            setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> setVisible(true));
        }
    }

    /**
     * 进度回调（线程安全，可在任意线程调用）
     */
    public void onStage(McpStartStage stage) {
        SwingUtilities.invokeLater(() -> {
            // 之前的标记为完成
            if (current != null) {
                JLabel prev = stageLabels.get(current);
                if (prev != null) {
                    prev.setText(itemText(current, false, true, false));
                    prev.setForeground(C_TEXT);
                }
            }
            // 当前
            current = stage;
            JLabel cur = stageLabels.get(stage);
            if (cur != null) {
                cur.setText(itemText(stage, true, false, false));
                cur.setForeground(C_PRIMARY);
            }
            // 进度条
            int v = stage.order();
            bar.setValue(v);
            bar.setString(v + " / " + stage.total());
            subLabel.setText("当前阶段：" + stage.getDescription());
        });
    }

    /**
     * 标记成功，对话框 800ms 后自动关闭
     */
    public void markDone() {
        SwingUtilities.invokeLater(() -> {
            // 把最后一个阶段也置为完成
            if (current != null) {
                JLabel last = stageLabels.get(current);
                if (last != null) {
                    last.setText(itemText(current, false, true, false));
                    last.setForeground(C_TEXT);
                }
            }
            bar.setValue(bar.getMaximum());
            bar.setString("完成");
            headerLabel.setForeground(C_SUCCESS);
            headerLabel.setText("✓ MCP 启动成功");
            subLabel.setForeground(C_SUCCESS);
            subLabel.setText("窗口将自动关闭");

            Timer t = new Timer(800, e -> dispose());
            t.setRepeats(false);
            t.start();
        });
    }

    /**
     * 标记失败，等待用户关闭
     */
    public void markFailed(String message) {
        failed = true;
        SwingUtilities.invokeLater(() -> {
            if (current != null) {
                JLabel cur = stageLabels.get(current);
                if (cur != null) {
                    cur.setText(itemText(current, true, false, true));
                    cur.setForeground(C_FAIL);
                }
            }
            bar.setForeground(C_FAIL);
            bar.setString("失败");
            headerLabel.setForeground(C_FAIL);
            headerLabel.setText("✗ MCP 启动失败");
            subLabel.setForeground(C_FAIL);
            subLabel.setText(message == null ? "未知错误" : message);

            // 添加关闭按钮
            JPanel close = new JPanel();
            close.setBorder(new EmptyBorder(0, 22, 14, 22));
            JButton btn = new JButton("关闭");
            btn.addActionListener(e -> dispose());
            close.add(btn);
            getContentPane().add(close, BorderLayout.PAGE_END);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            pack();
        });
    }

    public boolean isFailed() {
        return failed;
    }
}
