package me.n1ar4.jar.analyzer.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

public class LuceneSearchForm {
    private JPanel rootPanel;
    private JPanel searchInputPanel;
    private JLabel searchIconLabel;
    private JTextField searchText;
    private JScrollPane searchScroll;
    private JList searchResultList;
    private JPanel searchOptionPanel;
    private JRadioButton containsRadioButton;
    private JRadioButton regexpRadioButton;

    private static LuceneSearchForm instance;
    private static JFrame instanceFrame;

    public static LuceneSearchForm getInstance() {
        return instance;
    }

    public static JFrame getInstanceFrame() {
        return instanceFrame;
    }

    private void init() {
    }

    public static void start() {
        instanceFrame = new JFrame("LuceneSearchForm");
        instanceFrame.setUndecorated(true);
        instanceFrame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        instance = new LuceneSearchForm();
        instance.init();
        instanceFrame.setContentPane(instance.rootPanel);
        instanceFrame.pack();
        instanceFrame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        searchInputPanel = new JPanel();
        searchInputPanel.setLayout(new GridLayoutManager(1, 3, new Insets(3, 3, 3, 3), -1, -1));
        rootPanel.add(searchInputPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        searchIconLabel = new JLabel();
        searchIconLabel.setText("");
        searchInputPanel.add(searchIconLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchText = new JTextField();
        searchInputPanel.add(searchText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(200, -1), new Dimension(200, -1), null, 0, false));
        searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        searchInputPanel.add(searchOptionPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        containsRadioButton = new JRadioButton();
        containsRadioButton.setText("contains");
        searchOptionPanel.add(containsRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        regexpRadioButton = new JRadioButton();
        regexpRadioButton.setText("regexp");
        searchOptionPanel.add(regexpRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchScroll = new JScrollPane();
        rootPanel.add(searchScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(600, 400), new Dimension(600, 400), new Dimension(600, 400), 0, false));
        searchResultList = new JList();
        searchScroll.setViewportView(searchResultList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}