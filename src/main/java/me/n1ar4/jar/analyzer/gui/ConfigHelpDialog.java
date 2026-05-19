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

import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.gui.util.MenuUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置说明与设置面板（全中文）。
 * <p>
 * 所有 config 菜单项 + partition 批量大小，统一在此面板配置并即时生效。
 * 说明文字按短句分多行显示，每行内部不自动换行（{@code <nobr>}），行数稳定。
 */
public class ConfigHelpDialog extends JDialog {

    /**
     * 默认数据库批量分片大小（与 {@link DatabaseManager#PART_SIZE} 初始值保持一致）。
     */
    private static final int DEFAULT_PART_SIZE = 100;
    /**
     * 允许设置的分片大小范围，避免用户填写非法/极端值。
     */
    private static final int MIN_PART_SIZE = 1;
    private static final int MAX_PART_SIZE = 100_000;

    private static volatile ConfigHelpDialog INSTANCE;

    /**
     * 打开（或聚焦）面板。
     */
    public static void start() {
        SwingUtilities.invokeLater(() -> {
            JFrame parent = null;
            if (MainForm.getInstance() != null
                    && MainForm.getInstance().getMasterPanel() != null) {
                Window w = SwingUtilities.getWindowAncestor(MainForm.getInstance().getMasterPanel());
                if (w instanceof JFrame) {
                    parent = (JFrame) w;
                }
            }
            if (INSTANCE != null && INSTANCE.isDisplayable()) {
                INSTANCE.toFront();
                INSTANCE.requestFocus();
                return;
            }
            INSTANCE = new ConfigHelpDialog(parent);
            INSTANCE.setVisible(true);
        });
    }

    private ConfigHelpDialog(JFrame parent) {
        super(parent, "配置说明与设置", false);

        setSize(900, 720);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 12, 10, 12));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildScrollableContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ---------------------------------------------------------------------
    // 头 / 滚动内容 / 尾
    // ---------------------------------------------------------------------

    private JComponent buildHeader() {
        JLabel title = new JLabel(
                "<html><b style='font-size:13pt'>配置说明与设置</b><br/>" +
                        "<span style='color:gray'>所有改动即时生效，无需手动保存。" +
                        "" + "下列说明基于实际代码行为，可放心参考。</span></html>");
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        return title;
    }

    private JScrollPane buildScrollableContent() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (JPanel card : buildCards()) {
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(card);
            list.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)));
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        return footer;
    }

    // ---------------------------------------------------------------------
    // 卡片定义（语义已对照实际代码核实）
    // ---------------------------------------------------------------------

    private List<JPanel> buildCards() {
        List<JPanel> cards = new ArrayList<>();

        // 1. 文件树是否显示内部类（仅影响左侧文件树渲染）
        cards.add(buildBoolCard(
                "文件树显示内部类",
                "show inner class",
                "默认：关闭",
                new String[]{
                        "控制左侧 “文件树 / 类树” 是否展示带 $ 符号的内部类与匿名类文件。",
                        "实现位置：TreeFileFilter，仅做 GUI 树过滤，不影响搜索结果与数据库。",
                        "关闭时 Foo$1.class、Foo$Bar.class 这类条目会被隐藏。"
                },
                new String[]{
                        "默认关闭，结果更整洁；",
                        "需要查看 Lambda 编译产物或匿名类时再开启。"
                },
                MenuUtil.getShowInnerConfig().getState(),
                MenuUtil::setShowInner
        ));

        // 2. fix class path —— 实际是 BOOT-INF/WEB-INF 的类路径处理方式
        cards.add(buildBoolCard(
                "修正 BOOT-INF/WEB-INF 类路径",
                "fix class path",
                "默认：关闭",
                new String[]{
                        "控制 SpringBoot fat-jar / WAR 中 BOOT-INF 或 WEB-INF 下类的处理方式：",
                        "· 关闭：仅按字符串截断 “…/classes/” 之前的部分，速度快、轻量。",
                        "· 开启：用 ASM 解析每个 class 的真实全限定名，并把字节码复制到临时目录的真实包路径下。",
                        "实现位置：CoreRunner，开启后会落盘到 jar-analyzer-temp/<包路径>/Xxx.class。"
                },
                new String[]{
                        "普通 jar 关闭即可；",
                        "分析 SpringBoot fat-jar、WAR 或嵌套结构、且后续要反编译/查看源码时建议开启。"
                },
                MenuUtil.getFixClassPathConfig().getState(),
                MenuUtil::setFixClassPath
        ));

        // 3. 方法搜索结果排序方式（互斥单选）
        cards.add(buildSortCard());

        // 4. 方法实现/重写修复（互斥单选；构建期一次性写库行为）
        cards.add(buildFixImplCard());

        // 5. SQL 日志（实际只记录 query 等，跳过 INSERT/CREATE）
        cards.add(buildBoolCard(
                "记录 SQL 查询日志",
                "save all sql statement",
                "默认：关闭",
                new String[]{
                        "MyBatis 拦截器（PrintSqlInterceptor）的 SQL 日志总开关。",
                        "关闭时拦截器直接放行，不输出任何 SQL；",
                        "开启时也仅记录 SELECT 等查询语句，构建数据库阶段的大量 INSERT / CREATE 不会被记录（避免拖慢入库）。",
                        "日志会写入 sql 日志文件。"
                },
                new String[]{
                        "排查查询性能或调试 mapper 时开启；",
                        "日常使用关闭，减少 IO 与磁盘占用。"
                },
                MenuUtil.getLogAllSqlConfig().getState(),
                MenuUtil::setLogAllSql
        ));

        // 6. partition 批量分片大小（直接在面板里编辑，不再走单独窗口）
        cards.add(buildPartitionCard());

        return cards;
    }

    /**
     * 通用布尔型卡片。
     */
    private JPanel buildBoolCard(String zhTitle,
                                 String enKey,
                                 String defaultText,
                                 String[] descLines,
                                 String[] adviceLines,
                                 boolean initState,
                                 BoolSetter setter) {
        JPanel card = newCardPanel(zhTitle + "  ( " + enKey + " )");

        JCheckBox cb = new JCheckBox("启用此项");
        cb.setSelected(initState);
        cb.addActionListener(e -> setter.set(cb.isSelected()));

        card.add(buildControlRow(defaultText, cb), BorderLayout.NORTH);
        card.add(buildBody(descLines, adviceLines), BorderLayout.CENTER);
        return card;
    }

    /**
     * 排序方式（互斥单选）。
     */
    private JPanel buildSortCard() {
        JPanel card = newCardPanel("方法搜索结果排序  ( sort results )");

        JRadioButton byClass = new JRadioButton("按类名排序");
        JRadioButton byMethod = new JRadioButton("按方法名排序");
        ButtonGroup group = new ButtonGroup();
        group.add(byClass);
        group.add(byMethod);
        if (MenuUtil.sortedByMethod()) {
            byMethod.setSelected(true);
        } else {
            byClass.setSelected(true);
        }
        byClass.addActionListener(e -> {
            if (byClass.isSelected()) MenuUtil.setSortedByMethod(false);
        });
        byMethod.addActionListener(e -> {
            if (byMethod.isSelected()) MenuUtil.setSortedByMethod(true);
        });

        card.add(buildControlRow("默认：按类名排序", byClass, byMethod), BorderLayout.NORTH);
        card.add(buildBody(
                new String[]{
                        "对方法搜索结果（Callers / Callees / Impls 等返回的 MethodResult 列表）做内存排序。",
                        "实现位置：CoreHelper，使用 Java 端 ArrayList.sort + Comparator，与数据库 ORDER BY 无关。",
                        "· 按类名排序：以类的全限定名为序，便于按包结构浏览。",
                        "· 按方法名排序：以方法名为序，便于快速定位同名方法（如 doFilter、execute）。",
                        "二者必须二选一（互斥）。"
                },
                new String[]{"默认 “按类名排序” 即可。"}
        ), BorderLayout.CENTER);
        return card;
    }

    /**
     * 方法实现/重写修复（互斥单选）。
     */
    private JPanel buildFixImplCard() {
        JPanel card = newCardPanel("方法实现/重写修复  ( fix methods impl/override )");

        JRadioButton enable = new JRadioButton("启用（推荐）");
        JRadioButton disable = new JRadioButton("禁用");
        ButtonGroup group = new ButtonGroup();
        group.add(enable);
        group.add(disable);
        if (MenuUtil.enableFixMethodImpl()) {
            enable.setSelected(true);
        } else {
            disable.setSelected(true);
        }
        enable.addActionListener(e -> {
            if (enable.isSelected()) MenuUtil.setEnableFixMethodImpl(true);
        });
        disable.addActionListener(e -> {
            if (disable.isSelected()) MenuUtil.setEnableFixMethodImpl(false);
        });

        card.add(buildControlRow("默认：启用", enable, disable), BorderLayout.NORTH);
        card.add(buildBody(
                new String[]{
                        "在 “构建数据库” 阶段是否把每个父类/接口方法的所有子类 override 方法并入它的 callee 集合，再写入库。",
                        "实现位置：CoreRunner，紧随 saveImpls 之后；这是一次性写库行为，构建完成后才决定后续查询的形态。",
                        "· 启用：调用链分析能穿透虚方法，把 override 的实际实现也视为被父方法调用，结果更全。",
                        "· 禁用：只保留字面字节码 invoke 目标，结果更精确但会漏掉运行时真正执行的实现。",
                        "注意：由于是构建期决定的，切换该项后需要重新构建数据库才能完全生效。"
                },
                new String[]{
                        "做漏洞链 / Gadget 挖掘 / 调用追踪等场景必须启用；",
                        "仅当只关心声明位置、不关心运行时实现时再禁用。"
                }
        ), BorderLayout.CENTER);
        return card;
    }

    /**
     * partition 批量分片大小（直接在面板里编辑）。
     */
    private JPanel buildPartitionCard() {
        JPanel card = newCardPanel("数据库批量插入分片大小  ( partition size )");

        // 用 JSpinner 限制范围，避免非法输入
        SpinnerNumberModel model = new SpinnerNumberModel(
                clampPartSize(DatabaseManager.PART_SIZE),
                MIN_PART_SIZE, MAX_PART_SIZE, 50);
        JSpinner spinner = new JSpinner(model);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(7);

        JButton applyBtn = new JButton("应用");
        applyBtn.addActionListener(e -> {
            Object v = spinner.getValue();
            if (v instanceof Number) {
                int newVal = clampPartSize(((Number) v).intValue());
                DatabaseManager.PART_SIZE = newVal;
                spinner.setValue(newVal);
                JOptionPane.showMessageDialog(this,
                        "已设置 partition size = " + newVal,
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton resetBtn = new JButton("恢复默认 (" + DEFAULT_PART_SIZE + ")");
        resetBtn.addActionListener(e -> {
            spinner.setValue(DEFAULT_PART_SIZE);
            DatabaseManager.PART_SIZE = DEFAULT_PART_SIZE;
        });

        JLabel current = new JLabel();
        current.setForeground(new Color(0x1A, 0x7F, 0x37));
        Runnable refreshCurrent = () ->
                current.setText("当前值: " + DatabaseManager.PART_SIZE);
        refreshCurrent.run();
        // 应用/恢复后刷新当前值显示
        applyBtn.addActionListener(e -> refreshCurrent.run());
        resetBtn.addActionListener(e -> refreshCurrent.run());

        card.add(buildControlRow("默认：" + DEFAULT_PART_SIZE,
                new JLabel("分片大小:"), spinner, applyBtn, resetBtn, current), BorderLayout.NORTH);

        card.add(buildBody(
                new String[]{
                        "控制构建数据库阶段批量 INSERT 的分片大小（DatabaseManager.PART_SIZE）。",
                        "实现位置：DatabaseManager 的所有 saveXxx 方法都会先用 PartitionUtils.partition(list, PART_SIZE) 把大列表切片，再逐片调用 mapper 的批量 INSERT。",
                        "目的是避免单条 SQL 过长 / SQLite 参数超限。",
                        "与分页查询、分表无关，仅影响入库阶段。"
                },
                new String[]{
                        "保留默认 100 即可；",
                        "分析超大 jar（百万级类/方法）且观察到入库慢时，可适当调高（如 200~500）；",
                        "如遇到 SQLite parameter 超限错误，则调低。"
                }
        ), BorderLayout.CENTER);
        return card;
    }

    private static int clampPartSize(int v) {
        if (v < MIN_PART_SIZE) return MIN_PART_SIZE;
        if (v > MAX_PART_SIZE) return MAX_PART_SIZE;
        return v;
    }

    // ---------------------------------------------------------------------
    // 共用构造工具
    // ---------------------------------------------------------------------

    private JPanel newCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC)),
                " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP);
        tb.setTitleFont(card.getFont().deriveFont(Font.BOLD, 12f));
        card.setBorder(BorderFactory.createCompoundBorder(
                tb,
                new EmptyBorder(2, 6, 6, 6)));
        return card;
    }

    /**
     * 控件行：左侧若干控件 + 右侧灰色默认值提示，使用 BorderLayout 对齐两端。
     */
    private JPanel buildControlRow(String defaultText, JComponent... ctrls) {
        JPanel row = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        for (JComponent c : ctrls) {
            left.add(c);
        }
        row.add(left, BorderLayout.WEST);

        JLabel def = new JLabel(defaultText);
        def.setForeground(new Color(0x6B, 0x6B, 0x6B));
        def.setBorder(new EmptyBorder(0, 0, 0, 6));
        row.add(def, BorderLayout.EAST);
        return row;
    }

    /**
     * 主体说明区。每行用 {@code <nobr>} 包住，行间用 {@code <br/>} 显式断行；
     * 这样窗口宽度变化时不会自动折行，行数稳定。
     */
    private JComponent buildBody(String[] descLines, String[] adviceLines) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='margin:0; font-size:11pt'>");

        // 说明
        sb.append("<nobr><b style='color:#222'>说明：</b>")
                .append(safe(descLines[0])).append("</nobr>");
        for (int i = 1; i < descLines.length; i++) {
            sb.append("<br/><nobr>").append(indent())
                    .append(safe(descLines[i])).append("</nobr>");
        }

        // 建议
        if (adviceLines != null && adviceLines.length > 0) {
            sb.append("<br/><br/><nobr><b style='color:#0550AE'>建议：</b>")
                    .append("<span style='color:#0550AE'>").append(safe(adviceLines[0])).append("</span>")
                    .append("</nobr>");
            for (int i = 1; i < adviceLines.length; i++) {
                sb.append("<br/><nobr>").append(indent())
                        .append("<span style='color:#0550AE'>").append(safe(adviceLines[i])).append("</span>")
                        .append("</nobr>");
            }
        }

        sb.append("</body></html>");

        JLabel body = new JLabel(sb.toString());
        body.setBorder(new EmptyBorder(4, 4, 2, 4));
        body.setVerticalAlignment(SwingConstants.TOP);
        return body;
    }

    private static String indent() {
        return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    @FunctionalInterface
    private interface BoolSetter {
        void set(boolean v);
    }
}
