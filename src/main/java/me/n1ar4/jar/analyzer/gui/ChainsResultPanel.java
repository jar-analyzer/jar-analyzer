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

import me.n1ar4.jar.analyzer.core.FinderRunner;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.entity.ClassResult;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.gui.state.State;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chains结果显示面板，支持折叠展开和点击跳转功能
 */
public class ChainsResultPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger();

    private final JPanel contentPanel;
    private final JScrollPane scrollPane;
    private final Map<String, ChainPanel> chainPanels = new HashMap<>();
    private int chainCount = 0;

    public ChainsResultPanel() {
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 创建一个包装面板确保内容左对齐
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(contentPanel, BorderLayout.NORTH);

        scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 添加一条调用链
     */
    public void addChain(String chainId, String title, List<String> methods) {
        ChainPanel chainPanel = new ChainPanel(chainId, title, methods);
        chainPanels.put(chainId, chainPanel);

        contentPanel.add(chainPanel);
        contentPanel.add(Box.createVerticalStrut(5)); // 添加间距

        revalidate();
        repaint();

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * 添加普通文本消息
     */
    public void append(String text) {
        if (text == null) return;

        // 创建一个面板来确保左对齐
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        textPanel.setBackground(contentPanel.getBackground());

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        textPanel.add(textLabel);

        contentPanel.add(textPanel);
        contentPanel.add(Box.createVerticalStrut(2));

        revalidate();
        repaint();

        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * 清空所有内容
     */
    public void setText(String text) {
        if (text == null) {
            clear();
        } else {
            clear();
            append(text);
        }
    }

    /**
     * 清空所有内容
     */
    public void clear() {
        contentPanel.removeAll();
        chainPanels.clear();
        chainCount = 0;
        revalidate();
        repaint();
    }

    /**
     * 设置光标位置到末尾
     */
    public void setCaretPosition(int position) {
        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * 获取文档长度
     */
    public int getDocumentLength() {
        return contentPanel.getComponentCount();
    }

    /**
     * 单个调用链面板
     */
    private class ChainPanel extends JPanel {
        private final String chainId;
        private final JButton toggleButton;
        private final JPanel methodsPanel;
        private boolean expanded = false;

        public ChainPanel(String chainId, String title, List<String> methods) {
            this.chainId = chainId;

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());

            // 创建标题面板
            JPanel titlePanel = new JPanel(new BorderLayout());
            toggleButton = new JButton("▶ " + title);
            toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
            toggleButton.setBorderPainted(false);
            toggleButton.setContentAreaFilled(false);
            toggleButton.setFocusPainted(false);
            toggleButton.addActionListener(e -> toggleExpansion());

            titlePanel.add(toggleButton, BorderLayout.CENTER);
            add(titlePanel, BorderLayout.NORTH);

            // 创建方法列表面板
            methodsPanel = new JPanel();
            methodsPanel.setLayout(new BoxLayout(methodsPanel, BoxLayout.Y_AXIS));
            methodsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));

            // 添加方法
            for (int i = 0; i < methods.size(); i++) {
                String method = methods.get(i);
                String arrow = (i == 0) ? "" : " -> ";

                JPanel methodLinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

                if (!arrow.isEmpty()) {
                    JLabel arrowLabel = new JLabel(arrow);
                    arrowLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                    methodLinePanel.add(arrowLabel);
                }

                JLabel methodLabel = new JLabel(method);
                methodLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                methodLabel.setForeground(Color.BLUE);
                methodLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                methodLabel.addMouseListener(new MethodClickListener(method));

                methodLinePanel.add(methodLabel);
                methodsPanel.add(methodLinePanel);
            }

            add(methodsPanel, BorderLayout.CENTER);

            // 默认折叠状态
            methodsPanel.setVisible(expanded);
        }

        private void toggleExpansion() {
            expanded = !expanded;
            methodsPanel.setVisible(expanded);
            toggleButton.setText((expanded ? "▼ " : "▶ ") +
                    toggleButton.getText().substring(2));
            revalidate();
            repaint();
        }
    }

    /**
     * 方法点击监听器
     */
    private class MethodClickListener extends MouseAdapter {
        private final String methodSignature;

        public MethodClickListener(String methodSignature) {
            this.methodSignature = methodSignature;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                navigateToMethod(methodSignature);
            }
        }
    }

    /**
     * 导航到指定方法
     */
    private void navigateToMethod(String methodSignature) {
        try {
            // 解析方法签名: className.methodName(desc)
            String[] parts = methodSignature.split("\\.");
            if (parts.length < 2) return;

            String methodNameAndDesc = parts[parts.length - 1];
            String className = methodSignature.substring(0,
                    methodSignature.length() - methodNameAndDesc.length() - 1);

            String methodName;
            String methodDesc;

            int descStart = methodNameAndDesc.indexOf('(');
            if (descStart > 0) {
                methodName = methodNameAndDesc.substring(0, descStart);
                methodDesc = methodNameAndDesc.substring(descStart);
            } else {
                methodName = methodNameAndDesc;
                methodDesc = "";
            }

            // 创建MethodResult对象
            MethodResult methodResult = new MethodResult();
            methodResult.setClassName(className);
            methodResult.setMethodName(methodName);
            methodResult.setMethodDesc(methodDesc);

            // 使用CommonMouseAdapter类似的逻辑进行跳转
            navigateToMethodImpl(methodResult);

        } catch (Exception ex) {
            logger.error("导航到方法失败: " + methodSignature, ex);
            JOptionPane.showMessageDialog(this,
                    "导航到方法失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 导航到方法的具体实现
     */
    private void navigateToMethodImpl(MethodResult res) {
        // 查找方法实现（处理继承）
        ClassResult nowClass = MainForm.getEngine().getClassByClass(res.getClassName());
        while (nowClass != null) {
            ArrayList<MethodResult> method = MainForm.getEngine().getMethod(
                    nowClass.getClassName(),
                    res.getMethodName(),
                    res.getMethodDesc());
            if (method.size() > 0) {
                res = method.get(0);
                logger.debug("find target method in class: {}", nowClass.getClassName());
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
            String code = DecompileEngine.decompile(Paths.get(finalClassPath));
            String methodName = finalRes.getMethodName();

            int pos = FinderRunner.find(code, methodName, finalRes.getMethodDesc());

            // SET FILE TREE HIGHLIGHT
            SearchInputListener.getFileTree().searchPathTarget(className);

            MainForm.getCodeArea().setText(code);
            MainForm.getCodeArea().setCaretPosition(pos + 1);
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

        State newState = new State();
        newState.setClassPath(Paths.get(finalClassPath));
        newState.setJarName(jarName);
        newState.setClassName(res.getClassName());
        newState.setMethodDesc(res.getMethodDesc());
        newState.setMethodName(res.getMethodName());

        int curSI = MainForm.getCurStateIndex();
        if (curSI == -1) {
            MainForm.getStateList().add(curSI + 1, newState);
            MainForm.setCurStateIndex(curSI + 1);
        } else {
            if (curSI >= MainForm.getStateList().size()) {
                curSI = MainForm.getStateList().size() - 1;
            }
            State state = MainForm.getStateList().get(curSI);
            if (state != null) {
                int a = MainForm.getStateList().size();
                MainForm.getStateList().add(curSI + 1, newState);
                int b = MainForm.getStateList().size();
                // 达到最大容量
                if (a == b) {
                    MainForm.setCurStateIndex(curSI);
                } else {
                    MainForm.setCurStateIndex(curSI + 1);
                }
            } else {
                logger.warn("current state is null");
            }
        }
    }
} 