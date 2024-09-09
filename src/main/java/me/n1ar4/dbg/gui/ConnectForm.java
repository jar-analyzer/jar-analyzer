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

package me.n1ar4.dbg.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.dbg.core.DBGRunner;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ConnectForm {
    private static final Logger logger = LogManager.getLogger();
    private JPanel masterPanel;
    private JTextField ipText;
    private JTextField portText;
    private JTextField jdwpArgText;
    private JLabel jdwpIpLabel;
    private JLabel jdwpPortLabel;
    private JLabel jdwpArgLabel;
    private JButton connectButton;
    private JButton copyArgsButton;
    private JPanel actionPanel;
    private JTextField mainClassText;
    private JLabel mainClassLabel;
    private static JFrame frame;
    private static ConnectForm instance;

    public static JFrame getFrame() {
        return frame;
    }

    public static ConnectForm getInstance() {
        return instance;
    }

    public static void start() {
        frame = new JFrame("ConnectForm");
        frame.pack();
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        instance = new ConnectForm();
        instance.init();
        frame.setContentPane(instance.masterPanel);
        frame.setVisible(true);
    }

    private void init() {
        this.jdwpArgText.setText("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        this.jdwpArgText.setCaretPosition(0);
        this.jdwpArgText.setEditable(false);

        this.ipText.setText("localhost");
        this.portText.setText("5005");

        this.copyArgsButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(this.jdwpArgText.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this.masterPanel, "copy");
        });
        this.connectButton.addActionListener(e -> {
            String ip = ipText.getText();
            String port = portText.getText();
            String main = mainClassText.getText();
            if (ip.isEmpty() || port.isEmpty()) {
                JOptionPane.showMessageDialog(this.masterPanel,
                        "ip or port is null");
                return;
            }
            if (main.isEmpty()) {
                JOptionPane.showMessageDialog(this.masterPanel,
                        "main class is null");
                return;
            }
            logger.info("connect to {}:{}", ip, port);
            DBGRunner runner = new DBGRunner(ip, port, main);
            MainForm.setRunner(runner);
            MainForm.doStart();
            frame.dispose();
        });
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
        masterPanel = new JPanel();
        masterPanel.setLayout(new GridLayoutManager(5, 2, new Insets(3, 3, 3, 3), -1, -1));
        jdwpIpLabel = new JLabel();
        jdwpIpLabel.setText("JDWP IP");
        masterPanel.add(jdwpIpLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        ipText = new JTextField();
        masterPanel.add(ipText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jdwpPortLabel = new JLabel();
        jdwpPortLabel.setText("JDWP Port");
        masterPanel.add(jdwpPortLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        portText = new JTextField();
        masterPanel.add(portText, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jdwpArgLabel = new JLabel();
        jdwpArgLabel.setText("Args");
        masterPanel.add(jdwpArgLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        jdwpArgText = new JTextField();
        masterPanel.add(jdwpArgText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), new Dimension(250, -1), null, 0, false));
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayoutManager(1, 2, new Insets(3, 3, 3, 3), -1, -1));
        masterPanel.add(actionPanel, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        connectButton = new JButton();
        connectButton.setText("Connect");
        actionPanel.add(connectButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyArgsButton = new JButton();
        copyArgsButton.setText("Copy Args");
        actionPanel.add(copyArgsButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mainClassLabel = new JLabel();
        mainClassLabel.setText("Main Class");
        masterPanel.add(mainClassLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        mainClassText = new JTextField();
        masterPanel.add(mainClassText, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}
