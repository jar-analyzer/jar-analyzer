/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多标签页代码编辑器面板
 * 类似 VSCode/IDEA 的多 Tab 功能，支持：
 * - 打开多个类文件到不同 Tab
 * - 关闭当前/所有 Tab
 * - Tab 右键菜单（关闭当前、关闭其他、关闭所有）
 * - Ctrl+Click 跳转时在新 Tab 中打开
 */
public class CodeTabPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger();

    private final JTabbedPane tabbedPane;
    // key: className, value: RSyntaxTextArea
    private final Map<String, RSyntaxTextArea> tabMap = new LinkedHashMap<>();
    // key: className, value: tab title (short class name)
    private final Map<String, String> titleMap = new LinkedHashMap<>();
    // 默认的欢迎 Tab 的 key
    private static final String WELCOME_KEY = "__welcome__";
    // 最大 Tab 数量
    private static final int MAX_TABS = 20;

    public CodeTabPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(11f));

        // Tab 切换时更新 MainForm.codeArea 和 curClass
        tabbedPane.addChangeListener(e -> {
            RSyntaxTextArea activeArea = getActiveCodeArea();
            if (activeArea != null) {
                MainForm.setCodeArea(activeArea);
            }
            // 同步更新 curClass，避免 Ctrl+Click 时 className 与当前 Tab 不一致
            String activeClassName = getActiveClassName();
            if (activeClassName != null) {
                MainForm.setCurClass(activeClassName);
                MainForm.getInstance().getCurClassText().setText(activeClassName);
            }
        });

        // Tab 右键菜单
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex >= 0) {
                        showTabContextMenu(e, tabIndex);
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // 中键点击关闭 Tab
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex >= 0) {
                        closeTabAt(tabIndex);
                    }
                }
            }
        });

        // Tab 标签栏与代码区域之间留一点间隔
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        add(tabbedPane, BorderLayout.CENTER);

        // 创建一个欢迎 Tab
        createWelcomeTab();
    }

    /**
     * 创建欢迎 Tab
     */
    private void createWelcomeTab() {
        RSyntaxTextArea welcomeArea = createCodeArea();
        welcomeArea.setText(Const.welcome);
        welcomeArea.setEditable(false);
        welcomeArea.setCaretPosition(0);

        RTextScrollPane sp = new RTextScrollPane(welcomeArea);
        tabbedPane.addTab("Welcome", sp);
        setTabCloseButton(tabbedPane.getTabCount() - 1);

        tabMap.put(WELCOME_KEY, welcomeArea);
        titleMap.put(WELCOME_KEY, "Welcome");
        MainForm.setCodeArea(welcomeArea);
    }

    /**
     * 在新 Tab 中打开类文件
     *
     * @param className 完整类名（如 com/example/Test）
     * @param code      反编译的代码
     * @param caretPos  光标位置
     * @return 对应的 RSyntaxTextArea
     */
    public RSyntaxTextArea openTab(String className, String code, int caretPos) {
        if (className == null || className.isEmpty()) {
            className = "Untitled-" + System.currentTimeMillis();
        }

        // 如果这个类已经有 Tab 了，切换到它并更新内容
        if (tabMap.containsKey(className)) {
            RSyntaxTextArea existingArea = tabMap.get(className);
            existingArea.setText(code);
            if (caretPos >= 0 && caretPos < code.length()) {
                existingArea.setCaretPosition(caretPos);
            }
            // 切换到这个 Tab
            int index = findTabIndex(className);
            if (index >= 0) {
                tabbedPane.setSelectedIndex(index);
            }
            MainForm.setCodeArea(existingArea);
            return existingArea;
        }

        // 如果还有欢迎 Tab 且这是第一个真正的文件，替换欢迎 Tab
        if (tabMap.containsKey(WELCOME_KEY) && tabMap.size() == 1) {
            closeWelcomeTab();
        }

        // 如果超过最大 Tab 数，关闭最老的 Tab
        if (tabMap.size() >= MAX_TABS) {
            closeOldestTab();
        }

        // 创建新 Tab
        RSyntaxTextArea newArea = createCodeArea();
        newArea.setText(code);
        if (caretPos >= 0 && caretPos < code.length()) {
            newArea.setCaretPosition(caretPos);
        }

        String shortName = getShortClassName(className);

        RTextScrollPane sp = new RTextScrollPane(newArea);
        tabbedPane.addTab(shortName, sp);
        int newIndex = tabbedPane.getTabCount() - 1;
        setTabCloseButton(newIndex);
        tabbedPane.setToolTipTextAt(newIndex, className.replace("/", "."));

        tabMap.put(className, newArea);
        titleMap.put(className, shortName);

        // 切换到新 Tab
        tabbedPane.setSelectedIndex(newIndex);
        MainForm.setCodeArea(newArea);

        logger.info("opened new tab: {} (total: {})", shortName, tabMap.size());
        return newArea;
    }

    /**
     * 在当前 Tab 中更新内容（传统行为，兼容原有逻辑）
     */
    public RSyntaxTextArea updateCurrentTab(String code, int caretPos) {
        RSyntaxTextArea activeArea = getActiveCodeArea();
        if (activeArea != null) {
            activeArea.setText(code);
            if (caretPos >= 0 && caretPos <= code.length()) {
                activeArea.setCaretPosition(caretPos);
            }
            return activeArea;
        }
        return openTab(null, code, caretPos);
    }

    /**
     * 获取当前活动的 RSyntaxTextArea
     */
    public RSyntaxTextArea getActiveCodeArea() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }
        Component comp = tabbedPane.getComponentAt(selectedIndex);
        if (comp instanceof RTextScrollPane) {
            Component view = ((RTextScrollPane) comp).getViewport().getView();
            if (view instanceof RSyntaxTextArea) {
                return (RSyntaxTextArea) view;
            }
        }
        return null;
    }

    /**
     * 获取当前活动 Tab 对应的类名
     */
    public String getActiveClassName() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < 0) return null;
        RSyntaxTextArea activeArea = getActiveCodeArea();
        for (Map.Entry<String, RSyntaxTextArea> entry : tabMap.entrySet()) {
            if (entry.getValue() == activeArea) {
                String key = entry.getKey();
                return WELCOME_KEY.equals(key) ? null : key;
            }
        }
        return null;
    }

    /**
     * 关闭指定索引的 Tab
     */
    public void closeTabAt(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) return;

        // 找到对应的 className key
        Component comp = tabbedPane.getComponentAt(tabIndex);
        String keyToRemove = null;
        if (comp instanceof RTextScrollPane) {
            Component view = ((RTextScrollPane) comp).getViewport().getView();
            for (Map.Entry<String, RSyntaxTextArea> entry : tabMap.entrySet()) {
                if (entry.getValue() == view) {
                    keyToRemove = entry.getKey();
                    break;
                }
            }
        }

        tabbedPane.removeTabAt(tabIndex);
        if (keyToRemove != null) {
            tabMap.remove(keyToRemove);
            titleMap.remove(keyToRemove);
        }

        // 如果没有 Tab 了，创建欢迎 Tab
        if (tabbedPane.getTabCount() == 0) {
            createWelcomeTab();
        } else {
            // 更新 MainForm.codeArea 为当前活动的
            RSyntaxTextArea activeArea = getActiveCodeArea();
            if (activeArea != null) {
                MainForm.setCodeArea(activeArea);
            }
        }
    }

    /**
     * 关闭当前活动 Tab
     */
    public void closeCurrentTab() {
        int selected = tabbedPane.getSelectedIndex();
        if (selected >= 0) {
            closeTabAt(selected);
        }
    }

    /**
     * 关闭其他所有 Tab（保留当前）
     */
    public void closeOtherTabs() {
        int selected = tabbedPane.getSelectedIndex();
        if (selected < 0) return;
        Component currentComp = tabbedPane.getComponentAt(selected);

        for (int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
            if (tabbedPane.getComponentAt(i) != currentComp) {
                closeTabAt(i);
            }
        }
    }

    /**
     * 关闭所有 Tab
     */
    public void closeAllTabs() {
        tabMap.clear();
        titleMap.clear();
        tabbedPane.removeAll();
        createWelcomeTab();
    }

    /**
     * 关闭欢迎 Tab
     */
    private void closeWelcomeTab() {
        if (tabMap.containsKey(WELCOME_KEY)) {
            int index = findTabIndex(WELCOME_KEY);
            if (index >= 0) {
                tabbedPane.removeTabAt(index);
            }
            tabMap.remove(WELCOME_KEY);
            titleMap.remove(WELCOME_KEY);
        }
    }

    /**
     * 关闭最老的 Tab（第一个非 welcome 的）
     */
    private void closeOldestTab() {
        for (Map.Entry<String, RSyntaxTextArea> entry : tabMap.entrySet()) {
            if (!WELCOME_KEY.equals(entry.getKey())) {
                int index = findTabIndex(entry.getKey());
                if (index >= 0) {
                    closeTabAt(index);
                }
                break;
            }
        }
    }

    /**
     * 根据 className key 查找 Tab 索引
     */
    private int findTabIndex(String className) {
        RSyntaxTextArea area = tabMap.get(className);
        if (area == null) return -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof RTextScrollPane) {
                Component view = ((RTextScrollPane) comp).getViewport().getView();
                if (view == area) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 设置 Tab 关闭按钮
     */
    private void setTabCloseButton(int index) {
        tabbedPane.setTabComponentAt(index, new TabCloseComponent(tabbedPane, this));
    }

    /**
     * Tab 右键上下文菜单
     */
    private void showTabContextMenu(MouseEvent e, int tabIndex) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(ev -> closeTabAt(tabIndex));
        popup.add(closeItem);

        JMenuItem closeOthersItem = new JMenuItem("Close Others");
        closeOthersItem.addActionListener(ev -> {
            tabbedPane.setSelectedIndex(tabIndex);
            closeOtherTabs();
        });
        popup.add(closeOthersItem);

        JMenuItem closeAllItem = new JMenuItem("Close All");
        closeAllItem.addActionListener(ev -> closeAllTabs());
        popup.add(closeAllItem);

        popup.addSeparator();

        JMenuItem closeLeftItem = new JMenuItem("Close Tabs to the Left");
        closeLeftItem.addActionListener(ev -> {
            for (int i = tabIndex - 1; i >= 0; i--) {
                closeTabAt(i);
            }
        });
        closeLeftItem.setEnabled(tabIndex > 0);
        popup.add(closeLeftItem);

        JMenuItem closeRightItem = new JMenuItem("Close Tabs to the Right");
        closeRightItem.addActionListener(ev -> {
            for (int i = tabbedPane.getTabCount() - 1; i > tabIndex; i--) {
                closeTabAt(i);
            }
        });
        closeRightItem.setEnabled(tabIndex < tabbedPane.getTabCount() - 1);
        popup.add(closeRightItem);

        popup.show(tabbedPane, e.getX(), e.getY());
    }

    /**
     * 创建一个配置好的 RSyntaxTextArea，包含 Ctrl+Click 导航和高亮功能
     */
    private RSyntaxTextArea createCodeArea() {
        RSyntaxTextArea rArea = new RSyntaxTextArea();
        rArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        rArea.setCodeFoldingEnabled(true);
        rArea.setFont(rArea.getFont().deriveFont(MainForm.FONT_SIZE));

        // 选中文本高亮所有相同文本
        rArea.addCaretListener(e -> {
            String selectedText = rArea.getSelectedText();
            if (selectedText == null || selectedText.trim().isEmpty()) {
                Highlighter highlighter = rArea.getHighlighter();
                highlighter.removeAllHighlights();
                return;
            }
            Highlighter highlighter = rArea.getHighlighter();
            highlighter.removeAllHighlights();

            String text = rArea.getText();
            int index = 0;
            while ((index = text.indexOf(selectedText, index)) >= 0) {
                try {
                    highlighter.addHighlight(index, index + selectedText.length(),
                            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                    index += selectedText.length();
                } catch (BadLocationException ignored) {
                }
            }
        });

        Highlighter highlighter = rArea.getHighlighter();

        // 保存默认光标
        final Cursor defaultCursor = rArea.getCursor();
        final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

        // Ctrl+Click 跳转导航
        rArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isControlDown()) {
                    int caretPosition = rArea.getCaretPosition();
                    int start = SyntaxAreaHelper.findWordStart(rArea.getText(), caretPosition);
                    int end = SyntaxAreaHelper.findWordEnd(rArea.getText(), caretPosition);
                    if (start != -1 && end != -1) {
                        String word = rArea.getText().substring(start, end);
                        highlighter.removeAllHighlights();
                        try {
                            highlighter.addHighlight(start, end,
                                    new DefaultHighlighter.DefaultHighlightPainter(Color.BLUE));
                        } catch (BadLocationException ignored) {
                        }

                        String methodName = word.trim();
                        if (methodName.isEmpty()) {
                            return;
                        }
                        logger.info("ctrl+click navigate: {}", methodName);
                        String className = MainForm.getCurClass();
                        if (className == null || className.isEmpty()) {
                            return;
                        }

                        // 使用 CtrlClickNavigator 进行跳转导航（新 Tab 中打开）
                        CtrlClickNavigator.navigate(methodName, className,
                                e.getXOnScreen(), e.getYOnScreen());
                    }
                }
            }
        });

        // Ctrl 悬停时显示手型光标
        rArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.isControlDown()) {
                    int offset = rArea.viewToModel(e.getPoint());
                    if (offset >= 0) {
                        int start = SyntaxAreaHelper.findWordStart(rArea.getText(), offset);
                        int end = SyntaxAreaHelper.findWordEnd(rArea.getText(), offset);
                        if (start != -1 && end != -1 && start != end) {
                            rArea.setCursor(handCursor);
                            return;
                        }
                    }
                }
                rArea.setCursor(defaultCursor);
            }
        });

        // 当 Ctrl 键松开时恢复默认光标
        rArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                    rArea.setCursor(defaultCursor);
                }
            }
        });

        // Ctrl+W 关闭当前 Tab 快捷键
        rArea.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "closeTab");
        rArea.getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeCurrentTab();
            }
        });

        // 设置右键菜单
        CodeMenuHelper.setupPopupMenu(rArea);

        return rArea;
    }

    /**
     * 获取短类名（用作 Tab 标题）
     */
    private String getShortClassName(String className) {
        if (className == null) return "Untitled";
        if (className.contains("/")) {
            return className.substring(className.lastIndexOf('/') + 1);
        }
        return className;
    }

    /**
     * 获取 JTabbedPane（用于布局）
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * 获取 Tab 数量
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Tab 关闭按钮组件
     */
    private static class TabCloseComponent extends JPanel {
        public TabCloseComponent(JTabbedPane tabbedPane, CodeTabPanel codeTabPanel) {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

            // Tab 标题 label
            JLabel titleLabel = new JLabel() {
                @Override
                public String getText() {
                    int index = tabbedPane.indexOfTabComponent(TabCloseComponent.this);
                    if (index >= 0) {
                        return tabbedPane.getTitleAt(index);
                    }
                    return "";
                }
            };
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            add(titleLabel);

            // 关闭按钮
            JButton closeButton = new JButton("×");
            closeButton.setPreferredSize(new Dimension(17, 17));
            closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, 12f));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setForeground(new Color(150, 150, 150));
            closeButton.setToolTipText("Close (Ctrl+W)");
            closeButton.setMargin(new Insets(0, 0, 0, 0));

            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setForeground(Color.RED);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setForeground(new Color(150, 150, 150));
                }
            });

            closeButton.addActionListener(e -> {
                int index = tabbedPane.indexOfTabComponent(TabCloseComponent.this);
                if (index >= 0) {
                    codeTabPanel.closeTabAt(index);
                }
            });

            add(closeButton);
        }
    }
}
