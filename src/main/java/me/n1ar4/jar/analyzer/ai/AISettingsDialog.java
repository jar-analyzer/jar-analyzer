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
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 现代化 AI 配置面板（多配置 + 启用切换）
 * <p>
 * 设计参考 cc-switch：左侧卡片式列表，右侧表单卡片，扁平描边、绿点状态、
 * 顶部 header + 单一新增按钮、底部操作区集中右下。
 */
public class AISettingsDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger();

    // ----- 配色 -----
    private static final Color PAGE_BG = new Color(0xF8FAFC);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(0xE5E7EB);
    private static final Color BORDER_HOVER = new Color(0xCBD5E1);
    private static final Color TEXT_PRIMARY = new Color(0x111827);
    private static final Color TEXT_SECOND = new Color(0x6B7280);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color GREEN_BG = new Color(0xECFDF5);
    private static final Color ORANGE = new Color(0xFB923C);
    private static final Color RED = new Color(0xEF4444);
    private static final Color RED_BG = new Color(0xFEF2F2);

    // ----- 数据 -----
    private final DefaultListModel<AIConfig> listModel = new DefaultListModel<>();
    private final JList<AIConfig> profileList = new JList<>(listModel);

    // ----- 表单 -----
    private final JTextField nameField = new JTextField();
    private final JComboBox<AIProvider> providerBox = new JComboBox<>(AIProvider.values());
    private final JTextField baseUrlField = new JTextField();
    private final JComboBox<String> modelBox = new JComboBox<>();
    private final JPasswordField apiKeyField = new JPasswordField();
    private final JToggleButton showKeyToggle = new JToggleButton("显示");
    private final JTextField temperatureField = new JTextField();
    private final JTextField maxTokensField = new JTextField();
    private final JTextField timeoutField = new JTextField();
    private final JCheckBox streamBox = new JCheckBox("启用流式输出 (SSE)", true);
    private final JTextArea systemPromptArea = new JTextArea(5, 40);

    // ----- 行为按钮 -----
    private final JButton activateBtn = makePrimaryGreen("启用此配置");
    private final JButton testBtn = makeGhost("测试连接");
    private final JButton deleteBtn = makeGhostDanger("删除");
    private final JButton saveBtn = makePrimaryOrange("保存");

    // ----- 状态 -----
    private final JLabel toastLabel = new JLabel(" ");

    private AIConfig current;
    private boolean suppressEvents = false;

    public AISettingsDialog(Window owner) {
        super(owner, "AI 模型配置", ModalityType.APPLICATION_MODAL);
        setSize(1080, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        root.setOpaque(true);
        root.setBorder(new EmptyBorder(16, 18, 14, 18));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);

        // 事件
        providerBox.addItemListener(e -> {
            if (suppressEvents || e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            AIProvider p = (AIProvider) e.getItem();
            if (p == null) {
                return;
            }
            if (p != AIProvider.CUSTOM) {
                baseUrlField.setText(p.getDefaultBaseUrl());
            }
            rebuildModelOptions(p, p.getDefaultModel());
        });
        showKeyToggle.addActionListener(e -> {
            apiKeyField.setEchoChar(showKeyToggle.isSelected() ? (char) 0 : '\u2022');
            showKeyToggle.setText(showKeyToggle.isSelected() ? "隐藏" : "显示");
        });

        testBtn.addActionListener(e -> doTest());
        saveBtn.addActionListener(e -> doSave());
        deleteBtn.addActionListener(e -> doDelete());
        activateBtn.addActionListener(e -> doActivate());

        // 初始化数据
        reloadList();
        if (listModel.isEmpty()) {
            AIConfig def = newDefault("默认配置");
            AIConfigManager.upsert(def);
            reloadList();
        }
        AIConfigStore store = AIConfigManager.loadStore();
        int idx = indexOf(store.getActiveId());
        if (idx < 0) {
            idx = 0;
        }
        profileList.setSelectedIndex(idx);
    }

    // ====================================================
    //                        布局
    // ====================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 14, 4));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("AI 模型配置");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("管理多个模型；只能启用其中一个 · DeepSeek / 智谱 GLM / OpenAI 兼容");
        sub.setForeground(TEXT_SECOND);
        sub.setFont(sub.getFont().deriveFont(12f));
        sub.setBorder(new EmptyBorder(2, 0, 0, 0));
        titleBox.add(title);
        titleBox.add(sub);
        header.add(titleBox, BorderLayout.WEST);

        JButton add = makePrimaryOrange("+ 新增配置");
        add.addActionListener(e -> doAdd());
        header.add(add, BorderLayout.EAST);

        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.add(buildLeftPanel(), BorderLayout.WEST);
        body.add(buildRightPanel(), BorderLayout.CENTER);
        return body;
    }

    private JComponent buildLeftPanel() {
        RoundedPanel left = new RoundedPanel(12, CARD_BG, BORDER, new BorderLayout());
        left.setBorder(new EmptyBorder(8, 8, 8, 8));
        left.setPreferredSize(new Dimension(290, 0));

        JLabel hdr = new JLabel("配置列表");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 12f));
        hdr.setForeground(TEXT_SECOND);
        hdr.setBorder(new EmptyBorder(2, 6, 8, 6));
        left.add(hdr, BorderLayout.NORTH);

        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setCellRenderer(new ProfileCellRenderer());
        profileList.setFixedCellHeight(64);
        profileList.setBackground(CARD_BG);
        profileList.setOpaque(true);
        profileList.setBorder(null);
        profileList.addListSelectionListener(this::onListSelectionChanged);

        JScrollPane sp = new JScrollPane(profileList);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        left.add(sp, BorderLayout.CENTER);

        return left;
    }

    private JComponent buildRightPanel() {
        RoundedPanel right = new RoundedPanel(12, CARD_BG, BORDER, new BorderLayout(0, 12));
        right.setBorder(new EmptyBorder(20, 22, 18, 22));

        // ===== 顶部：编辑标题 =====
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel sectionTitle = new JLabel("编辑配置");
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 14f));
        sectionTitle.setForeground(TEXT_PRIMARY);
        titleRow.add(sectionTitle, BorderLayout.WEST);

        JButton applyLink = makeLink("获取 Key ↗");
        applyLink.addActionListener(e -> {
            AIProvider p = (AIProvider) providerBox.getSelectedItem();
            if (p == null || p.getApplyUrl() == null || p.getApplyUrl().isEmpty()) {
                toast("自定义后端无申请链接");
                return;
            }
            try {
                Desktop.getDesktop().browse(new URI(p.getApplyUrl()));
            } catch (Exception ex) {
                logger.error("open url error: {}", ex.toString());
            }
        });
        titleRow.add(applyLink, BorderLayout.EAST);
        right.add(titleRow, BorderLayout.NORTH);

        // ===== 中部：表单内容（可滚动） =====
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(6, 0, 0, 0));

        // 基本信息
        FormPanel basic = new FormPanel();
        basic.addRow("名称", nameField);
        basic.addRow("厂商", providerBox);
        basic.addRow("Base URL", baseUrlField);
        basic.addRow("模型", makeEditableComboBox());
        basic.addRow("API Key", buildKeyRow());
        content.add(basic);
        content.add(Box.createVerticalStrut(14));

        // 高级参数（卡片）
        SectionCard adv = new SectionCard("高级参数");
        FormPanel advForm = new FormPanel();
        JPanel tuneRow = new JPanel(new GridLayout(1, 6, 8, 0));
        tuneRow.setOpaque(false);
        tuneRow.add(makeTinyLabel("temperature"));
        tuneRow.add(temperatureField);
        tuneRow.add(makeTinyLabel("max_tokens"));
        tuneRow.add(maxTokensField);
        tuneRow.add(makeTinyLabel("timeout(s)"));
        tuneRow.add(timeoutField);
        styleTextField(temperatureField);
        styleTextField(maxTokensField);
        styleTextField(timeoutField);
        advForm.addRow("调参", tuneRow);
        advForm.addRow("流式输出", streamBox);
        styleCheckBox(streamBox);
        systemPromptArea.setLineWrap(true);
        systemPromptArea.setWrapStyleWord(true);
        systemPromptArea.setFont(systemPromptArea.getFont().deriveFont(13f));
        systemPromptArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane spScroll = new JScrollPane(systemPromptArea);
        spScroll.setBorder(new RoundedBorder(8, BORDER));
        spScroll.setPreferredSize(new Dimension(0, 110));
        advForm.addRow("System Prompt", spScroll);
        adv.setContent(advForm);
        content.add(adv);

        // 让 nameField/baseUrlField 也用统一样式
        styleTextField(nameField);
        styleTextField(baseUrlField);
        styleTextField(apiKeyField);
        styleComboBox(providerBox);
        styleComboBox(modelBox);

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getVerticalScrollBar().setUnitIncrement(24);
        contentScroll.setOpaque(false);
        contentScroll.getViewport().setOpaque(false);
        right.add(contentScroll, BorderLayout.CENTER);

        // ===== 底部：操作区 =====
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(4, 0, 0, 0));

        toastLabel.setForeground(TEXT_SECOND);
        toastLabel.setFont(toastLabel.getFont().deriveFont(12f));
        actions.add(toastLabel, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(deleteBtn);
        btns.add(testBtn);
        btns.add(saveBtn);
        btns.add(activateBtn);
        actions.add(btns, BorderLayout.EAST);
        right.add(actions, BorderLayout.SOUTH);

        return right;
    }

    private JComponent makeEditableComboBox() {
        modelBox.setEditable(true);
        return modelBox;
    }

    private JComponent buildKeyRow() {
        JPanel keyRow = new JPanel(new BorderLayout(8, 0));
        keyRow.setOpaque(false);
        styleTextField(apiKeyField);
        keyRow.add(apiKeyField, BorderLayout.CENTER);

        showKeyToggle.setFocusPainted(false);
        showKeyToggle.setBorderPainted(false);
        showKeyToggle.setContentAreaFilled(false);
        showKeyToggle.setOpaque(true);
        showKeyToggle.setBackground(CARD_BG);
        showKeyToggle.setForeground(TEXT_SECOND);
        showKeyToggle.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER),
                new EmptyBorder(4, 12, 4, 12)));
        showKeyToggle.setFont(showKeyToggle.getFont().deriveFont(12f));
        showKeyToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        keyRow.add(showKeyToggle, BorderLayout.EAST);
        return keyRow;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 4, 0, 4));

        JLabel hint = new JLabel("配置文件：./jar-analyzer-ai.json · API Key 仅本地保存");
        hint.setForeground(TEXT_SECOND);
        hint.setFont(hint.getFont().deriveFont(11f));
        footer.add(hint, BorderLayout.WEST);

        JButton close = makeGhost("关闭");
        close.addActionListener(e -> dispose());
        footer.add(close, BorderLayout.EAST);
        return footer;
    }

    // ====================================================
    //                       业务动作
    // ====================================================

    private void reloadList() {
        AIConfigStore store = AIConfigManager.loadStore();
        suppressEvents = true;
        try {
            listModel.clear();
            for (AIConfig c : store.getProfiles()) {
                listModel.addElement(c);
            }
        } finally {
            suppressEvents = false;
        }
    }

    private int indexOf(String id) {
        if (id == null || id.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < listModel.size(); i++) {
            if (id.equals(listModel.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private static AIConfig newDefault(String name) {
        AIConfig c = new AIConfig();
        c.setId(AIConfigManager.newId());
        c.setName(name == null ? "新配置" : name);
        c.setProvider(AIProvider.DEEPSEEK.name());
        c.setBaseUrl(AIProvider.DEEPSEEK.getDefaultBaseUrl());
        c.setModel(AIProvider.DEEPSEEK.getDefaultModel());
        return c;
    }

    private void rebuildModelOptions(AIProvider p, String currentModel) {
        suppressEvents = true;
        try {
            modelBox.removeAllItems();
            List<String> options = new ArrayList<>();
            if (p == AIProvider.DEEPSEEK) {
                options.addAll(Arrays.asList("deepseek-v4-pro", "deepseek-v4-flash"));
            } else if (p == AIProvider.GLM) {
                options.addAll(Arrays.asList("glm-5", "glm-5-turbo", "glm-5.1", "glm-4.7"));
            }
            for (String o : options) {
                modelBox.addItem(o);
            }
            if (currentModel != null && !currentModel.isEmpty()) {
                modelBox.setSelectedItem(currentModel);
            }
        } finally {
            suppressEvents = false;
        }
    }

    private void onListSelectionChanged(ListSelectionEvent e) {
        if (suppressEvents || e.getValueIsAdjusting()) {
            return;
        }
        AIConfig sel = profileList.getSelectedValue();
        if (sel == null) {
            return;
        }
        // 编辑副本，避免直接修改 store 内的对象，造成"未保存却已生效"的幽灵状态
        current = sel.copy();
        suppressEvents = true;
        try {
            nameField.setText(safe(current.getName()));
            providerBox.setSelectedItem(AIProvider.fromName(current.getProvider()));
            baseUrlField.setText(safe(current.getBaseUrl()));
            rebuildModelOptions((AIProvider) providerBox.getSelectedItem(), current.getModel());
            apiKeyField.setText(safe(current.getApiKey()));
            apiKeyField.setEchoChar(showKeyToggle.isSelected() ? (char) 0 : '\u2022');
            temperatureField.setText(String.valueOf(current.getTemperature()));
            maxTokensField.setText(String.valueOf(current.getMaxTokens()));
            timeoutField.setText(String.valueOf(current.getTimeoutSeconds()));
            streamBox.setSelected(current.isStream());
            systemPromptArea.setText(safe(current.getSystemPrompt()));
            systemPromptArea.setCaretPosition(0);
        } finally {
            suppressEvents = false;
        }
        refreshActivateButton();
    }

    private void refreshActivateButton() {
        AIConfigStore store = AIConfigManager.loadStore();
        boolean isActive = current != null && current.getId() != null
                && current.getId().equals(store.getActiveId());
        if (isActive) {
            activateBtn.setText("✓ 已启用");
            activateBtn.setEnabled(false);
        } else {
            activateBtn.setText("启用此配置");
            activateBtn.setEnabled(current != null);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private boolean collectIntoCurrent() {
        if (current == null) {
            toast("请先选择或新增一条配置");
            return false;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            toast("名称不能为空");
            return false;
        }
        String baseUrl = baseUrlField.getText().trim();
        if (baseUrl.isEmpty()
                || (!baseUrl.toLowerCase().startsWith("http://")
                && !baseUrl.toLowerCase().startsWith("https://"))) {
            toast("Base URL 必须以 http:// 或 https:// 开头");
            return false;
        }
        Object m = modelBox.getEditor().getItem();
        String model = m == null ? "" : m.toString().trim();
        if (model.isEmpty()) {
            toast("模型名不能为空");
            return false;
        }
        String apiKey = new String(apiKeyField.getPassword());
        if (apiKey.isEmpty()) {
            toast("API Key 不能为空");
            return false;
        }

        AIProvider p = (AIProvider) providerBox.getSelectedItem();
        current.setName(name);
        current.setProvider(p == null ? AIProvider.DEEPSEEK.name() : p.name());
        current.setBaseUrl(baseUrl);
        current.setModel(model);
        current.setApiKey(apiKey);
        current.setStream(streamBox.isSelected());
        try {
            current.setTemperature(Double.parseDouble(temperatureField.getText().trim()));
        } catch (Exception ignored) {
            current.setTemperature(0.3);
        }
        try {
            current.setMaxTokens(Integer.parseInt(maxTokensField.getText().trim()));
        } catch (Exception ignored) {
            current.setMaxTokens(4096);
        }
        try {
            current.setTimeoutSeconds(Integer.parseInt(timeoutField.getText().trim()));
        } catch (Exception ignored) {
            current.setTimeoutSeconds(120);
        }
        current.setSystemPrompt(systemPromptArea.getText());
        return true;
    }

    private void doAdd() {
        AIConfig c = newDefault("新配置");
        AIConfigManager.upsert(c);
        reloadList();
        int idx = indexOf(c.getId());
        if (idx >= 0) {
            profileList.setSelectedIndex(idx);
        }
        toast("已新增配置，请编辑后保存");
    }

    private void doDelete() {
        if (current == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "确认删除配置：\"" + current.getName() + "\" ？",
                "删除确认", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String id = current.getId();
        AIConfigManager.remove(id);
        reloadList();
        if (!listModel.isEmpty()) {
            profileList.setSelectedIndex(0);
        } else {
            current = null;
            clearForm();
            refreshActivateButton();
        }
        toast("已删除");
    }

    private void clearForm() {
        suppressEvents = true;
        try {
            nameField.setText("");
            baseUrlField.setText("");
            apiKeyField.setText("");
            modelBox.removeAllItems();
            systemPromptArea.setText("");
        } finally {
            suppressEvents = false;
        }
    }

    private void doActivate() {
        if (current == null) {
            return;
        }
        if (!collectIntoCurrent()) {
            return;
        }
        AIConfig snapshot = current;
        String id = snapshot.getId();
        AIConfigManager.upsert(snapshot);
        AIConfigManager.setActive(id);
        reloadList();
        int idx = indexOf(id);
        if (idx >= 0) {
            profileList.setSelectedIndex(idx);
        }
        refreshActivateButton();
        profileList.repaint();
        toastSuccess("已启用：" + snapshot.getName());
    }

    private void doSave() {
        if (!collectIntoCurrent()) {
            return;
        }
        AIConfig snapshot = current;
        AIConfigManager.upsert(snapshot);
        int sel = profileList.getSelectedIndex();
        if (sel >= 0 && sel < listModel.size()) {
            suppressEvents = true;
            try {
                listModel.set(sel, snapshot);
            } finally {
                suppressEvents = false;
            }
        }
        refreshActivateButton();
        toastSuccess("已保存：" + snapshot.getName());
    }

    private void doTest() {
        if (!collectIntoCurrent()) {
            return;
        }
        toast("测试中…");
        final AIConfig snapshot = current.copy();
        new Thread(() -> {
            try {
                LLMClient client = new LLMClient(snapshot);
                String resp = client.chat(LLMClient.singleTurn(
                        "You are a helpful assistant. Reply with one short sentence.",
                        "ping"));
                final String shown = resp == null ? "(空)" : resp.trim();
                SwingUtilities.invokeLater(() -> {
                    toastSuccess("连接成功");
                    JOptionPane.showMessageDialog(this,
                            "连接成功！模型回复：\n\n" + shown,
                            "测试成功", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Throwable ex) {
                String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                SwingUtilities.invokeLater(() -> {
                    toastError("连接失败");
                    JOptionPane.showMessageDialog(this,
                            "连接失败: " + msg,
                            "测试失败", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "ai-test").start();
    }

    private void toast(String msg) {
        toastLabel.setForeground(TEXT_SECOND);
        toastLabel.setText(msg);
    }

    private void toastSuccess(String msg) {
        toastLabel.setForeground(GREEN);
        toastLabel.setText("✓ " + msg);
    }

    private void toastError(String msg) {
        toastLabel.setForeground(RED);
        toastLabel.setText("✗ " + msg);
    }

    public static void open() {
        Window owner = MainForm.getInstance() != null ? SwingUtilities.getWindowAncestor(
                MainForm.getInstance().getMasterPanel()) : null;
        AISettingsDialog dlg = new AISettingsDialog(owner);
        // AI 配置面板强制浅色主题：不受用户当前主题影响（暗色/橙色）
        LightLafContext.applyLightTo(dlg);
        dlg.setVisible(true);
    }

    // ====================================================
    //                     渲染 / 样式
    // ====================================================

    /**
     * 配置列表卡片渲染（用 RoundedPanel 自绘背景，避免 LaF 覆盖）
     */
    private static class ProfileCellRenderer extends JPanel implements ListCellRenderer<AIConfig> {
        private final JLabel name = new JLabel();
        private final JLabel meta = new JLabel();
        private final StatusDot dot = new StatusDot();
        private final JLabel badge = new JLabel("在用");
        private final RoundedPanel inner;

        ProfileCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(3, 4, 3, 4));

            inner = new RoundedPanel(10, CARD_BG, BORDER, new BorderLayout(10, 0));
            inner.setBorder(new EmptyBorder(10, 12, 10, 12));

            JPanel left = new JPanel(new BorderLayout());
            left.setOpaque(false);
            left.setBorder(new EmptyBorder(4, 0, 0, 0));
            left.add(dot, BorderLayout.NORTH);
            inner.add(left, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setOpaque(false);
            name.setFont(name.getFont().deriveFont(Font.BOLD, 13f));
            name.setForeground(TEXT_PRIMARY);
            meta.setForeground(TEXT_SECOND);
            meta.setFont(meta.getFont().deriveFont(11f));
            meta.setBorder(new EmptyBorder(2, 0, 0, 0));
            center.add(name);
            center.add(meta);
            inner.add(center, BorderLayout.CENTER);

            // 在用徽标（自绘圆角）
            badge.setOpaque(false);
            badge.setForeground(GREEN);
            badge.setFont(badge.getFont().deriveFont(Font.BOLD, 10f));
            badge.setBorder(new EmptyBorder(2, 8, 2, 8));
            RoundedPanel badgeWrap = new RoundedPanel(8, GREEN_BG, GREEN, new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrap.add(badge);
            JPanel badgeOuter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeOuter.setOpaque(false);
            badgeOuter.add(badgeWrap);
            inner.add(badgeOuter, BorderLayout.EAST);
            this.badgeOuter = badgeOuter;

            add(inner, BorderLayout.CENTER);
        }

        private final JPanel badgeOuter;

        @Override
        public Component getListCellRendererComponent(JList<? extends AIConfig> list, AIConfig value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            String activeId = AIConfigManager.loadStore().getActiveId();
            boolean active = value.getId() != null && value.getId().equals(activeId);

            name.setText(value.getName() == null || value.getName().isEmpty() ? "(未命名)" : value.getName());
            String prov = value.getProvider() == null ? "" : value.getProvider();
            String model = value.getModel() == null ? "" : value.getModel();
            meta.setText(prov + " · " + model);

            dot.setColor(active ? GREEN : new Color(0xCBD5E1));
            badgeOuter.setVisible(active);

            // 选中态：换 fill / border 色
            inner.setColors(
                    isSelected ? new Color(0xFFF7ED) : CARD_BG,
                    isSelected ? ORANGE : BORDER);
            return this;
        }
    }

    /**
     * "标签 - 控件" 表单容器
     */
    private static class FormPanel extends JPanel {
        FormPanel() {
            setLayout(new GridBagLayout());
            setOpaque(false);
        }

        void addRow(String label, JComponent field) {
            int row = getComponentCount() / 2;

            GridBagConstraints g1 = new GridBagConstraints();
            g1.gridx = 0;
            g1.gridy = row;
            g1.anchor = GridBagConstraints.NORTHWEST;
            g1.insets = new Insets(8, 0, 8, 14);
            JLabel lbl = new JLabel(label);
            lbl.setFont(lbl.getFont().deriveFont(12f));
            lbl.setForeground(TEXT_SECOND);
            lbl.setPreferredSize(new Dimension(96, 28));
            add(lbl, g1);

            GridBagConstraints g2 = new GridBagConstraints();
            g2.gridx = 1;
            g2.gridy = row;
            g2.weightx = 1;
            g2.fill = GridBagConstraints.HORIZONTAL;
            g2.insets = new Insets(8, 0, 8, 0);
            add(field, g2);
        }
    }

    /**
     * 圆角分组卡片（自绘背景）
     */
    private static class SectionCard extends RoundedPanel {
        private final JPanel body = new JPanel(new BorderLayout());

        SectionCard(String title) {
            super(10, new Color(0xFAFAFB), BORDER, new BorderLayout());
            setBorder(new EmptyBorder(12, 14, 14, 14));

            JLabel hdr = new JLabel(title);
            hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 12f));
            hdr.setForeground(TEXT_SECOND);
            hdr.setBorder(new EmptyBorder(0, 0, 8, 0));
            add(hdr, BorderLayout.NORTH);

            body.setOpaque(false);
            add(body, BorderLayout.CENTER);
        }

        void setContent(JComponent c) {
            body.removeAll();
            body.add(c, BorderLayout.CENTER);
            body.revalidate();
            body.repaint();
        }
    }

    /**
     * 顶部状态点
     */
    private static class StatusDot extends JComponent {
        private Color color = new Color(0xCBD5E1);

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
     * 圆角描边 Border —— 仅画描边，不再填充背景
     * （之前用 c.getBackground() 填充会被 FlatLaf 的 ButtonUI 覆盖，导致白底白字问题）
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
     * 自绘圆角背景 + 描边的 Panel
     * 用于代替 setBackground + RoundedBorder 的组合（避免被 LaF UI 覆盖背景）
     */
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;
        private Color border;

        RoundedPanel(int radius, Color fill, Color border, LayoutManager lm) {
            super(lm);
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        void setColors(Color fill, Color border) {
            this.fill = fill;
            this.border = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = radius * 2;
            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===== 控件样式工具 =====

    private static JLabel makeTinyLabel(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(TEXT_SECOND);
        l.setFont(l.getFont().deriveFont(11f));
        return l;
    }

    private static void styleTextField(JTextField tf) {
        Border b = BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER),
                new EmptyBorder(7, 10, 7, 10));
        tf.setBorder(b);
        tf.setBackground(CARD_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setOpaque(true);
        tf.setFont(tf.getFont().deriveFont(13f));
        // hover 效果
        tf.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(8, BORDER_HOVER),
                        new EmptyBorder(7, 10, 7, 10)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tf.setBorder(b);
            }
        });
    }

    private static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(CARD_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setOpaque(true);
        cb.setBorder(new RoundedBorder(8, BORDER));
        cb.setFont(cb.getFont().deriveFont(13f));
    }

    private static void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(cb.getFont().deriveFont(13f));
    }

    // ===== 按钮 =====

    /**
     * 自绘圆角按钮：完全脱离 LaF 影响，保证多主题下文字/背景色可见
     */
    private static class RoundedButton extends JButton {
        private final Color bg;
        private final Color fg;
        private final Color borderColor;
        private final boolean filled;

        RoundedButton(String text, Color bg, Color fg, Color borderColor, boolean filled) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            this.borderColor = borderColor;
            this.filled = filled;
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(7, 16, 7, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 16;
            // 悬停淡化
            Color fill = bg;
            if (getModel().isPressed()) {
                fill = darker(bg);
            } else if (getModel().isRollover()) {
                fill = lighter(bg);
            }
            if (filled) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
            } else {
                // 幽灵：填充 fill（通常浅色），描边 borderColor
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            }
            // 文字
            g2.setColor(isEnabled() ? fg : new Color(0x9CA3AF));
            FontMetrics fm = g2.getFontMetrics(getFont());
            String text = getText();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.drawString(text, tx, ty);
            g2.dispose();
        }

        private static Color darker(Color c) {
            return new Color(
                    Math.max(0, c.getRed() - 18),
                    Math.max(0, c.getGreen() - 18),
                    Math.max(0, c.getBlue() - 18));
        }

        private static Color lighter(Color c) {
            return new Color(
                    Math.min(255, c.getRed() + 12),
                    Math.min(255, c.getGreen() + 12),
                    Math.min(255, c.getBlue() + 12));
        }
    }

    private static JButton makePrimaryOrange(String text) {
        RoundedButton b = new RoundedButton(text, ORANGE, Color.WHITE, ORANGE, true);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        return b;
    }

    private static JButton makePrimaryGreen(String text) {
        RoundedButton b = new RoundedButton(text, GREEN, Color.WHITE, GREEN, true);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        return b;
    }

    private static JButton makeGhost(String text) {
        RoundedButton b = new RoundedButton(text, CARD_BG, TEXT_PRIMARY, BORDER, false);
        b.setFont(b.getFont().deriveFont(12f));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    private static JButton makeGhostDanger(String text) {
        RoundedButton b = new RoundedButton(text, RED_BG, RED, new Color(0xFCA5A5), false);
        b.setFont(b.getFont().deriveFont(12f));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    private static JButton makeLink(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int tx = 0;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setForeground(new Color(0x2563EB));
        b.setFont(b.getFont().deriveFont(12f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
