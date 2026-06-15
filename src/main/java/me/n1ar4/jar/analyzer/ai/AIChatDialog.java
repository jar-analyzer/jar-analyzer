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

import com.github.rjeschke.txtmark.Processor;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 现代化 AI 对话窗口
 * <p>
 * 设计参考主流 AI 助手工具：气泡消息流、流式光标、空状态引导、
 * 顶部紧凑工具栏、Enter 发送 / Shift+Enter 换行。
 */
public class AIChatDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger();

    // 配色（与浅/深主题都协调的中性配色，FlatLaf 适配）
    private static final Color USER_BUBBLE = new Color(0xFFEDD5);     // 暖橙浅
    private static final Color ASSIST_BUBBLE = new Color(0xF1F3F5);   // 中性浅灰
    private static final Color ASSIST_BUBBLE_DARK = new Color(0x2A2D31);
    private static final Color ERROR_BUBBLE = new Color(0xFEE2E2);
    private static final Color USER_TEXT = new Color(0x7C2D12);
    private static final Color ASSIST_TEXT = new Color(0x111827);
    private static final Color ASSIST_TEXT_DARK = new Color(0xE5E7EB);
    private static final Color ERROR_TEXT = new Color(0xB91C1C);
    private static final Color META_TEXT = new Color(0x6B7280);
    private static final Color DOT_GREEN = new Color(0x10B981);
    private static final Color DOT_AMBER = new Color(0xF59E0B);
    private static final Color DOT_RED = new Color(0xEF4444);

    // 顶部
    private final StatusDot statusDot;
    private final JLabel statusLabel;

    // 主消息流
    private final ChatMessagesPanel messagesPanel;
    private final JScrollPane messagesScroll;

    // 底部输入区
    private final JTextArea inputArea;
    private final JButton sendBtn;
    private final JButton stopBtn;
    private final JLabel inputMetaLabel = new JLabel(" ");
    private final JLabel sessionMetaLabel = new JLabel(" ");

    // 会话累积 token（粗略估计）
    private int sessionTokens = 0;

    // 业务
    private final List<LLMClient.ChatMessage> history = new ArrayList<>();
    private LLMClient.CancelHandle currentHandle;
    private MessageBubble pendingAssistant;        // 当前正在流式接收的助手气泡
    private final StringBuilder pendingAcc = new StringBuilder();

    public AIChatDialog(Window owner) {
        super(owner, "AI 助手", ModalityType.MODELESS);
        setSize(960, 720);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // ===== 顶部 toolbar =====
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(new EmptyBorder(8, 16, 8, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        statusDot = new StatusDot();
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(META_TEXT);
        left.add(statusDot);
        left.add(statusLabel);
        toolbar.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        sessionMetaLabel.setForeground(META_TEXT);
        sessionMetaLabel.setFont(sessionMetaLabel.getFont().deriveFont(11f));
        center.add(sessionMetaLabel);
        toolbar.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JButton clearBtn = makeFlatButton("清空", null);
        clearBtn.addActionListener(e -> doClear());
        JButton settingsBtn = makeFlatButton("设置", SvgManager.AiSettingsIcon);
        settingsBtn.addActionListener(e -> {
            AISettingsDialog.open();
            refreshStatus();
        });
        right.add(clearBtn);
        right.add(settingsBtn);
        toolbar.add(right, BorderLayout.EAST);

        toolbar.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
        root.add(toolbar, BorderLayout.NORTH);

        // ===== 中部消息流 =====
        messagesPanel = new ChatMessagesPanel();
        messagesScroll = new JScrollPane(messagesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScroll.setBorder(BorderFactory.createEmptyBorder());
        messagesScroll.getVerticalScrollBar().setUnitIncrement(24);
        root.add(messagesScroll, BorderLayout.CENTER);

        // ===== 底部输入 =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(8, 16, 14, 16));

        JPanel composer = new JPanel(new BorderLayout(8, 8));
        composer.setBorder(new RoundedBorder(14, new Color(0xD1D5DB)));

        inputArea = new JTextArea(3, 50);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        inputArea.setOpaque(true);
        inputArea.setBackground(Color.WHITE);
        inputArea.setForeground(new Color(0x111827));
        inputArea.setCaretColor(new Color(0x111827));
        inputArea.setFont(inputArea.getFont().deriveFont(14f));

        // 占位符
        new PlaceholderPainter(inputArea, "输入消息，Enter 发送，Shift+Enter 换行");

        JScrollPane inScroll = new JScrollPane(inputArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        inScroll.setBorder(BorderFactory.createEmptyBorder());
        inScroll.setOpaque(false);
        inScroll.getViewport().setOpaque(false);
        inScroll.setPreferredSize(new Dimension(0, 96));
        composer.add(inScroll, BorderLayout.CENTER);

        // 输入区下方：左侧字符/token 估计；右侧 Stop / Send
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(0, 6, 4, 6));

        inputMetaLabel.setForeground(META_TEXT);
        inputMetaLabel.setFont(inputMetaLabel.getFont().deriveFont(11f));
        actionRow.add(inputMetaLabel, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        stopBtn = makeFlatButton("停止", null);
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> doStop());
        sendBtn = makePrimaryButton("发送 ⏎");
        sendBtn.addActionListener(e -> doSend());
        btnRow.add(stopBtn);
        btnRow.add(sendBtn);
        actionRow.add(btnRow, BorderLayout.EAST);
        composer.add(actionRow, BorderLayout.SOUTH);

        bottom.add(composer, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        // 输入框文档变化时刷新字符 / token 估计
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshInputMeta();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshInputMeta();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshInputMeta();
            }
        });
        refreshInputMeta();

        // 键盘绑定：Enter 发送 / Shift+Enter 换行 / Ctrl+Enter 也兼容
        bindKeys();

        refreshStatus();
        showEmptyStateIfNeeded();
    }

    // ---------------- 顶部状态 ----------------

    private void refreshStatus() {
        AIConfig cfg = AIConfigManager.load();
        if (!cfg.isReady()) {
            statusDot.setColor(DOT_RED);
            statusLabel.setText("未启用 AI 配置 · 请点击右上角「设置」");
            statusLabel.setForeground(DOT_RED);
            sendBtn.setEnabled(false);
        } else {
            statusDot.setColor(DOT_GREEN);
            String name = cfg.getName() == null || cfg.getName().isEmpty() ? "未命名" : cfg.getName();
            statusLabel.setText(String.format("%s · %s · %s",
                    name, cfg.getProvider(), cfg.getModel()));
            statusLabel.setForeground(META_TEXT);
            sendBtn.setEnabled(true);
        }
    }

    /**
     * 输入字符 / token 估算
     */
    private void refreshInputMeta() {
        String text = inputArea.getText();
        int chars = text == null ? 0 : text.length();
        int tokens = estimateTokens(text);
        inputMetaLabel.setText(String.format("%d chars · ~%d tokens", chars, tokens));
    }

    private void refreshSessionMeta() {
        if (sessionTokens <= 0) {
            sessionMetaLabel.setText(" ");
        } else {
            sessionMetaLabel.setText(String.format("会话累计 ≈ %s tokens",
                    formatCompact(sessionTokens)));
        }
    }

    private static String formatCompact(int n) {
        if (n < 1000) {
            return String.valueOf(n);
        }
        if (n < 1_000_000) {
            return String.format("%.1fK", n / 1000.0);
        }
        return String.format("%.1fM", n / 1_000_000.0);
    }

    /**
     * 简化版 token 估算（无外部依赖）：
     * - ASCII 可见字符按 1 token / 4 chars
     * - 其他（中文 / 日文 / 韩文 等 CJK）按 1 token / 1.5 chars
     * <p>
     * 真实 BPE 数会有 10~20% 偏差，但作为输入指示已足够。
     */
    static int estimateTokens(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        int ascii = 0;
        int cjk = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x80) {
                ascii++;
            } else {
                cjk++;
            }
        }
        return (int) Math.ceil(ascii / 4.0 + cjk / 1.5);
    }

    // ---------------- 业务动作 ----------------

    private void doClear() {
        if (currentHandle != null) {
            currentHandle.cancel();
        }
        history.clear();
        sessionTokens = 0;
        refreshSessionMeta();
        messagesPanel.clearAll();
        showEmptyStateIfNeeded();
        refreshStatus();
    }

    private void doStop() {
        if (currentHandle != null) {
            currentHandle.cancel();
        }
        if (pendingAssistant != null) {
            pendingAssistant.appendText("\n\n[已停止]");
            pendingAssistant.markDone();
            // 把已生成的部分写入 history（即使被中断）
            if (pendingAcc.length() > 0) {
                history.add(LLMClient.ChatMessage.assistant(pendingAcc.toString()));
                sessionTokens += estimateTokens(pendingAcc.toString());
                refreshSessionMeta();
            }
            pendingAssistant = null;
            pendingAcc.setLength(0);
        }
        sendBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        statusDot.setColor(DOT_AMBER);
        statusLabel.setText("已停止");
    }

    private void doSend() {
        AIConfig cfg = AIConfigManager.load();
        if (!cfg.isReady()) {
            JOptionPane.showMessageDialog(this, "请先在「设置」中配置 Provider / API Key");
            return;
        }
        String userInput = inputArea.getText();
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }
        inputArea.setText("");
        messagesPanel.removeEmptyState();

        // 添加 user 气泡
        messagesPanel.addBubble(new MessageBubble(MessageBubble.Role.USER, userInput));

        // 准备发送列表：system 始终位于最前，保证多轮对话也保留角色设定
        List<LLMClient.ChatMessage> messages = new ArrayList<>();
        if (cfg.getSystemPrompt() != null && !cfg.getSystemPrompt().isEmpty()) {
            messages.add(LLMClient.ChatMessage.system(cfg.getSystemPrompt()));
        }
        messages.addAll(history);
        messages.add(LLMClient.ChatMessage.user(userInput));
        history.add(LLMClient.ChatMessage.user(userInput));

        // 累计输入 token（粗估）
        sessionTokens += estimateTokens(userInput);
        refreshSessionMeta();

        // 添加 assistant 占位气泡（带"思考中"效果）
        pendingAssistant = new MessageBubble(MessageBubble.Role.ASSISTANT, "");
        pendingAssistant.markPending();
        messagesPanel.addBubble(pendingAssistant);
        pendingAcc.setLength(0);

        sendBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        statusDot.setColor(DOT_AMBER);
        statusLabel.setText("生成中…");

        scrollToBottomLater();

        LLMClient client = new LLMClient(cfg);

        if (cfg.isStream()) {
            currentHandle = client.chatStream(messages, new LLMClient.StreamListener() {
                @Override
                public void onDelta(String delta) {
                    SwingUtilities.invokeLater(() -> {
                        if (pendingAssistant == null) {
                            return;
                        }
                        pendingAcc.append(delta);
                        pendingAssistant.appendText(delta);
                        scrollToBottomLater();
                    });
                }

                @Override
                public void onDone() {
                    SwingUtilities.invokeLater(() -> finishAssistant(false, null));
                }

                @Override
                public void onError(Throwable t) {
                    SwingUtilities.invokeLater(() -> finishAssistant(true, safeMsg(t)));
                    logger.error("ai chat error: {}", t.toString());
                }
            });
        } else {
            new Thread(() -> {
                try {
                    String text = client.chat(messages);
                    final String s = text == null ? "" : text;
                    SwingUtilities.invokeLater(() -> {
                        if (pendingAssistant != null) {
                            pendingAcc.append(s);
                            pendingAssistant.appendText(s);
                        }
                        finishAssistant(false, null);
                    });
                } catch (Throwable t) {
                    SwingUtilities.invokeLater(() -> finishAssistant(true, safeMsg(t)));
                    logger.error("ai chat error: {}", t.toString());
                }
            }, "ai-chat").start();
        }
    }

    private void finishAssistant(boolean error, String errMsg) {
        if (pendingAssistant != null) {
            if (error) {
                pendingAssistant.markError(errMsg == null ? "请求失败" : errMsg);
            } else {
                pendingAssistant.markDone();
                history.add(LLMClient.ChatMessage.assistant(pendingAcc.toString()));
                // 累计输出 token
                sessionTokens += estimateTokens(pendingAcc.toString());
                refreshSessionMeta();
            }
        }
        pendingAssistant = null;
        pendingAcc.setLength(0);
        sendBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        if (error) {
            statusDot.setColor(DOT_RED);
            statusLabel.setText("出错");
        } else {
            statusDot.setColor(DOT_GREEN);
            statusLabel.setText("就绪");
        }
        scrollToBottomLater();
    }

    private void scrollToBottomLater() {
        SwingUtilities.invokeLater(() -> {
            messagesPanel.revalidate();
            JScrollBar bar = messagesScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ---------------- 键位 ----------------

    private void bindKeys() {
        InputMap im = inputArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = inputArea.getActionMap();

        // Enter -> 发送（避开 Shift+Enter）
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ai-send");
        am.put("ai-send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSend();
            }
        });

        // Ctrl+Enter 也发送（兼容老习惯）
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "ai-send");

        // Shift+Enter 插入换行（保持默认行为：让 inputArea 处理）
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                java.awt.event.InputEvent.SHIFT_DOWN_MASK), "insert-break");
    }

    // ---------------- 空状态 ----------------

    private void showEmptyStateIfNeeded() {
        if (messagesPanel.bubbleCount() == 0) {
            messagesPanel.showEmptyState();
        }
    }

    // ---------------- 静态入口 ----------------

    /**
     * 解析"应作为弹窗 owner 的 Window"。优先使用 anchor 所属的 Window，
     * 这样从 EL / Diff / 调用链等子窗口触发 AI 时，弹窗会浮在触发源之上，
     * 不会被反复挡到主窗口下层。
     */
    private static Window resolveOwner(Component anchor) {
        if (anchor != null) {
            Window w = SwingUtilities.getWindowAncestor(anchor);
            if (w != null) {
                return w;
            }
        }
        return MainForm.getInstance() != null ? SwingUtilities.getWindowAncestor(
                MainForm.getInstance().getMasterPanel()) : null;
    }

    private static void showOnTop(AIChatDialog dlg) {
        LightLafContext.applyLightTo(dlg);
        dlg.setVisible(true);
        // 进入前台，避免在多窗口场景下被触发源窗口反盖
        dlg.toFront();
        dlg.requestFocus();
    }

    public static void open() {
        open((Component) null);
    }

    /**
     * 以 {@code anchor} 所在窗口为 owner 打开 AI 对话框。
     */
    public static void open(Component anchor) {
        AIChatDialog dlg = new AIChatDialog(resolveOwner(anchor));
        showOnTop(dlg);
    }

    /**
     * 带初始上下文打开（如：右键反编译代码 → AI 解释）
     */
    public static void openWithPrompt(String preset) {
        openWithPrompt(null, preset);
    }

    /**
     * 带初始上下文 + 触发源组件打开。owner 取 anchor 所在的窗口，
     * 弹窗会稳定浮在该子窗口之上。
     */
    public static void openWithPrompt(Component anchor, String preset) {
        AIChatDialog dlg = new AIChatDialog(resolveOwner(anchor));
        if (preset != null) {
            dlg.inputArea.setText(preset);
        }
        showOnTop(dlg);
    }

    private static String safeMsg(Throwable t) {
        String m = t.getMessage();
        return m == null ? t.toString() : m;
    }

    // ============================================================
    //                          内部组件
    // ============================================================

    /**
     * 消息流容器（垂直 BoxLayout）
     */
    private static class ChatMessagesPanel extends JPanel implements Scrollable {
        private final JPanel emptyState;

        ChatMessagesPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(16, 24, 16, 24));
            setBackground(new Color(0xFAFAFA));
            emptyState = buildEmptyState();
        }

        private static JPanel buildEmptyState() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(80, 0, 0, 0));

            JLabel title = new JLabel("AI 助手");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
            title.setForeground(new Color(0x111827));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel sub = new JLabel("Java 安全研究与代码审计 · 由你启用的模型驱动");
            sub.setForeground(META_TEXT);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            sub.setBorder(new EmptyBorder(8, 0, 24, 0));

            JPanel tips = new JPanel(new GridLayout(2, 2, 12, 12));
            tips.setOpaque(false);
            tips.setMaximumSize(new Dimension(720, 160));
            tips.setAlignmentX(Component.CENTER_ALIGNMENT);
            tips.add(buildTipCard("解释代码", "粘贴一段反编译代码，问它在做什么"));
            tips.add(buildTipCard("漏洞研判", "粘贴 source→sink 调用链，让 AI 判可达性"));
            tips.add(buildTipCard("生成表达式", "用自然语言描述要找的方法，生成 SpEL"));
            tips.add(buildTipCard("补丁分析", "粘贴 JAR DIFF，识别是否为安全修复"));

            p.add(title);
            p.add(sub);
            p.add(tips);
            return p;
        }

        private static JPanel buildTipCard(String title, String desc) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(10, new Color(0xE5E7EB)),
                    new EmptyBorder(12, 14, 12, 14)));
            card.setBackground(Color.WHITE);
            JLabel t = new JLabel(title);
            t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));
            t.setForeground(new Color(0x111827));
            JLabel d = new JLabel("<html><div style='color:#6B7280'>" + desc + "</div></html>");
            d.setBorder(new EmptyBorder(4, 0, 0, 0));
            card.add(t);
            card.add(d);
            return card;
        }

        void showEmptyState() {
            removeAll();
            add(emptyState);
            revalidate();
            repaint();
        }

        void removeEmptyState() {
            for (int i = 0; i < getComponentCount(); i++) {
                if (getComponent(i) == emptyState) {
                    remove(i);
                    revalidate();
                    repaint();
                    return;
                }
            }
        }

        void addBubble(MessageBubble bubble) {
            removeEmptyState();
            // 在添加前留间距
            if (bubbleCount() > 0) {
                add(Box.createVerticalStrut(10));
            }
            add(bubble);
            revalidate();
            repaint();
        }

        int bubbleCount() {
            int n = 0;
            for (Component c : getComponents()) {
                if (c instanceof MessageBubble) {
                    n++;
                }
            }
            return n;
        }

        void clearAll() {
            removeAll();
            revalidate();
            repaint();
        }

        // ---- Scrollable: 让面板宽度跟随视口、不出现水平滚动 ----
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * 单条消息气泡
     */
    private static class MessageBubble extends JPanel {
        enum Role {USER, ASSISTANT}

        private final Role role;
        private final JTextArea textArea;
        private final JLabel metaLabel;
        private final JButton copyBtn;
        private final JPanel contentBubble;
        private boolean pending;

        MessageBubble(Role role, String initial) {
            this.role = role;
            setOpaque(false);
            setLayout(new BorderLayout());
            setAlignmentX(Component.LEFT_ALIGNMENT);

            // 头像
            JLabel avatar = new JLabel(role == Role.USER ? "U" : "AI", SwingConstants.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(role == Role.USER ? new Color(0xFB923C) : new Color(0x6366F1));
            avatar.setForeground(Color.WHITE);
            avatar.setFont(avatar.getFont().deriveFont(Font.BOLD, 12f));
            avatar.setPreferredSize(new Dimension(28, 28));
            avatar.setBorder(new RoundedBorder(14, role == Role.USER ? new Color(0xFB923C) : new Color(0x6366F1)));

            // 文本（用 JTextArea 便于流式 append + 选择/复制）
            textArea = new JTextArea(initial == null ? "" : initial);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setOpaque(false);
            textArea.setBorder(null);
            textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            textArea.setForeground(role == Role.USER ? USER_TEXT : ASSIST_TEXT);
            textArea.setMargin(new Insets(0, 0, 0, 0));

            // 气泡
            contentBubble = new JPanel(new BorderLayout(0, 4));
            contentBubble.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(12,
                            role == Role.USER ? new Color(0xFDBA74) : new Color(0xE5E7EB)),
                    new EmptyBorder(10, 14, 10, 14)));
            contentBubble.setBackground(role == Role.USER ? USER_BUBBLE : ASSIST_BUBBLE);

            contentBubble.add(textArea, BorderLayout.CENTER);

            // meta 行（角色标签 + 复制按钮）
            JPanel metaRow = new JPanel(new BorderLayout());
            metaRow.setOpaque(false);
            metaLabel = new JLabel(role == Role.USER ? "You" : "Assistant");
            metaLabel.setForeground(META_TEXT);
            metaLabel.setFont(metaLabel.getFont().deriveFont(11f));
            metaRow.add(metaLabel, BorderLayout.WEST);

            copyBtn = new JButton("复制");
            copyBtn.setBorder(new EmptyBorder(0, 6, 0, 6));
            copyBtn.setFocusPainted(false);
            copyBtn.setContentAreaFilled(false);
            copyBtn.setForeground(META_TEXT);
            copyBtn.setFont(copyBtn.getFont().deriveFont(11f));
            copyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            copyBtn.setVisible(false);
            copyBtn.addActionListener(e -> {
                String t = textArea.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(t == null ? "" : t), null);
                copyBtn.setText("已复制");
                Timer timer = new Timer(1200, ev -> copyBtn.setText("复制"));
                timer.setRepeats(false);
                timer.start();
            });
            metaRow.add(copyBtn, BorderLayout.EAST);

            // 整体布局：头像 + （meta + 气泡）
            JPanel column = new JPanel(new BorderLayout());
            column.setOpaque(false);
            column.add(metaRow, BorderLayout.NORTH);
            column.add(contentBubble, BorderLayout.CENTER);

            JPanel avatarBox = new JPanel(new BorderLayout());
            avatarBox.setOpaque(false);
            avatarBox.setBorder(new EmptyBorder(18, 0, 0, 8));
            avatarBox.add(avatar, BorderLayout.NORTH);

            if (role == Role.USER) {
                add(avatarBox, BorderLayout.EAST);
                add(column, BorderLayout.CENTER);
                avatarBox.setBorder(new EmptyBorder(18, 8, 0, 0));
            } else {
                add(avatarBox, BorderLayout.WEST);
                add(column, BorderLayout.CENTER);
            }

            // 用户消息显示 hover 复制
            if (role == Role.USER) {
                copyBtn.setVisible(false);
            }

            // 鼠标进入显示复制按钮
            MouseAdapter hover = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!pending) {
                        copyBtn.setVisible(true);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Point p = SwingUtilities.convertPoint(
                            (Component) e.getSource(), e.getPoint(), MessageBubble.this);
                    if (!MessageBubble.this.contains(p)) {
                        copyBtn.setVisible(false);
                    }
                }
            };
            contentBubble.addMouseListener(hover);
            textArea.addMouseListener(hover);
            metaRow.addMouseListener(hover);
        }

        void appendText(String s) {
            if (s == null || s.isEmpty()) {
                return;
            }
            textArea.append(s);
        }

        void markPending() {
            pending = true;
            metaLabel.setText(role == Role.USER ? "You" : "Assistant · 思考中…");
            if (textArea.getText().isEmpty()) {
                textArea.setText("▍");
            }
        }

        void markDone() {
            pending = false;
            // 若文字以光标占位符开头则去掉
            String t = textArea.getText();
            if (t != null && t.startsWith("▍")) {
                t = t.substring(1);
                textArea.setText(t);
            }
            metaLabel.setText(role == Role.USER ? "You" : "Assistant");
            // 助手消息：用 markdown 渲染替换纯文本
            if (role == Role.ASSISTANT && t != null && !t.isEmpty()) {
                renderMarkdown(t);
            }
        }

        /**
         * 将 textArea 内容渲染为 markdown HTML，并替换显示组件
         */
        private void renderMarkdown(String md) {
            try {
                String html = Processor.process(md);
                JEditorPane pane = new JEditorPane();
                pane.setContentType("text/html");
                pane.setEditable(false);
                pane.setOpaque(false);
                pane.setBorder(null);
                pane.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                // 自定义 stylesheet：代码块 / 行内代码 / 列表 / 标题
                String wrapped =
                        "<html><head><style>" +
                        "body{font-family:'Microsoft YaHei','PingFang SC','Helvetica Neue',Arial,sans-serif;" +
                        "font-size:13px;color:#111827;margin:0;padding:0;line-height:1.55;}" +
                        "h1,h2,h3,h4{color:#111827;margin:8px 0 4px 0;}" +
                        "h1{font-size:16px;}h2{font-size:15px;}h3{font-size:14px;}" +
                        "p{margin:4px 0;}" +
                        "code{background-color:#EEF1F4;color:#B91C1C;padding:1px 4px;" +
                        "border-radius:3px;font-family:'Consolas','Menlo',monospace;font-size:12px;}" +
                        "pre{background-color:#F3F4F6;border:1px solid #E5E7EB;border-radius:6px;" +
                        "padding:8px 10px;margin:6px 0;font-family:'Consolas','Menlo',monospace;" +
                        "font-size:12px;overflow:auto;}" +
                        "pre code{background:none;color:#111827;padding:0;}" +
                        "ul,ol{margin:4px 0 4px 22px;padding:0;}" +
                        "li{margin:2px 0;}" +
                        "blockquote{border-left:3px solid #D1D5DB;color:#6B7280;margin:6px 0;padding:0 10px;}" +
                        "a{color:#2563EB;text-decoration:none;}" +
                        "table{border-collapse:collapse;margin:6px 0;}" +
                        "th,td{border:1px solid #E5E7EB;padding:4px 8px;}" +
                        "</style></head><body>" + html + "</body></html>";
                pane.setText(wrapped);
                pane.setCaretPosition(0);

                // 替换 contentBubble 中的 textArea 为 pane
                contentBubble.removeAll();
                contentBubble.add(pane, BorderLayout.CENTER);
                contentBubble.revalidate();
                contentBubble.repaint();

                // 复制按钮取的也要换为 markdown 原文
                rebindCopyToMarkdown(md);
            } catch (Throwable ex) {
                // 渲染失败保留纯文本
                logger.debug("markdown render failed: {}", ex.toString());
            }
        }

        private void rebindCopyToMarkdown(String md) {
            // 替换 copyBtn 的 ActionListener，使其复制 markdown 原文
            for (java.awt.event.ActionListener l : copyBtn.getActionListeners()) {
                copyBtn.removeActionListener(l);
            }
            copyBtn.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(md == null ? "" : md), null);
                copyBtn.setText("已复制");
                Timer timer = new Timer(1200, ev -> copyBtn.setText("复制"));
                timer.setRepeats(false);
                timer.start();
            });
        }

        void markError(String msg) {
            pending = false;
            contentBubble.setBackground(ERROR_BUBBLE);
            contentBubble.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(12, new Color(0xFCA5A5)),
                    new EmptyBorder(10, 14, 10, 14)));
            textArea.setForeground(ERROR_TEXT);
            String prev = textArea.getText();
            if (prev.startsWith("▍")) {
                prev = prev.substring(1);
            }
            textArea.setText((prev == null || prev.isEmpty() ? "" : prev + "\n\n") + "⚠ 请求失败：" + msg);
            metaLabel.setText("Assistant · 错误");
            metaLabel.setForeground(ERROR_TEXT);
        }

        @Override
        public Dimension getMaximumSize() {
            // 限制宽度撑满，但不超出，避免 BoxLayout 过度拉伸
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    }

    /**
     * 顶部状态点
     */
    private static class StatusDot extends JComponent {
        private Color color = new Color(0x9CA3AF);

        StatusDot() {
            setPreferredSize(new Dimension(10, 10));
        }

        void setColor(Color c) {
            this.color = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int s = Math.min(getWidth(), getHeight()) - 2;
            g2.fillOval((getWidth() - s) / 2, (getHeight() - s) / 2, s, s);
            g2.dispose();
        }
    }

    /**
     * 圆角描边 Border
     */
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 背景填充（让父容器的背景色生效到圆角）
            if (c.isOpaque()) {
                g2.setColor(c.getParent() != null ? c.getParent().getBackground() : Color.WHITE);
                g2.fillRect(x, y, width, height);
            }
            // 填充自身背景
            g2.setColor(c.getBackground());
            g2.fillRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
            // 描边
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }
    }

    /**
     * JTextArea 占位符
     */
    private static class PlaceholderPainter {
        PlaceholderPainter(JTextArea area, String text) {
            area.putClientProperty("JTextField.placeholderText", text);
            // FlatLaf 不一定对 JTextArea 生效，加一个自绘后备
            area.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    area.repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    area.repaint();
                }
            });
            area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    area.repaint();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    area.repaint();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    area.repaint();
                }
            });
            // 简单覆盖 paint
            area.setUI(new javax.swing.plaf.basic.BasicTextAreaUI() {
                @Override
                protected void paintSafely(Graphics g) {
                    super.paintSafely(g);
                    if (area.getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0x9CA3AF));
                        g2.setFont(area.getFont());
                        Insets ins = area.getInsets();
                        g2.drawString(text, ins.left + 2, ins.top + g2.getFontMetrics().getAscent());
                        g2.dispose();
                    }
                }
            });
        }
    }

    // ---------------- 按钮工厂 ----------------

    /**
     * 自绘圆角按钮：脱离 LaF ButtonUI，避免 FlatLaf 暗色主题下按钮文字/背景错乱
     */
    private static class RoundedBtn extends JButton {
        private final Color bg;
        private final Color fg;
        private final boolean primary;

        RoundedBtn(String text, Icon icon, Color bg, Color fg, boolean primary) {
            super(text, icon);
            this.bg = bg;
            this.fg = fg;
            this.primary = primary;
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 14;
            Color fill = bg;
            if (getModel().isPressed()) {
                fill = shift(bg, -18);
            } else if (getModel().isRollover()) {
                fill = shift(bg, primary ? 12 : 8);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            if (!primary) {
                g2.setColor(new Color(0xE5E7EB));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            // 文字 + 图标
            g2.setColor(isEnabled() ? fg : new Color(0x9CA3AF));
            FontMetrics fm = g2.getFontMetrics(getFont());
            String text = getText() == null ? "" : getText();
            Icon icon = getIcon();
            int iconW = icon == null ? 0 : icon.getIconWidth();
            int gap = (icon != null && !text.isEmpty()) ? 6 : 0;
            int totalW = iconW + gap + fm.stringWidth(text);
            int tx = (w - totalW) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            if (icon != null) {
                int iy = (h - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, tx, iy);
                tx += iconW + gap;
            }
            g2.setFont(getFont());
            g2.drawString(text, tx, ty);
            g2.dispose();
        }

        private static Color shift(Color c, int delta) {
            return new Color(
                    Math.max(0, Math.min(255, c.getRed() + delta)),
                    Math.max(0, Math.min(255, c.getGreen() + delta)),
                    Math.max(0, Math.min(255, c.getBlue() + delta)));
        }
    }

    private static JButton makeFlatButton(String text, Icon icon) {
        RoundedBtn b = new RoundedBtn(text, icon, new Color(0xF3F4F6), new Color(0x111827), false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.setFont(b.getFont().deriveFont(12f));
        return b;
    }

    private static JButton makePrimaryButton(String text) {
        RoundedBtn b = new RoundedBtn(text, null, new Color(0xFB923C), Color.WHITE, true);
        b.setBorder(new EmptyBorder(7, 16, 7, 16));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        return b;
    }
}
