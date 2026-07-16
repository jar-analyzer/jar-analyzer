/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.dbg.gui;

import me.n1ar4.dbg.core.DBGRunner;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
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
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        instance = new ConnectForm();
        instance.init();
        frame.setContentPane(instance.masterPanel);
        frame.pack();
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
        initializeComponents();
    }

    private void initializeComponents() {
        masterPanel = new JPanel();
        SwingLayout.configureGrid(masterPanel, 5, 2, new Insets(3, 3, 3, 3), -1, -1);
        jdwpIpLabel = new JLabel();
        jdwpIpLabel.setText("JDWP IP");
        SwingLayout.add(masterPanel, jdwpIpLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        ipText = new JTextField();
        SwingLayout.add(masterPanel, ipText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        jdwpPortLabel = new JLabel();
        jdwpPortLabel.setText("JDWP Port");
        SwingLayout.add(masterPanel, jdwpPortLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        portText = new JTextField();
        SwingLayout.add(masterPanel, portText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        jdwpArgLabel = new JLabel();
        jdwpArgLabel.setText("Args");
        SwingLayout.add(masterPanel, jdwpArgLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        jdwpArgText = new JTextField();
        SwingLayout.add(masterPanel, jdwpArgText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(250, -1), new Dimension(250, -1), null, 0);
        actionPanel = new JPanel();
        SwingLayout.configureGrid(actionPanel, 1, 2, new Insets(3, 3, 3, 3), -1, -1);
        SwingLayout.add(masterPanel, actionPanel, 4, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        connectButton = new JButton();
        connectButton.setText("Connect");
        SwingLayout.add(actionPanel, connectButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        copyArgsButton = new JButton();
        copyArgsButton.setText("Copy Args");
        SwingLayout.add(actionPanel, copyArgsButton, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        mainClassLabel = new JLabel();
        mainClassLabel.setText("Main Class");
        SwingLayout.add(masterPanel, mainClassLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        mainClassText = new JTextField();
        SwingLayout.add(masterPanel, mainClassText, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

}
