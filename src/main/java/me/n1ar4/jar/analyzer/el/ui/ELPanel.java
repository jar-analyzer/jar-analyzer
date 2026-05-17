/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el.ui;

import me.n1ar4.jar.analyzer.config.UIPrefs;
import me.n1ar4.jar.analyzer.el.Templates;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * SPEL search workbench panel -- IDEA-style.
 * <p>
 * Layout:
 * <pre>
 *  +---------------------------------------------------------------+
 *  | toolbar:  ▶ run  ■ stop  ✓ check  ⤓ export  | format clear ...|
 *  +---------------+-----------------------------------------------+
 *  | Live Templates|  SPEL Editor  (RSyntaxTextArea + autocomplete)|
 *  |  ▼ filter     |                                               |
 *  |  ▶ 基础       +-----------------------------------------------+
 *  |  ▶ Web        |  Output (timestamped console, IDEA dark)      |
 *  |  ▶ ...        |                                               |
 *  +---------------+-----------------------------------------------+
 *  | status: caret / chars / msg               progress bar        |
 *  +---------------------------------------------------------------+
 * </pre>
 * <p>
 * Selecting any leaf in the template tree <em>replaces</em> the editor
 * contents -- mirroring how IntelliJ's "Live Templates" preview works.
 * "插入" remains as an alternative, append-style action for power users
 * who want to combine multiple snippets.
 * <p>
 * Public getters retained verbatim so {@code ELForm} keeps working
 * without changes to its wiring.
 */
public class ELPanel extends JPanel {

    // ---- editor / actions ----------------------------------------------
    private final RSyntaxTextArea codeArea;
    private final JButton checkButton;
    private final JButton searchButton;
    private final JButton stopBtn;
    private final JButton exportBtn;
    private final JProgressBar progressBar;
    private final JLabel msgLabel;

    // ---- left pane (template browser) ----------------------------------
    private final JTree templateTree;
    private final DefaultTreeModel templateModel;
    private final DefaultMutableTreeNode templateRoot;
    private final JTextField templateFilter;

    // ---- output console ------------------------------------------------
    private final JTextArea outputArea;
    private final JLabel caretLabel;

    // ---- splits (persisted) --------------------------------------------
    public static final String K_SPLIT_EL_LEFT = "el.split.left";
    public static final String K_SPLIT_EL_VERT = "el.split.vert";

    // ---- IDEA-ish palette ----------------------------------------------
    // Pick light values so we still look fine under the project's
    // default FlatLaf "light" theme; the console keeps its own dark
    // background to mimic IntelliJ's run console.
    private static final Color C_BORDER = new Color(0xC9CCD6);
    private static final Color C_BG_PANEL = new Color(0xF7F8FA);
    private static final Color C_BG_HEADER = new Color(0xEDEFF3);
    private static final Color C_FG_HEADER = new Color(0x2B2D30);
    private static final Color C_BG_CONSOLE = new Color(0x2B2B2B);
    private static final Color C_FG_CONSOLE = new Color(0xA9B7C6);
    private static final Color C_BLUE_ACCENT = new Color(0x3574F0);

    public ELPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setBackground(C_BG_PANEL);

        // ============================================================
        // Toolbar (top)
        // ============================================================
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(2, 4, 2, 4)));
        toolbar.setBackground(C_BG_HEADER);
        toolbar.setOpaque(true);

        searchButton = makeIconBtn("使用该表达式搜索",
                "按当前 SpEL 表达式在已索引的方法中搜索 (Ctrl+Enter)",
                SvgManager.ElRunIcon);
        stopBtn = makeIconBtn("强行停止",
                "中断正在执行的搜索任务", SvgManager.ElStopIcon);
        checkButton = makeIconBtn("验证表达式",
                "仅做 SpEL 语法检查，不发起搜索", SvgManager.ElCheckIcon);
        exportBtn = makeIconBtn("导出 CSV",
                "把当前结果列表导出到 CSV 文件", SvgManager.ElExportIcon);

        JButton formatBtn = makeIconBtn("格式化",
                "按链式调用规范缩进当前 SpEL 表达式", SvgManager.ElFormatIcon);
        JButton clearBtn = makeIconBtn("清空",
                "清空编辑区", SvgManager.ElClearIcon);
        JButton copyBtn = makeIconBtn("复制",
                "把编辑区内容复制到系统剪贴板", SvgManager.ElCopyIcon);
        JButton clearLogBtn = makeIconBtn("清空日志",
                "清空下方输出区", SvgManager.ElClearLogIcon);

        toolbar.add(searchButton);
        toolbar.add(stopBtn);
        toolbar.add(checkButton);
        toolbar.addSeparator(new Dimension(8, 16));
        toolbar.add(exportBtn);
        toolbar.addSeparator(new Dimension(8, 16));
        toolbar.add(formatBtn);
        toolbar.add(clearBtn);
        toolbar.add(copyBtn);
        toolbar.addSeparator(new Dimension(8, 16));
        toolbar.add(clearLogBtn);
        toolbar.add(Box.createHorizontalGlue());

        add(toolbar, BorderLayout.NORTH);

        // ============================================================
        // Editor (center-top)
        // ============================================================
        // No fixed (rows, cols) constructor on purpose: that would lock
        // a large preferred size on the text area and make the
        // surrounding JSplitPane refuse to shrink past it. We let
        // BorderLayout.CENTER stretch the editor freely instead.
        codeArea = new RSyntaxTextArea();
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setAutoIndentEnabled(true);
        codeArea.setTabSize(4);
        codeArea.setFont(codeArea.getFont().deriveFont(MainForm.FONT_SIZE));

        codeArea.setText("// SpEL 表达式搜索\n" +
                "// 支持 // 单行注释\n" +
                "// 所有条件之间是 AND 关系\n" +
                "#method\n" +
                "        .nameContains(\"lookup\")\n" +
                "        .classNameContains(\"Context\")\n" +
                "        .isPublic(true)");

        RTextScrollPane editorScroll = new RTextScrollPane(codeArea);
        editorScroll.setLineNumbersEnabled(true);
        editorScroll.setFoldIndicatorEnabled(true);
        editorScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        // Allow the scroll pane (and therefore the editor) to be shrunk
        // by the user via the split divider, all the way down to a
        // sensible minimum. Without this, the scroll pane reports its
        // viewport's preferred size and the JSplitPane locks up.
        editorScroll.setMinimumSize(new Dimension(0, 0));

        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(C_BG_PANEL);
        editorPanel.add(makeHeader("SPEL Expression", null), BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        // Pick a small but non-zero floor so a determined user can
        // still grab the divider; the header + caret bar take ~40px,
        // leaving ~40px of editor visible at the minimum.
        editorPanel.setMinimumSize(new Dimension(120, 80));

        // editor-local quick info bar (caret / length)
        caretLabel = new JLabel("Ln 1, Col 1   ·   0 chars");
        caretLabel.setFont(caretLabel.getFont().deriveFont(Font.PLAIN, 11f));
        caretLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
        editorPanel.add(caretLabel, BorderLayout.SOUTH);

        // install autocomplete
        new ELAutoCompleteProvider(codeArea);

        // ============================================================
        // Output console (center-bottom)
        // ============================================================
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBackground(C_BG_CONSOLE);
        outputArea.setForeground(C_FG_CONSOLE);
        outputArea.setCaretColor(C_FG_CONSOLE);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        outputScroll.getViewport().setBackground(C_BG_CONSOLE);
        outputScroll.setMinimumSize(new Dimension(0, 0));

        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBackground(C_BG_PANEL);
        consolePanel.add(makeHeader("Output", SvgManager.ElConsoleIcon), BorderLayout.NORTH);
        consolePanel.add(outputScroll, BorderLayout.CENTER);
        consolePanel.setMinimumSize(new Dimension(120, 60));
        // Keep a small initial preferred height so the editor gets the
        // bulk of the vertical space on first show, while still being
        // small enough that the user can grow it via the divider.
        consolePanel.setPreferredSize(new Dimension(120, 140));

        JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, consolePanel);
        vertSplit.setResizeWeight(0.78);
        vertSplit.setDividerSize(6);
        vertSplit.setOneTouchExpandable(true);
        // continuousLayout makes the drag feel native (and is what
        // IntelliJ's tool-window splitters do); without it the panes
        // only repaint on mouse-release.
        vertSplit.setContinuousLayout(true);
        vertSplit.setBorder(null);
        UIPrefs.bindSplit(vertSplit, K_SPLIT_EL_VERT);

        // ============================================================
        // Template browser (left)
        // ============================================================
        templateRoot = new DefaultMutableTreeNode("templates");
        templateModel = new DefaultTreeModel(templateRoot);
        rebuildTemplateTree(null);

        templateTree = new JTree(templateModel);
        templateTree.setRootVisible(false);
        templateTree.setShowsRootHandles(true);
        templateTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        templateTree.setToggleClickCount(2);
        templateTree.setRowHeight(22);
        templateTree.setBorder(new EmptyBorder(2, 2, 2, 2));
        ToolTipManager.sharedInstance().registerComponent(templateTree);
        templateTree.setCellRenderer(new TemplateTreeRenderer());

        // ---- click semantics --------------------------------------------
        // Per user request: clicking a template = replacing the editor
        // contents (the previous template is dropped). This matches
        // IDEA's Live Template preview behaviour, and avoids the common
        // surprise of accidentally piling N templates on top of each
        // other when browsing.
        templateTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                replaceFromSelection(false);
            }
        });
        // Single-click on an already-selected leaf also re-applies the
        // template -- handy when the user just edited the editor and
        // wants to quickly reset to the snippet.
        templateTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath p = templateTree.getPathForLocation(e.getX(), e.getY());
                if (p == null) {
                    return;
                }
                templateTree.setSelectionPath(p);
                if (e.getClickCount() >= 2) {
                    replaceFromSelection(true);
                }
            }
        });
        templateTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    replaceFromSelection(true);
                    e.consume();
                }
            }
        });

        templateFilter = new JTextField();
        templateFilter.setToolTipText("按名称或表达式片段过滤模板");
        templateFilter.putClientProperty("JTextField.placeholderText", "过滤模板...");
        templateFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuildTemplateTree(templateFilter.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuildTemplateTree(templateFilter.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuildTemplateTree(templateFilter.getText());
            }
        });

        JPanel filterRow = new JPanel(new BorderLayout(4, 0));
        filterRow.setBackground(C_BG_PANEL);
        filterRow.setBorder(new EmptyBorder(4, 4, 4, 4));
        JLabel filterIcon = new JLabel(SvgManager.ElFilterIcon);
        filterIcon.setBorder(new EmptyBorder(0, 2, 0, 4));
        filterRow.add(filterIcon, BorderLayout.WEST);
        filterRow.add(templateFilter, BorderLayout.CENTER);

        JButton appendBtn = new JButton("追加到光标处");
        appendBtn.setToolTipText("把当前选中的模板插入到光标处而不是替换");
        appendBtn.setIcon(SvgManager.ElSnippetIcon);
        appendBtn.setFocusPainted(false);
        appendBtn.addActionListener(e -> appendFromSelection());

        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(C_BG_PANEL);
        leftPanel.add(makeHeader("Live Templates", SvgManager.ElTemplateIcon), BorderLayout.NORTH);

        JPanel leftCenter = new JPanel(new BorderLayout(0, 0));
        leftCenter.setBackground(C_BG_PANEL);
        leftCenter.add(filterRow, BorderLayout.NORTH);
        JScrollPane treeScroll = new JScrollPane(templateTree);
        treeScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        treeScroll.setMinimumSize(new Dimension(0, 0));
        leftCenter.add(treeScroll, BorderLayout.CENTER);
        leftPanel.add(leftCenter, BorderLayout.CENTER);

        JPanel templateActions = new JPanel(new BorderLayout(0, 0));
        templateActions.setBorder(new EmptyBorder(4, 4, 4, 4));
        templateActions.setBackground(C_BG_PANEL);
        templateActions.add(appendBtn, BorderLayout.CENTER);
        leftPanel.add(templateActions, BorderLayout.SOUTH);

        // Allow the left pane to shrink down to its minimum (so users
        // who want a near-fullscreen editor can collapse it via the
        // divider) while preserving a sensible initial width.
        leftPanel.setMinimumSize(new Dimension(140, 0));
        leftPanel.setPreferredSize(new Dimension(260, 400));

        expandAllRows(templateTree);

        // ============================================================
        // Main split (left | center)
        // ============================================================
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, vertSplit);
        mainSplit.setResizeWeight(0.22);
        mainSplit.setDividerSize(6);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setContinuousLayout(true);
        mainSplit.setBorder(null);
        UIPrefs.bindSplit(mainSplit, K_SPLIT_EL_LEFT);

        add(mainSplit, BorderLayout.CENTER);

        // ============================================================
        // South: status bar + progress
        // ============================================================
        msgLabel = new JLabel("ready");
        msgLabel.setFont(msgLabel.getFont().deriveFont(Font.PLAIN, 12f));
        msgLabel.setBorder(new EmptyBorder(2, 6, 2, 6));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBorder(new EmptyBorder(0, 4, 4, 4));

        JPanel southPanel = new JPanel(new BorderLayout(0, 0));
        southPanel.setBackground(C_BG_HEADER);
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        southPanel.add(msgLabel, BorderLayout.NORTH);
        southPanel.add(progressBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // ============================================================
        // Listeners (caret / toolbar buttons that don't depend on engine)
        // ============================================================
        codeArea.addCaretListener((CaretEvent e) -> updateCaretLabel());
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCaretLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCaretLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCaretLabel();
            }
        });

        formatBtn.addActionListener(e -> formatExpression());
        clearBtn.addActionListener(e -> codeArea.setText(""));
        copyBtn.addActionListener(e -> {
            StringSelection sel = new StringSelection(codeArea.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            appendOutput("INFO ", "已复制到系统剪贴板 (" + codeArea.getText().length() + " chars)");
        });
        clearLogBtn.addActionListener(e -> outputArea.setText(""));

        // welcome banner
        appendOutput("INFO ", "EL search workbench ready. 点击左侧模板即可替换当前表达式。");
    }

    // ====================================================================
    // Helpers -- toolbar / header construction
    // ====================================================================

    private static JButton makeIconBtn(String text, String tip, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setToolTipText(tip);
        b.setFocusPainted(false);
        b.setIconTextGap(4);
        b.setMargin(new Insets(2, 8, 2, 8));
        return b;
    }

    /**
     * Builds a small section header label in IDEA's "tool window
     * heading" style: light grey background, 11px bold caption, optional
     * leading icon.
     */
    private static JComponent makeHeader(String text, Icon icon) {
        JLabel l = new JLabel(text);
        l.setForeground(C_FG_HEADER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        if (icon != null) {
            l.setIcon(icon);
            l.setIconTextGap(4);
        }
        l.setOpaque(true);
        l.setBackground(C_BG_HEADER);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(3, 6, 3, 6)));
        return l;
    }

    // ====================================================================
    // Template tree
    // ====================================================================

    /**
     * Rebuilds the grouped template tree. Group key is taken from the
     * leading {@code 【...】} marker; entries without one fall under
     * "其他".
     *
     * @param filter case-insensitive substring; both the visible name and
     *               the underlying template body are matched. May be null.
     */
    private void rebuildTemplateTree(String filter) {
        templateRoot.removeAllChildren();
        String f = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);

        LinkedHashMap<String, DefaultMutableTreeNode> groups = new LinkedHashMap<>();

        for (Map.Entry<String, String> e : Templates.data.entrySet()) {
            String key = e.getKey();
            String body = e.getValue();
            if (!f.isEmpty()) {
                String hay = (key + "\n" + body).toLowerCase(Locale.ROOT);
                if (!hay.contains(f)) {
                    continue;
                }
            }
            String group;
            String label;
            if (key.startsWith("【") && key.indexOf('】') > 0) {
                int end = key.indexOf('】');
                group = key.substring(1, end);
                label = key.substring(end + 1).trim();
                if (label.isEmpty()) {
                    label = key;
                }
            } else {
                group = "其他";
                label = key;
            }
            DefaultMutableTreeNode g = groups.get(group);
            if (g == null) {
                g = new DefaultMutableTreeNode(group);
                groups.put(group, g);
            }
            g.add(new DefaultMutableTreeNode(new TemplateLeaf(key, label, body)));
        }
        for (DefaultMutableTreeNode g : groups.values()) {
            templateRoot.add(g);
        }
        templateModel.reload();
        if (templateTree != null) {
            expandAllRows(templateTree);
        }
    }

    private static void expandAllRows(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    /**
     * Replaces the editor content with the currently selected template's
     * body, dropping whatever was there before. Intentionally silent on
     * group nodes so the user can browse categories without nuking their
     * draft.
     *
     * @param logEvent when {@code true}, also emits an INFO line to the
     *                 output console; selection-change driven calls pass
     *                 false to keep the log quiet during arrow-key
     *                 navigation.
     */
    private void replaceFromSelection(boolean logEvent) {
        TemplateLeaf leaf = currentLeaf();
        if (leaf == null) {
            return;
        }
        codeArea.setText(leaf.body == null ? "" : leaf.body);
        codeArea.setCaretPosition(0);
        if (logEvent) {
            appendOutput("INFO ", "已切换到模板: " + leaf.fullKey);
        }
    }

    private void appendFromSelection() {
        TemplateLeaf leaf = currentLeaf();
        if (leaf == null || leaf.body == null) {
            return;
        }
        int caret = codeArea.getCaretPosition();
        try {
            String existing = codeArea.getText();
            String prefix = (caret > 0 && existing.charAt(caret - 1) != '\n') ? "\n" : "";
            codeArea.getDocument().insertString(caret, prefix + leaf.body + "\n", null);
        } catch (BadLocationException ex) {
            codeArea.setText(leaf.body);
        }
        appendOutput("INFO ", "已追加模板: " + leaf.fullKey);
        codeArea.requestFocusInWindow();
    }

    private TemplateLeaf currentLeaf() {
        TreePath path = templateTree.getSelectionPath();
        if (path == null) {
            return null;
        }
        Object last = path.getLastPathComponent();
        if (!(last instanceof DefaultMutableTreeNode)) {
            return null;
        }
        Object user = ((DefaultMutableTreeNode) last).getUserObject();
        return (user instanceof TemplateLeaf) ? (TemplateLeaf) user : null;
    }

    /**
     * Holds a leaf entry's metadata so the tree can show a short label
     * while the full template body stays accessible for insertion and
     * tooltip preview.
     */
    private static final class TemplateLeaf {
        final String fullKey;
        final String label;
        final String body;

        TemplateLeaf(String fullKey, String label, String body) {
            this.fullKey = fullKey;
            this.label = label;
            this.body = body;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Renderer that exposes a tooltip preview (first ~12 lines of the
     * template body) on hover and assigns IDEA-flavoured icons to
     * group / leaf nodes.
     */
    private static final class TemplateTreeRenderer extends DefaultTreeCellRenderer {
        TemplateTreeRenderer() {
            // hide the default folder/leaf icons -- we paint our own
            setLeafIcon(null);
            setOpenIcon(null);
            setClosedIcon(null);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            JLabel c = (JLabel) super.getTreeCellRendererComponent(
                    tree, value, sel, expanded, leaf, row, hasFocus);
            c.setBorder(new EmptyBorder(1, 2, 1, 6));
            if (value instanceof DefaultMutableTreeNode) {
                Object user = ((DefaultMutableTreeNode) value).getUserObject();
                if (user instanceof TemplateLeaf) {
                    TemplateLeaf t = (TemplateLeaf) user;
                    c.setIcon(SvgManager.ElSnippetIcon);
                    c.setToolTipText(buildTooltip(t.fullKey, t.body));
                } else {
                    c.setIcon(SvgManager.ElGroupIcon);
                    c.setToolTipText(null);
                    if (user != null) {
                        c.setText(user.toString());
                    }
                }
            }
            return c;
        }

        private String buildTooltip(String key, String body) {
            if (body == null) {
                return key;
            }
            String[] lines = body.split("\n", -1);
            int max = Math.min(12, lines.length);
            StringBuilder sb = new StringBuilder("<html><b>")
                    .append(escape(key)).append("</b><pre style='margin:4px 0 0 0'>");
            for (int i = 0; i < max; i++) {
                sb.append(escape(lines[i])).append("\n");
            }
            if (lines.length > max) {
                sb.append("...");
            }
            sb.append("</pre></html>");
            return sb.toString();
        }

        private String escape(String s) {
            return s == null ? "" :
                    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    // ====================================================================
    // Editor utilities
    // ====================================================================

    private void updateCaretLabel() {
        try {
            int caret = codeArea.getCaretPosition();
            int line = codeArea.getLineOfOffset(caret);
            int col = caret - codeArea.getLineStartOffset(line);
            int total = codeArea.getDocument().getLength();
            caretLabel.setText("Ln " + (line + 1) + ", Col " + (col + 1)
                    + "   ·   " + total + " chars");
        } catch (BadLocationException ignored) {
        }
    }

    /**
     * Pretty-prints the current expression: keeps comment lines intact,
     * folds whitespace so each {@code .xxx(..)} call lands on its own
     * line indented by 8 spaces, matching the in-tree templates.
     */
    private void formatExpression() {
        String src = codeArea.getText();
        if (src == null || src.isEmpty()) {
            return;
        }
        StringBuilder out = new StringBuilder(src.length() + 64);

        StringBuilder code = new StringBuilder();
        for (String raw : src.split("\n", -1)) {
            String t = raw.trim();
            if (t.startsWith("//")) {
                if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') {
                    out.append('\n');
                }
                out.append(t).append('\n');
            } else {
                code.append(t).append(' ');
            }
        }

        String collapsed = code.toString().replaceAll("\\s+", " ").trim();
        if (collapsed.isEmpty()) {
            codeArea.setText(out.toString());
            return;
        }

        java.util.List<String> parts = splitTopLevelDot(collapsed);
        boolean first = true;
        for (String p : parts) {
            String tok = p.trim();
            if (tok.isEmpty()) {
                continue;
            }
            if (first) {
                out.append(tok);
                first = false;
            } else {
                out.append("\n        .").append(tok);
            }
        }
        codeArea.setText(out.toString());
        appendOutput("INFO ", "已格式化表达式");
    }

    /**
     * Splits {@code expr} on every top-level {@code .} -- i.e. dots that
     * sit outside string literals and parentheses. The leading segment
     * (typically {@code #method}) becomes the first element; subsequent
     * elements are the call chain entries (without the leading dot).
     */
    private static java.util.List<String> splitTopLevelDot(String expr) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        boolean inStr = false;
        boolean escape = false;
        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if (escape) {
                cur.append(ch);
                escape = false;
                continue;
            }
            if (inStr) {
                cur.append(ch);
                if (ch == '\\') {
                    escape = true;
                } else if (ch == '"') {
                    inStr = false;
                }
                continue;
            }
            if (ch == '"') {
                inStr = true;
                cur.append(ch);
                continue;
            }
            if (ch == '(') {
                depth++;
                cur.append(ch);
                continue;
            }
            if (ch == ')') {
                if (depth > 0) depth--;
                cur.append(ch);
                continue;
            }
            if (ch == '.' && depth == 0) {
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out;
    }

    // ====================================================================
    // Output console
    // ====================================================================

    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Append a timestamped line to the output console. Thread-safe; call
     * from any thread, the actual document mutation happens on the EDT.
     */
    public void appendOutput(String level, String text) {
        Runnable r = () -> {
            String ts = TS_FMT.format(new Date());
            outputArea.append("[" + ts + "] " + level + " " + text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    // ====================================================================
    // Public getters (kept identical to previous version)
    // ====================================================================

    public RSyntaxTextArea getCodeArea() {
        return codeArea;
    }

    public JButton getCheckButton() {
        return checkButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getStopBtn() {
        return stopBtn;
    }

    public JButton getExportBtn() {
        return exportBtn;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getMsgLabel() {
        return msgLabel;
    }

    /**
     * Output console accessor; lets {@link me.n1ar4.jar.analyzer.el.ELForm}
     * report search lifecycle events without coupling to widget internals.
     */
    public JTextArea getOutputArea() {
        return outputArea;
    }
}
