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

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.util.CtrlClickNavigator;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * "All Strings" 浏览窗口（纯代码 UI 版）。
 * <p>
 * 功能：
 * <ul>
 *   <li>左侧 JList 渲染当前页字符串，等宽字体 + 斑马纹；</li>
 *   <li>顶部工具栏：实时过滤 + 跳转到指定页；</li>
 *   <li>右侧详情区：完整展示选中字符串，避免被列表截断；</li>
 *   <li>双击 / Enter：跳转到包含该字符串的方法（唯一结果直跳，多结果灌到 SEARCH 面板）；</li>
 *   <li>右键菜单：精确反查 / 模糊反查 / 复制；</li>
 *   <li>底部状态栏：Total / Page / Prev / Next / Close。</li>
 * </ul>
 */
public class ShowStringForm {
    private static final Logger logger = LogManager.getLogger();

    /**
     * 与 CoreEngine.getStrings 的 LIMIT 100 保持一致。
     */
    private static final int PAGE_SIZE = 100;

    private final JFrame frame;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    /**
     * 完整的当前页数据（未过滤）。
     */
    private final List<String> rawPageData = new ArrayList<>();
    private final JList<String> stringList = new JList<>(listModel);
    private final JTextField filterField = new JTextField();
    private final JTextArea detailArea = new JTextArea();
    private final JLabel totalLabel = new JLabel();
    private final JLabel pageLabel = new JLabel();
    private final JSpinner pageSpinner;
    private final JButton prevBtn = new JButton("上一页");
    private final JButton nextBtn = new JButton("下一页");

    private int curPage = 1;
    private int totalPage = 1;
    private final int totalCount;

    public static void start(int total, ArrayList<String> firstPage, JDialog progressDialog) {
        SwingUtilities.invokeLater(() -> {
            ShowStringForm form = new ShowStringForm(total);
            form.applyPageData(firstPage, 1);
            if (progressDialog != null) {
                progressDialog.dispose();
            }
            form.showWindow();
        });
    }

    private ShowStringForm(int totalCount) {
        this.totalCount = Math.max(0, totalCount);
        this.totalPage = Math.max(1, (totalCount + PAGE_SIZE - 1) / PAGE_SIZE);

        this.pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, totalPage, 1));

        this.frame = new JFrame(Const.StringForm + "  -  All Strings");
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.frame.setSize(960, 640);
        this.frame.setMinimumSize(new Dimension(720, 480));

        // 关键：setLocationRelativeTo 必须传一个真正的 Window/Frame，而不是 JPanel；
        // 传 JPanel 时 Swing 会把它当作 "owner-less" 处理，再叠加 LAF 行为，
        // 在 Windows 上常常出现新窗口被压在主窗口下面的现象。
        Window owner = resolveMainFrame();
        this.frame.setLocationRelativeTo(owner);

        this.frame.setContentPane(buildRoot());

        installShortcuts();
    }

    /**
     * 找到主窗口的 JFrame；找不到返回 null（让 Swing 居中到屏幕）。
     */
    private static Window resolveMainFrame() {
        try {
            MainForm mf = MainForm.getInstance();
            if (mf == null || mf.getMasterPanel() == null) {
                return null;
            }
            Window w = SwingUtilities.getWindowAncestor(mf.getMasterPanel());
            return w; // 可能为 null
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 显式确保窗口出现在最前面。
     * <p>
     * Swing 在 Windows 平台上有个老问题：从一个非聚焦窗口调用 setVisible(true) 时，
     * 系统不保证新窗口被置顶（任务栏闪烁但窗口被压在主窗口之下）。
     * 这里使用 setVisible -> toFront -> requestFocus 的组合，
     * 同时短暂打开 alwaysOnTop 再关掉，作为兜底保证一定能浮到最前。
     */
    private void showWindow() {
        frame.setVisible(true);
        // 兜底：极少数 LAF/平台下 toFront 不生效，临时 alwaysOnTop 一帧
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        // 下一帧关闭 alwaysOnTop，避免长期凌驾其它窗口
        SwingUtilities.invokeLater(() -> frame.setAlwaysOnTop(false));
        // 把焦点交给列表，方便键盘直接操作
        SwingUtilities.invokeLater(stringList::requestFocusInWindow);
    }

    // ---------------------------------------------------------------------
    // 布局
    // ---------------------------------------------------------------------

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 12, 10, 12));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSplit(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));

        // 左：过滤
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel fl = new JLabel("过滤当前页:");
        left.add(fl);
        filterField.setColumns(28);
        filterField.setToolTipText("输入关键词实时过滤当前页（不区分大小写，仅子串匹配）");
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        left.add(filterField);
        JButton clearBtn = new JButton("清空");
        clearBtn.addActionListener(e -> filterField.setText(""));
        left.add(clearBtn);
        bar.add(left, BorderLayout.WEST);

        // 右：跳转到第 N 页
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.add(new JLabel("跳到第"));
        ((JSpinner.DefaultEditor) pageSpinner.getEditor()).getTextField().setColumns(5);
        right.add(pageSpinner);
        right.add(new JLabel("页 / 共 " + totalPage + " 页"));
        JButton goBtn = new JButton("跳转");
        goBtn.addActionListener(e -> gotoPage(((Number) pageSpinner.getValue()).intValue()));
        right.add(goBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JSplitPane buildSplit() {
        // 左侧列表
        stringList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stringList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        stringList.setCellRenderer(new ZebraStringRenderer());
        stringList.setToolTipText("<html>双击 / Enter：跳转到包含该字符串的方法<br/>"
                + "右键：精确反查 / 模糊反查 / 复制</html>");
        stringList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String s = stringList.getSelectedValue();
                detailArea.setText(s == null ? "" : s);
                detailArea.setCaretPosition(0);
            }
        });
        stringList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    String s = stringList.getSelectedValue();
                    if (s != null) {
                        jumpByString(s);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            private void maybeShowMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int idx = stringList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        stringList.setSelectedIndex(idx);
                    }
                    String s = stringList.getSelectedValue();
                    if (s != null) {
                        showContextMenu(e, s);
                    }
                }
            }
        });
        JScrollPane leftScroll = new JScrollPane(stringList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("字符串列表"));

        // 右侧详情
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane rightScroll = new JScrollPane(detailArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("详情（选中即显示，便于查看长字符串）"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        split.setResizeWeight(0.65);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        return split;
    }

    private JComponent buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));

        // 左：状态信息
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        totalLabel.setText("Total: " + totalCount);
        totalLabel.setForeground(new Color(0x1A, 0x7F, 0x37));
        left.add(totalLabel);
        left.add(pageLabel);
        bar.add(left, BorderLayout.WEST);

        // 右：翻页 + 关闭
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        prevBtn.addActionListener(e -> gotoPage(curPage - 1));
        nextBtn.addActionListener(e -> gotoPage(curPage + 1));
        right.add(prevBtn);
        right.add(nextBtn);
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> frame.dispose());
        right.add(closeBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private void installShortcuts() {
        JRootPane rp = frame.getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc-close");
        am.put("esc-close", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                frame.dispose();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "next-page");
        am.put("next-page", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                gotoPage(curPage + 1);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "prev-page");
        am.put("prev-page", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                gotoPage(curPage - 1);
            }
        });

        // Enter on list -> jump
        InputMap listIm = stringList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap listAm = stringList.getActionMap();
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "jump");
        listAm.put("jump", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String s = stringList.getSelectedValue();
                if (s != null) jumpByString(s);
            }
        });
        // Ctrl+C on list -> copy
        int menuShortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        listIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut), "copy");
        listAm.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String s = stringList.getSelectedValue();
                if (s != null) copyToClipboard(s);
            }
        });
    }

    // ---------------------------------------------------------------------
    // 翻页 / 过滤 / 数据装载
    // ---------------------------------------------------------------------

    private void gotoPage(int page) {
        if (page < 1 || page > totalPage) {
            return;
        }
        if (page == curPage) {
            return;
        }
        if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
            JOptionPane.showMessageDialog(frame, "please start engine first");
            return;
        }
        // 异步取数据，避免大库阻塞 EDT
        new Thread(() -> {
            try {
                ArrayList<String> data = MainForm.getEngine().getStrings(page);
                SwingUtilities.invokeLater(() -> applyPageData(data, page));
            } catch (Exception ex) {
                logger.error("load page error: {}", ex.toString());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        frame, "加载失败：" + ex.getMessage()));
            }
        }).start();
    }

    private void applyPageData(List<String> data, int page) {
        rawPageData.clear();
        if (data != null) {
            for (String s : data) {
                if (s != null) {
                    rawPageData.add(s);
                }
            }
        }
        curPage = page;
        applyFilter();
        pageLabel.setText("Page: " + curPage + " / " + totalPage);
        pageSpinner.setValue(curPage);
        prevBtn.setEnabled(curPage > 1);
        nextBtn.setEnabled(curPage < totalPage);
        if (!listModel.isEmpty()) {
            stringList.setSelectedIndex(0);
            stringList.ensureIndexIsVisible(0);
        } else {
            detailArea.setText("");
        }
    }

    /**
     * 根据 filterField 内容过滤 rawPageData，写入 listModel。
     */
    private void applyFilter() {
        String kw = filterField.getText();
        listModel.clear();
        if (kw == null || kw.isEmpty()) {
            for (String s : rawPageData) {
                listModel.addElement(s);
            }
        } else {
            String lower = kw.toLowerCase();
            for (String s : rawPageData) {
                if (s.toLowerCase().contains(lower)) {
                    listModel.addElement(s);
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // 跳转 / 反查 / 右键菜单
    // ---------------------------------------------------------------------

    /**
     * 双击/Enter：精确匹配反查。
     * 0 个 → 提示；1 个 → 直接跳转；多个 → 灌到 SEARCH 面板。
     */
    private void jumpByString(String value) {
        if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
            JOptionPane.showMessageDialog(frame, "please start engine first");
            return;
        }
        new Thread(() -> {
            try {
                ArrayList<MethodResult> results = MainForm.getEngine().getMethodsByStrEqual(value);
                int size = results == null ? 0 : results.size();
                if (size == 0) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            frame,
                            "未找到包含该字符串的方法（精确匹配）：\n" + abbreviate(value, 200)));
                    return;
                }
                if (size == 1) {
                    SwingUtilities.invokeLater(() ->
                            CtrlClickNavigator.jumpToMethod(results.get(0)));
                    return;
                }
                // 多结果：refreshStrSearchEqual 会把结果灌到 SEARCH 面板并切 Tab
                CoreHelper.refreshStrSearchEqual(null, value, null);
            } catch (Exception ex) {
                logger.error("jump by string error: {}", ex.toString());
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        frame, "跳转失败：" + ex.getMessage()));
            }
        }).start();
    }

    private void showContextMenu(MouseEvent e, String value) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem jumpItem = new JMenuItem("跳转到方法（精确匹配）");
        jumpItem.addActionListener(ev -> jumpByString(value));
        menu.add(jumpItem);

        JMenuItem strictItem = new JMenuItem("在 SEARCH 面板精确反查");
        strictItem.addActionListener(ev ->
                new Thread(() -> CoreHelper.refreshStrSearchEqual(null, value, null)).start());
        menu.add(strictItem);

        JMenuItem fuzzyItem = new JMenuItem("在 SEARCH 面板模糊反查");
        fuzzyItem.addActionListener(ev ->
                new Thread(() -> CoreHelper.refreshStrSearch(null, value, null)).start());
        menu.add(fuzzyItem);

        menu.addSeparator();

        JMenuItem copyItem = new JMenuItem("复制字符串");
        copyItem.addActionListener(ev -> copyToClipboard(value));
        menu.add(copyItem);

        menu.show(stringList, e.getX(), e.getY());
    }

    private static void copyToClipboard(String s) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(s), null);
        } catch (Exception ignored) {
            // 复制失败不影响主流程
        }
    }

    private static String abbreviate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    // ---------------------------------------------------------------------
    // 斑马纹列表渲染器
    // ---------------------------------------------------------------------

    private static class ZebraStringRenderer extends DefaultListCellRenderer {
        private static final Color ZEBRA = new Color(0xF6, 0xF8, 0xFA);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            c.setBorder(new EmptyBorder(2, 6, 2, 6));
            if (!isSelected) {
                c.setBackground(index % 2 == 0 ? list.getBackground() : ZEBRA);
            }
            // 列表渲染时把字符串里的 tab/newline 可视化，避免渲染混乱
            if (value instanceof String) {
                String s = (String) value;
                if (s.indexOf('\n') >= 0 || s.indexOf('\t') >= 0 || s.indexOf('\r') >= 0) {
                    c.setText(s.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"));
                }
            }
            return c;
        }
    }
}
