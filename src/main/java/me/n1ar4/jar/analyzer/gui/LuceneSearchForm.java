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

import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.lucene.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

public class LuceneSearchForm {
    private JPanel rootPanel;
    private JPanel searchInputPanel;
    private JLabel searchIconLabel;
    private JTextArea searchText;
    private JScrollPane searchScroll;
    private JList<LuceneSearchResult> searchResultList;
    private JPanel searchOptionPanel;
    private JRadioButton containsRadio;
    private JRadioButton regexRadio;
    private JButton luceneBuildBtn;
    private JRadioButton noLuceneRadio;
    private JRadioButton paLuceneRadio;
    private JPanel searchOptionsPanel;
    private JLabel luceneSizeLabel;
    private JScrollPane searchTextPanel;
    private JCheckBox caseCheckBox;

    private static LuceneSearchForm instance;
    private static JFrame instanceFrame;
    private static AWTEventListener globalMouseListener = null;

    /**
     * 安装全局鼠标监听器：点击搜索窗口外部任意区域时自动关闭
     */
    private static void installClickOutsideListener() {
        // 防止重复注册：先移除旧的监听器
        if (globalMouseListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
        }
        globalMouseListener = event -> {
            if (event instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                    JFrame frame = instanceFrame;
                    if (frame == null || !frame.isShowing()) {
                        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
                        globalMouseListener = null;
                        return;
                    }
                    // 点击在搜索窗口内部：不关闭
                    Component source = mouseEvent.getComponent();
                    if (source != null) {
                        Window sourceWindow = SwingUtilities.getWindowAncestor(source);
                        if (sourceWindow == frame) {
                            return;
                        }
                    }
                    // 点击在搜索窗口外部（主窗口、其他面板等）：关闭
                    closeInstanceFrame();
                    Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
                    globalMouseListener = null;
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(globalMouseListener, AWTEvent.MOUSE_EVENT_MASK);
    }

    public static boolean usePaLucene() {
        return instance.paLuceneRadio.isSelected();
    }

    public static boolean useNoLucene() {
        return instance.noLuceneRadio.isSelected();
    }

    public static boolean useContains() {
        return instance.containsRadio.isSelected();
    }

    public static boolean useCaseSensitive() {
        return instance.caseCheckBox.isSelected();
    }

    public static boolean useRegex() {
        return instance.regexRadio.isSelected();
    }

    public static JButton getBuildButton() {
        return instance.luceneBuildBtn;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public static LuceneSearchForm getInstance() {
        return instance;
    }

    public static JFrame getInstanceFrame() {
        return instanceFrame;
    }

    public static void closeInstanceFrame() {
        if (instanceFrame != null) {
            instanceFrame.dispose();
            instanceFrame = null;
        }
    }

    private void init() {
        containsRadio.setSelected(true);
        paLuceneRadio.setSelected(true);

        caseCheckBox.setSelected(false);

        luceneBuildBtn.addActionListener(new LuceneBuildListener());
        searchResultList.setCellRenderer(new LuceneResultRender());
        searchResultList.addMouseListener(new LuceneMouseListener());

        searchIconLabel.setIcon(IconManager.gsIcon);
        LuceneSearchListener listener = new LuceneSearchListener(searchText, searchResultList);
        searchText.getDocument().addDocumentListener(listener);
        new LuceneIndexWatcher(luceneSizeLabel).start();
    }

    public static void start(int position) {
        // 如果 frame 存在但已经被 dispose，重置为 null 强制重建
        if (instanceFrame != null && !instanceFrame.isDisplayable()) {
            instanceFrame = null;
            instance = null;
        }

        if (instanceFrame == null) {
            instanceFrame = new JFrame();
            instanceFrame.setUndecorated(true);

            instance = new LuceneSearchForm();
            instance.init();
            instanceFrame.setContentPane(instance.rootPanel);
            instanceFrame.pack();

            if (position == 0) {
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                instanceFrame.setLocation(mouseLocation.x + 10, mouseLocation.y + 10);
            } else if (position == 1) {
                instanceFrame.setLocationRelativeTo(null);
            }

            instanceFrame.setVisible(true);
        } else {
            if (position == 0) {
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                instanceFrame.setLocation(mouseLocation.x + 10, mouseLocation.y + 10);
            } else if (position == 1) {
                instanceFrame.setLocationRelativeTo(null);
            }
            instanceFrame.setVisible(true);
            instanceFrame.toFront();
            instanceFrame.requestFocus();
        }

        // 每次打开/显示窗口时都安装点击外部关闭监听器
        installClickOutsideListener();
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        searchInputPanel = new JPanel();
        SwingLayout.configureGrid(searchInputPanel, 2, 3, new Insets(8, 3, 3, 3), -1, -1);
        SwingLayout.add(rootPanel, searchInputPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchIconLabel = new JLabel();
        searchIconLabel.setText("");
        SwingLayout.add(searchInputPanel, searchIconLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        searchOptionPanel = new JPanel();
        SwingLayout.configureGrid(searchOptionPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(searchInputPanel, searchOptionPanel, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        containsRadio = new JRadioButton();
        containsRadio.setText("contains");
        SwingLayout.add(searchOptionPanel, containsRadio, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        regexRadio = new JRadioButton();
        regexRadio.setText("regexp");
        SwingLayout.add(searchOptionPanel, regexRadio, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        searchOptionsPanel = new JPanel();
        SwingLayout.configureGrid(searchOptionsPanel, 2, 3, new Insets(3, 3, 3, 3), -1, -1);
        SwingLayout.add(searchInputPanel, searchOptionsPanel, 1, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchOptionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "索引设置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        noLuceneRadio = new JRadioButton();
        noLuceneRadio.setText("不使用 Lucene 索引 (仅搜索类名/文件名)");
        SwingLayout.add(searchOptionsPanel, noLuceneRadio, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        paLuceneRadio = new JRadioButton();
        paLuceneRadio.setText("被动构建 Lucene 索引 (每次反编译代码自动提交)");
        SwingLayout.add(searchOptionsPanel, paLuceneRadio, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        luceneSizeLabel = new JLabel();
        luceneSizeLabel.setText("当前索引大小：0 MB");
        SwingLayout.add(searchOptionsPanel, luceneSizeLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        luceneBuildBtn = new JButton();
        luceneBuildBtn.setText("手动构建完整索引");
        SwingLayout.add(searchOptionsPanel, luceneBuildBtn, 1, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        caseCheckBox = new JCheckBox();
        caseCheckBox.setText("大小写敏感");
        SwingLayout.add(searchOptionsPanel, caseCheckBox, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        searchTextPanel = new JScrollPane();
        SwingLayout.add(searchInputPanel, searchTextPanel, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        searchText = new JTextArea();
        searchText.setColumns(10);
        searchText.setLineWrap(false);
        searchText.setRows(3);
        searchTextPanel.setViewportView(searchText);
        searchScroll = new JScrollPane();
        SwingLayout.add(rootPanel, searchScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(650, 400), new Dimension(650, 400), new Dimension(650, 400), 0);
        searchResultList = new JList();
        searchScroll.setViewportView(searchResultList);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(containsRadio);
        buttonGroup.add(regexRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(noLuceneRadio);
        buttonGroup.add(paLuceneRadio);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
