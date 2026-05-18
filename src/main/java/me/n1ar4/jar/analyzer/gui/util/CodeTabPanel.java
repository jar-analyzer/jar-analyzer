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
import me.n1ar4.jar.analyzer.gui.adapter.GlobalKeyListener;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.MouseUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
    // 当前语法主题路径（供新 Tab 使用）
    private static String currentSyntaxTheme = "syntax/default.xml";

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
        // 注意：macOS 在 mousePressed 时触发 isPopupTrigger，
        // Windows/Linux 在 mouseReleased 时触发，因此两边都需要处理。
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowTabPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowTabPopup(e);
            }

            private void maybeShowTabPopup(MouseEvent e) {
                if (MouseUtil.isPopupTrigger(e)) {
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
        JPanel wrapper = createTabContentWrapper(sp);
        tabbedPane.addTab("Welcome", wrapper);
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

        // Decide the displayed title.
        //   - For class entries the key is "com/foo/Bar" -> short "Bar".
        //   - For resource entries the key is the full filesystem path
        //     (separators may be '/' or '\\' depending on the OS).
        // Default: show only the leaf segment. If another tab is already
        // showing the same leaf (so the user could not tell them apart),
        // promote BOTH tabs back to the full key, IDE-style.
        String shortName = getShortClassName(className);
        String displayTitle = resolveDisplayTitle(className, shortName);

        RTextScrollPane sp = new RTextScrollPane(newArea);
        JPanel wrapper = createTabContentWrapper(sp);
        tabbedPane.addTab(displayTitle, wrapper);
        int newIndex = tabbedPane.getTabCount() - 1;
        setTabCloseButton(newIndex);
        tabbedPane.setToolTipTextAt(newIndex, className.replace("/", "."));

        tabMap.put(className, newArea);
        titleMap.put(className, displayTitle);

        // 切换到新 Tab
        tabbedPane.setSelectedIndex(newIndex);
        MainForm.setCodeArea(newArea);

        logger.info("opened new tab: {} (total: {})", displayTitle, tabMap.size());
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
        RTextScrollPane scrollPane = getScrollPaneAt(selectedIndex);
        if (scrollPane != null) {
            Component view = scrollPane.getViewport().getView();
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
        String keyToRemove = null;
        RTextScrollPane scrollPane = getScrollPaneAt(tabIndex);
        if (scrollPane != null) {
            Component view = scrollPane.getViewport().getView();
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

        // After any tab removal a previously-promoted full-path title
        // may now be the only one of its short name and can be
        // collapsed back. Cheap to run and keeps the UI tidy.
        recomputeTitlesAfterRemoval();

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
            RTextScrollPane scrollPane = getScrollPaneAt(i);
            if (scrollPane != null) {
                Component view = scrollPane.getViewport().getView();
                if (view == area) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 创建 Tab 内容的包装面板，在顶部留出间隙
     * 使用独立的 JPanel 包装，避免 JTabbedPane 选中时边框高亮覆盖到间隙区域
     */
    private JPanel createTabContentWrapper(RTextScrollPane scrollPane) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * 从 Tab 索引获取 RTextScrollPane（支持直接或包装在 JPanel 中的情况）
     */
    private RTextScrollPane getScrollPaneAt(int index) {
        if (index < 0 || index >= tabbedPane.getTabCount()) return null;
        Component comp = tabbedPane.getComponentAt(index);
        if (comp instanceof RTextScrollPane) {
            return (RTextScrollPane) comp;
        }
        if (comp instanceof JPanel) {
            for (Component child : ((JPanel) comp).getComponents()) {
                if (child instanceof RTextScrollPane) {
                    return (RTextScrollPane) child;
                }
            }
        }
        return null;
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

        // 应用当前语法主题（确保新 Tab 跟随已选主题）
        try {
            ClassLoader cl = CodeTabPanel.class.getClassLoader();
            Theme theme = Theme.load(cl.getResourceAsStream(currentSyntaxTheme));
            theme.apply(rArea);
            rArea.setFont(rArea.getFont().deriveFont(MainForm.FONT_SIZE));
        } catch (Exception ex) {
            logger.error("apply theme to new code area failed: {}", ex.toString());
        }

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

        // Ctrl+Click（macOS 上是 Cmd+Click）跳转导航
        rArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 必须是真正的左键 + 跳转修饰键。
                // macOS 上 Ctrl+左键 会被识别为右键（BUTTON3），
                // 因此跳转修饰键在 macOS 上必须使用 Command（Meta）。
                if (!MouseUtil.isNavigateClick(e)) {
                    return;
                }
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
                    logger.info("ctrl/cmd+click navigate: {}", methodName);
                    String className = MainForm.getCurClass();
                    if (className == null || className.isEmpty()) {
                        return;
                    }

                    // 使用 CtrlClickNavigator 进行跳转导航（新 Tab 中打开）
                    CtrlClickNavigator.navigate(methodName, className,
                            e.getXOnScreen(), e.getYOnScreen());
                }
            }
        });

        // 跳转修饰键（Win/Linux: Ctrl, macOS: Cmd）悬停时显示手型光标
        rArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (MouseUtil.isMenuShortcutDown(e)) {
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

        // 当跳转修饰键松开时恢复默认光标
        rArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_META) {
                    rArea.setCursor(defaultCursor);
                }
            }
        });

        // Ctrl+W（macOS 上为 Cmd+W）关闭当前 Tab 快捷键
        rArea.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, MouseUtil.getMenuShortcutKeyMask()), "closeTab");
        rArea.getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeCurrentTab();
            }
        });

        // 设置右键菜单
        CodeMenuHelper.setupPopupMenu(rArea);

        // 注册 Ctrl+F / Ctrl+X 快捷键（每个新 Tab 都需要）
        rArea.addKeyListener(new GlobalKeyListener());

        return rArea;
    }

    /**
     * 获取短类名（用作 Tab 标题）
     * <p>
     * Splits on every common path separator so the leaf segment is
     * found regardless of whether the key came from a class name
     * ({@code com/foo/Bar}) or a filesystem path on Windows
     * ({@code C:\proj\app.yml}) or POSIX ({@code /etc/app.yml}).
     */
    private String getShortClassName(String className) {
        if (className == null || className.isEmpty()) {
            return "Untitled";
        }
        int cut = -1;
        for (int i = className.length() - 1; i >= 0; i--) {
            char c = className.charAt(i);
            if (c == '/' || c == '\\' || c == File.separatorChar) {
                cut = i;
                break;
            }
        }
        if (cut < 0) {
            return className;
        }
        if (cut == className.length() - 1) {
            // Trailing separator, e.g. "foo/" -- fall back to the
            // whole string rather than emitting an empty title.
            return className;
        }
        return className.substring(cut + 1);
    }

    /**
     * Decides the title shown on the new tab and, when needed,
     * promotes existing tabs that would clash on their leaf name back
     * to their full key, so two same-named files are always
     * distinguishable visually.
     * <p>
     * Mutates the tab strip and {@link #titleMap} for the existing
     * conflicting tabs as a side-effect; returns the title the caller
     * should use for the new tab.
     */
    private String resolveDisplayTitle(String newKey, String shortName) {
        // Walk every existing tab; if any non-welcome tab is currently
        // displayed under the same shortName, both that tab and the
        // new one need to fall back to their full key.
        boolean conflict = false;
        for (Map.Entry<String, String> e : titleMap.entrySet()) {
            String key = e.getKey();
            if (WELCOME_KEY.equals(key) || key.equals(newKey)) {
                continue;
            }
            String existingShort = getShortClassName(key);
            if (shortName.equals(existingShort)) {
                conflict = true;
                // Bump the existing tab's title to its full key.
                if (!key.equals(e.getValue())) {
                    setTabTitleFor(key, key);
                    titleMap.put(key, key);
                }
            }
        }
        return conflict ? newKey : shortName;
    }

    /**
     * Re-collapses tab titles after a tab has been removed: if a tab
     * was previously shown by its full key only because of a clash,
     * and the clash is gone, restore the short leaf-name title.
     */
    private void recomputeTitlesAfterRemoval() {
        // Index every non-welcome tab by its short name so we can tell
        // who is still in conflict.
        Map<String, java.util.List<String>> byShort = new LinkedHashMap<>();
        for (String key : titleMap.keySet()) {
            if (WELCOME_KEY.equals(key)) {
                continue;
            }
            byShort.computeIfAbsent(getShortClassName(key),
                    k -> new java.util.ArrayList<>()).add(key);
        }
        for (Map.Entry<String, java.util.List<String>> e : byShort.entrySet()) {
            String shortName = e.getKey();
            java.util.List<String> keys = e.getValue();
            if (keys.size() == 1) {
                // No more conflict on this short name -- shrink the
                // single survivor's title back to the short form, if
                // it isn't already.
                String key = keys.get(0);
                if (!shortName.equals(titleMap.get(key))) {
                    setTabTitleFor(key, shortName);
                    titleMap.put(key, shortName);
                }
            }
        }
    }

    /**
     * Updates the title of the tab whose key is {@code className}.
     * Silently no-ops when the tab cannot be located, so callers don't
     * have to special-case races with concurrent close.
     */
    private void setTabTitleFor(String className, String title) {
        int idx = findTabIndex(className);
        if (idx < 0) {
            return;
        }
        tabbedPane.setTitleAt(idx, title);
        // The tab component may be a TabCloseComponent that pulls its
        // text from getTitleAt(...) every paint -- nudge it to refresh
        // immediately so the change is visible without a focus event.
        Component comp = tabbedPane.getTabComponentAt(idx);
        if (comp != null) {
            comp.invalidate();
            comp.repaint();
        }
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
     * 设置当前语法主题，并将主题应用到所有已打开的 Tab
     *
     * @param syntaxThemePath 语法主题资源路径，如 "syntax/dark.xml"
     */
    public void applyThemeToAllTabs(String syntaxThemePath) {
        currentSyntaxTheme = syntaxThemePath;
        ClassLoader cl = CodeTabPanel.class.getClassLoader();
        try {
            Theme theme = Theme.load(cl.getResourceAsStream(syntaxThemePath));
            for (RSyntaxTextArea area : tabMap.values()) {
                theme.apply(area);
                area.setFont(area.getFont().deriveFont(MainForm.FONT_SIZE));
            }
        } catch (Exception ex) {
            logger.error("apply theme to all tabs failed: {}", ex.toString());
        }
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
