/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.n1ar4.jar.analyzer.gui.util.SyntaxAreaHelper;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SearchForm {
    private JPanel rootPanel;
    private JTextField searchText;
    private JLabel searchLabel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel resultLabel;
    private JPanel showPanel;
    private static SearchForm instance;
    private static String searchTextGlobal = null;
    private static int total = 0;

    public static void start() {
        JFrame frame = new JFrame();
        instance = new SearchForm();
        instance.init();
        frame.setContentPane(instance.rootPanel);
        frame.setTitle("SEARCH");
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private void init() {
        resultLabel.setText("0/0");

        instance.searchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel(e);
            }

            private void updateLabel(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(0, e.getDocument().getLength());
                    // 修复卡死的 BUG
                    if (StringUtil.isNull(text)) {
                        return;
                    }
                    searchTextGlobal = text;
                    total = SyntaxAreaHelper.addSearchAction(text);
                    if (total == 0) {
                        resultLabel.setText("0/0");
                        return;
                    }
                    int cur = SyntaxAreaHelper.getCurrentIndex();
                    resultLabel.setText(String.format("%d/%d", cur + 1, total));
                } catch (Exception ignored) {
                }
            }
        });

        instance.searchText.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    getNext();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        instance.prevButton.addActionListener(e -> {
            getPrev();
        });
        instance.nextButton.addActionListener(e -> {
            getNext();
        });
    }

    private void getNext() {
        if (searchTextGlobal == null || searchTextGlobal.isEmpty()) {
            return;
        }
        if (total == 0) {
            return;
        }
        SyntaxAreaHelper.navigate(searchTextGlobal, true);
        int cur = SyntaxAreaHelper.getCurrentIndex();
        resultLabel.setText(String.format("%d/%d", cur + 1, total));
    }

    private void getPrev() {
        if (searchTextGlobal == null || searchTextGlobal.isEmpty()) {
            return;
        }
        if (total == 0) {
            return;
        }
        SyntaxAreaHelper.navigate(searchTextGlobal, false);
        int cur = SyntaxAreaHelper.getCurrentIndex();
        resultLabel.setText(String.format("%d/%d", cur + 1, total));
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
        rootPanel.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        searchLabel = new JLabel();
        searchLabel.setText("Search Text");
        rootPanel.add(searchLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        searchText = new JTextField();
        rootPanel.add(searchText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(150, -1), null, 0, false));
        showPanel = new JPanel();
        showPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(showPanel, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        prevButton = new JButton();
        prevButton.setText("Prev");
        showPanel.add(prevButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextButton = new JButton();
        nextButton.setText("Next");
        showPanel.add(nextButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resultLabel = new JLabel();
        resultLabel.setText("0");
        showPanel.add(resultLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, -1), null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
