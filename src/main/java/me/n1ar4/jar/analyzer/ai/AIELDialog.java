/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * "AI 生成 SpEL 表达式" 弹窗。
 * <p>
 * 嵌入到 EL Search 面板里，复用全局 AI 配置（{@link AIConfigManager}）。
 * 风格与 {@link AIChatDialog} 一致：浅色 LaF + 圆角 + 大间距。
 * <p>
 * 流程：用户输入意图 → 后台线程调用 {@link AIELGenerator} →
 * 结果显示在只读 RSyntax 区域，可一键插入到 EL 编辑器或复制。
 */
public class AIELDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger();

    private static final Color BG = new Color(0xFAFAFA);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(0xE5E7EB);
    private static final Color META = new Color(0x6B7280);
    private static final Color ACCENT = new Color(0x3A8DFF);

    private final JTextArea intentArea;
    private final RSyntaxTextArea resultArea;
    private final JButton generateBtn;
    private final JButton insertBtn;
    private final JButton copyBtn;
    private final JLabel statusLabel;
    private final JProgressBar busyBar;

    /**
     * 用户点击"插入到编辑器"时的回调，参数为最终的 SpEL 表达式
     */
    private final Consumer<String> onInsert;

    /**
     * 当前线程是否正在运行
     */
    private volatile Thread worker;

    private AIELDialog(Window owner, Consumer<String> onInsert) {
        super(owner, "AI 生成 EL 表达式", ModalityType.MODELESS);
        this.onInsert = onInsert;
        setSize(720, 600);
        setMinimumSize(new Dimension(600, 480));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 14, 12, 14));
        root.setBackground(BG);

        // ===== 顶部说明 =====
        JLabel title = new JLabel("用自然语言描述你想搜索的方法，AI 会生成 SpEL 表达式");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        JLabel subtitle = new JLabel("复用全局 AI 配置；输出的表达式可一键插入到 EL 编辑器");
        subtitle.setForeground(META);
        subtitle.setFont(subtitle.getFont().deriveFont(11f));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        head.add(title);
        head.add(Box.createVerticalStrut(2));
        head.add(subtitle);
        head.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(head, BorderLayout.NORTH);

        // ===== 中部：上下两段 =====
        intentArea = new JTextArea(5, 40);
        intentArea.setLineWrap(true);
        intentArea.setWrapStyleWord(true);
        intentArea.setFont(intentArea.getFont().deriveFont(13f));
        JScrollPane intentScroll = new JScrollPane(intentArea);
        intentScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        intentScroll.getViewport().setBackground(CARD_BG);

        JPanel intentBox = withCardLabel("需求描述（中文 / 英文均可）", intentScroll);

        resultArea = new RSyntaxTextArea();
        resultArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        resultArea.setEditable(false);
        resultArea.setCodeFoldingEnabled(false);
        resultArea.setAntiAliasingEnabled(true);
        resultArea.setHighlightCurrentLine(false);
        resultArea.setFont(resultArea.getFont().deriveFont(13f));
        resultArea.setText("// 生成结果将显示在这里");

        RTextScrollPane resultScroll = new RTextScrollPane(resultArea);
        resultScroll.setLineNumbersEnabled(true);
        resultScroll.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel resultBox = withCardLabel("生成的 SpEL 表达式", resultScroll);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, intentBox, resultBox);
        split.setResizeWeight(0.32);
        split.setDividerSize(6);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG);

        root.add(split, BorderLayout.CENTER);

        // ===== 底部按钮区 =====
        generateBtn = new JButton("生成", SvgManager.AiGenIcon);
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setBackground(ACCENT);
        generateBtn.setFocusPainted(false);
        generateBtn.setOpaque(true);
        generateBtn.setBorderPainted(false);
        generateBtn.addActionListener(e -> doGenerate());

        insertBtn = new JButton("插入到编辑器");
        insertBtn.setEnabled(false);
        insertBtn.addActionListener(e -> doInsert());

        copyBtn = new JButton("复制");
        copyBtn.setEnabled(false);
        copyBtn.addActionListener(e -> doCopy());

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(META);
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));

        busyBar = new JProgressBar();
        busyBar.setIndeterminate(true);
        busyBar.setVisible(false);
        busyBar.setPreferredSize(new Dimension(120, 6));

        JPanel southLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        southLeft.setOpaque(false);
        southLeft.add(busyBar);
        southLeft.add(statusLabel);

        JPanel southRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        southRight.setOpaque(false);
        southRight.add(generateBtn);
        southRight.add(insertBtn);
        southRight.add(copyBtn);
        southRight.add(closeBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(8, 0, 0, 0));
        south.add(southLeft, BorderLayout.WEST);
        south.add(southRight, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);

        // Ctrl+Enter 快捷生成
        intentArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()), "ai-generate");
        intentArea.getActionMap().put("ai-generate", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerate();
            }
        });

        // 关闭时取消正在运行的任务（仅做标记；OkHttp 同步 chat 不可中途取消，
        // 但 Dialog 已 dispose，不影响用户体验）
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                Thread t = worker;
                if (t != null) {
                    t.interrupt();
                }
            }
        });
    }

    private static JPanel withCardLabel(String label, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(META);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        l.setBorder(new EmptyBorder(0, 2, 4, 0));
        p.add(l, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void doGenerate() {
        if (worker != null && worker.isAlive()) {
            return;
        }
        String intent = intentArea.getText();
        if (intent == null || intent.trim().isEmpty()) {
            statusLabel.setText("请输入需求描述");
            return;
        }
        AIConfig cfg = AIConfigManager.load();
        if (!cfg.isReady()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "尚未启用任何 AI 配置。\n是否打开 AI 设置面板？",
                    "需要配置 AI",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.OK_OPTION) {
                AISettingsDialog.open();
            }
            return;
        }

        setBusy(true, "正在调用 AI 生成表达式…");
        resultArea.setText("// 等待模型响应…");
        insertBtn.setEnabled(false);
        copyBtn.setEnabled(false);

        worker = new Thread(() -> {
            final AIELGenerator.Result rs = AIELGenerator.generate(intent);
            SwingUtilities.invokeLater(() -> {
                setBusy(false, "");
                if (rs.ok) {
                    resultArea.setText(rs.expression);
                    resultArea.setCaretPosition(0);
                    insertBtn.setEnabled(true);
                    copyBtn.setEnabled(true);
                    statusLabel.setText("已生成，可一键插入到 EL 编辑器");
                } else {
                    String shownRaw = rs.rawResponse == null ? "" :
                            "\n\n// ---- 模型原始回复 ----\n// " +
                                    rs.rawResponse.replace("\n", "\n// ");
                    resultArea.setText("// 生成失败：" + rs.error + shownRaw);
                    statusLabel.setText("生成失败");
                }
            });
        }, "ai-el-gen");
        worker.setDaemon(true);
        worker.start();
    }

    private void setBusy(boolean busy, String msg) {
        busyBar.setVisible(busy);
        generateBtn.setEnabled(!busy);
        intentArea.setEnabled(!busy);
        if (msg != null) {
            statusLabel.setText(msg);
        }
    }

    private void doInsert() {
        String text = resultArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        if (onInsert != null) {
            try {
                onInsert.accept(text);
            } catch (Throwable ex) {
                logger.error("AI EL insert callback error: {}", ex.toString());
            }
        }
        statusLabel.setText("已插入到 EL 编辑器");
    }

    private void doCopy() {
        StringSelection sel = new StringSelection(resultArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        statusLabel.setText("已复制到剪贴板");
    }

    /**
     * 打开 AI EL 生成弹窗（兼容旧入口：以 MainForm 作为 owner）。
     *
     * @param onInsert 当用户点击"插入到编辑器"时的回调；通常把表达式写入 ELPanel.codeArea
     */
    public static void open(Consumer<String> onInsert) {
        open(null, onInsert);
    }

    /**
     * 打开 AI EL 生成弹窗，并以 {@code anchor} 所在窗口作为 owner。
     * 这样弹窗会稳定浮在 EL 面板所在的窗口之上，避免被触发它的子窗口反盖。
     */
    public static void open(Component anchor, Consumer<String> onInsert) {
        Window owner = null;
        if (anchor != null) {
            owner = SwingUtilities.getWindowAncestor(anchor);
        }
        if (owner == null && MainForm.getInstance() != null) {
            owner = SwingUtilities.getWindowAncestor(MainForm.getInstance().getMasterPanel());
        }
        AIELDialog dlg = new AIELDialog(owner, onInsert);
        // AI 弹窗强制浅色主题：不受用户当前主题影响
        LightLafContext.applyLightTo(dlg);
        dlg.setVisible(true);
        dlg.toFront();
        dlg.requestFocus();
    }
}
