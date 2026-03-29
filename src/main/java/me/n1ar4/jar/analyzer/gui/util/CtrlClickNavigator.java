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

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Ctrl+Click 导航器
 * 实现类似 IDEA 的 Ctrl+左键点击跳转功能
 * 弹出选择窗口，显示 caller（谁调用了它）和 callee（被调用的方法定义），
 * 用户选择后直接跳转到目标方法的源码
 */
public class CtrlClickNavigator {
    private static final Logger logger = LogManager.getLogger();

    /**
     * 根据 Ctrl+Click 选中的方法名，弹出导航选择窗口
     *
     * @param methodName 用户点击的方法名
     * @param className  当前正在查看的类名
     * @param screenX    鼠标屏幕 X 坐标（用于定位弹窗）
     * @param screenY    鼠标屏幕 Y 坐标（用于定位弹窗）
     */
    public static void navigate(String methodName, String className, int screenX, int screenY) {
        if (MainForm.getEngine() == null) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE BUILD DATABASE FIRST");
            return;
        }

        if (methodName == null || methodName.trim().isEmpty()) {
            return;
        }

        methodName = methodName.trim();

        // 处理构造方法：如果点击的是类名（短名），视为 <init>
        String finalMethodName = methodName;
        if (className != null && className.contains("/")) {
            String shortClassName = className.substring(className.lastIndexOf('/') + 1);
            if (methodName.equals(shortClassName)) {
                finalMethodName = "<init>";
            }
        } else if (className != null && methodName.equals(className)) {
            finalMethodName = "<init>";
        }

        String mn = finalMethodName;
        // 弹窗最多展示的条目数
        final int MAX_DISPLAY = 20;
        // 在后台线程查询 caller / callee
        new Thread(() -> {
            List<MethodResult> callerList = MainForm.getEngine().getCallers(
                    className, mn, null);
            List<MethodResult> calleeList = MainForm.getEngine().getCallee(
                    className, mn, null);

            // 同时刷新侧边面板的 caller/callee 列表
            DefaultListModel<MethodResult> calleeData = (DefaultListModel<MethodResult>)
                    MainForm.getInstance().getCalleeList().getModel();
            DefaultListModel<MethodResult> callerData = (DefaultListModel<MethodResult>)
                    MainForm.getInstance().getCallerList().getModel();
            calleeData.clear();
            callerData.clear();
            for (MethodResult mr : callerList) {
                callerData.addElement(mr);
            }
            for (MethodResult mr : calleeList) {
                calleeData.addElement(mr);
            }
            MainForm.getInstance().getTabbedPanel().setSelectedIndex(2);

            // 合并 caller + callee 的结果
            List<NavigationItem> items = new ArrayList<>();
            for (MethodResult mr : calleeList) {
                items.add(new NavigationItem(mr, NavigationType.CALLEE));
            }
            for (MethodResult mr : callerList) {
                items.add(new NavigationItem(mr, NavigationType.CALLER));
            }

            // 如果基于当前类的精确查询无结果，回退到只按方法名全局搜索
            if (items.isEmpty()) {
                logger.info("no caller/callee in current class, fallback to global method search: {}", mn);
                List<MethodResult> globalMethods = MainForm.getEngine().getMethod(
                        null, mn, null);
                for (MethodResult mr : globalMethods) {
                    items.add(new NavigationItem(mr, NavigationType.METHOD));
                    if (items.size() >= MAX_DISPLAY) {
                        break;
                    }
                }
            }

            // 限制最终结果数量不超过 MAX_DISPLAY
            if (items.size() > MAX_DISPLAY) {
                items = new ArrayList<>(items.subList(0, MAX_DISPLAY));
            }

            if (items.isEmpty()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                "no caller/callee/method found for: " + mn));
                return;
            }

            // 如果只有1个结果，直接跳转
            List<NavigationItem> finalItems = items;
            if (items.size() == 1) {
                SwingUtilities.invokeLater(() -> jumpToMethod(finalItems.get(0).methodResult));
                return;
            }

            // 多个结果，弹出选择窗口
            SwingUtilities.invokeLater(() -> showNavigationPopup(finalItems, mn, screenX, screenY));
        }).start();
    }

    /**
     * 弹出导航选择窗口
     */
    private static void showNavigationPopup(List<NavigationItem> items, String methodName,
                                            int screenX, int screenY) {
        // 创建无装饰弹窗
        JWindow popup = new JWindow(SwingUtilities.getWindowAncestor(
                MainForm.getInstance().getMasterPanel()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(4, 0, 4, 0)));
        mainPanel.setBackground(new Color(43, 43, 43));

        // 标题面板
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(60, 63, 65));
        titlePanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        JLabel titleLabel = new JLabel("Navigate to: " + methodName);
        titleLabel.setForeground(new Color(187, 187, 187));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("(callee/method=GO TO DEF, caller=WHO CALLS)");

        hintLabel.setForeground(new Color(128, 128, 128));
        hintLabel.setFont(hintLabel.getFont().deriveFont(10f));
        titlePanel.add(hintLabel, BorderLayout.EAST);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 列表模型
        DefaultListModel<NavigationItem> model = new DefaultListModel<>();
        for (NavigationItem item : items) {
            model.addElement(item);
        }

        JList<NavigationItem> list = new JList<>(model);
        list.setBackground(new Color(43, 43, 43));
        list.setForeground(new Color(187, 187, 187));
        list.setSelectionBackground(new Color(75, 110, 175));
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(24);
        list.setSelectedIndex(0);

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NavigationItem) {
                    NavigationItem nav = (NavigationItem) value;
                    MethodResult mr = nav.methodResult;
                    String shortClass = mr.getClassName();
                    if (shortClass.contains("/")) {
                        shortClass = shortClass.substring(shortClass.lastIndexOf('/') + 1);
                    }
                    String tag;
                    Color tagColor;
                    if (nav.type == NavigationType.CALLEE) {
                        tag = "[CALLEE] ";
                        tagColor = new Color(104, 151, 187);
                    } else if (nav.type == NavigationType.CALLER) {
                        tag = "[CALLER] ";
                        tagColor = new Color(152, 118, 170);
                    } else {
                        tag = "[METHOD] ";
                        tagColor = new Color(106, 171, 115);
                    }

                    setText("<html><font color='" + toHex(tagColor) + "'>" + tag + "</font>"
                            + "<font color='#CC7832'>" + shortClass + "</font>"
                            + ".<b>" + mr.getMethodName() + "</b>"
                            + "<font color='#808080'>" + mr.getMethodDesc() + "</font></html>");

                    if (!isSelected) {
                        setBackground(new Color(43, 43, 43));
                        setForeground(new Color(187, 187, 187));
                    }
                }
                return this;
            }
        });

        // 双击或回车选择
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || e.getClickCount() == 1) {
                    NavigationItem selected = list.getSelectedValue();
                    if (selected != null) {
                        popup.dispose();
                        jumpToMethod(selected.methodResult);
                    }
                }
            }
        });

        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    NavigationItem selected = list.getSelectedValue();
                    if (selected != null) {
                        popup.dispose();
                        jumpToMethod(selected.methodResult);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.dispose();
                }
            }
        });

        // 点击弹窗外部关闭
        popup.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                popup.dispose();
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(43, 43, 43));
        scrollPane.getViewport().setBackground(new Color(43, 43, 43));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        popup.setContentPane(mainPanel);

        // 限制最多显示 15 条，超出则滚动
        int visibleRows = Math.min(items.size(), 15);
        int popupHeight = visibleRows * 24 + 40;
        int popupWidth = 600;
        popup.setSize(popupWidth, popupHeight);

        // 定位弹窗：在鼠标点击位置附近
        popup.setLocation(screenX, screenY + 5);

        // 确保弹窗在屏幕内
        Rectangle screenBounds = popup.getGraphicsConfiguration().getBounds();
        Point loc = popup.getLocation();
        if (loc.x + popupWidth > screenBounds.x + screenBounds.width) {
            loc.x = screenBounds.x + screenBounds.width - popupWidth;
        }
        if (loc.y + popupHeight > screenBounds.y + screenBounds.height) {
            loc.y = screenY - popupHeight - 5;
        }
        popup.setLocation(loc);

        popup.setVisible(true);
        list.requestFocusInWindow();
    }

    /**
     * 跳转到指定方法（复用 CommonMouseAdapter 的跳转逻辑）
     */
    @SuppressWarnings("all")
    public static void jumpToMethod(MethodResult res) {
        if (res == null) {
            return;
        }

        // 处理继承链：子类通过 this.method 调用父类的 method
        ClassResult nowClass = MainForm.getEngine().getClassByClass(res.getClassName());
        while (nowClass != null) {
            ArrayList<MethodResult> method = MainForm.getEngine().getMethod(
                    nowClass.getClassName(),
                    res.getMethodName(),
                    res.getMethodDesc());
            if (method.size() > 0) {
                res = method.get(0);
                break;
            }
            nowClass = MainForm.getEngine().getClassByClass(nowClass.getSuperClassName());
        }

        String className = res.getClassName();
        String tempPath = className.replace("/", File.separator);
        String classPath;

        classPath = String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath);
        if (!Files.exists(Paths.get(classPath))) {
            classPath = String.format("%s%sBOOT-INF%sclasses%s%s.class",
                    Const.tempDir, File.separator, File.separator, File.separator, tempPath);
            if (!Files.exists(Paths.get(classPath))) {
                classPath = String.format("%s%sWEB-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                if (!Files.exists(Paths.get(classPath))) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>" +
                                    "<p>need dependency or class file not found</p>" +
                                    "<p>缺少依赖或者文件找不到（考虑加载 rt.jar 并检查你的 JAR 是否合法）</p>" +
                                    "</html>");
                    return;
                }
            }
        }

        String finalClassPath = classPath;
        MethodResult finalRes = res;

        new Thread(() -> {
            // LUCENE 索引处理
            if (LuceneSearchForm.getInstance() != null && LuceneSearchForm.usePaLucene()) {
                IndexPluginsSupport.addIndex(Paths.get(finalClassPath).toFile());
            }

            String code = DecompileEngine.decompile(Paths.get(finalClassPath));
            String methodName = finalRes.getMethodName();
            int pos = FinderRunner.find(code, methodName, finalRes.getMethodDesc());

            // SET FILE TREE HIGHLIGHT
            SearchInputListener.getFileTree().searchPathTarget(className);

            // 在新 Tab 中打开（Ctrl+Click 跳转）
            SwingUtilities.invokeLater(() -> {
                CodeTabPanel tabPanel = MainForm.getCodeTabPanel();
                if (tabPanel != null) {
                    tabPanel.openTab(className, code, pos + 1);
                } else {
                    MainForm.getCodeArea().setText(code);
                    MainForm.getCodeArea().setCaretPosition(pos + 1);
                }
            });
        }).start();

        JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
        new Thread(() -> dialog.setVisible(true)).start();
        MethodResult refreshRes = res;
        new Thread() {
            @Override
            public void run() {
                CoreHelper.refreshAllMethods(className);
                CoreHelper.refreshCallers(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                CoreHelper.refreshCallee(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                CoreHelper.refreshHistory(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                CoreHelper.refreshImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                CoreHelper.refreshSuperImpls(className, refreshRes.getMethodName(), refreshRes.getMethodDesc());
                dialog.dispose();
            }
        }.start();

        MainForm.getInstance().getCurClassText().setText(className);
        String jarName = res.getJarName();
        if (StringUtil.isNull(jarName)) {
            jarName = MainForm.getEngine().getJarByClass(className);
        }
        MainForm.getInstance().getCurJarText().setText(jarName);
        MainForm.getInstance().getCurMethodText().setText(res.getMethodName());
        res.setClassPath(Paths.get(finalClassPath));
        MainForm.setCurMethod(res);
        MainForm.setCurClass(className);
    }

    private static String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * 导航项类型
     */
    enum NavigationType {
        CALLER,  // 谁调用了这个方法
        CALLEE,  // 这个方法调用了谁（跳转到定义）
        METHOD   // 全局搜索的方法定义（按方法名匹配）
    }

    /**
     * 导航项
     */
    static class NavigationItem {
        final MethodResult methodResult;
        final NavigationType type;

        NavigationItem(MethodResult methodResult, NavigationType type) {
            this.methodResult = methodResult;
            this.type = type;
        }

        @Override
        public String toString() {
            String tag;
            if (type == NavigationType.CALLEE) {
                tag = "[CALLEE] ";
            } else if (type == NavigationType.CALLER) {
                tag = "[CALLER] ";
            } else {
                tag = "[METHOD] ";
            }
            return tag + methodResult.getClassName() + "." +
                    methodResult.getMethodName() + methodResult.getMethodDesc();
        }
    }
}
