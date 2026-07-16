/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.listener;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.plugins.repeater.SocketUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ListenUtilForm {
    public JPanel listenUtilPanel;
    private JTextField portText;
    private JButton listenButton;
    private JTextArea terminalArea;
    private JScrollPane terminalScroll;
    private JButton sendButton;
    private JTextField sendText;
    private JLabel portLabel;
    private JPanel centerPanel;
    private JPanel sendPanel;

    private static Thread t;
    private static boolean isRunning = false;

    private void initLang() {
        portLabel.setText("Port");
        listenButton.setText("Start Listen");
        sendButton.setText("Send");
    }

    public ListenUtilForm() {

        initLang();

        listenButton.addActionListener(e -> {
            if (isRunning) {
                isRunning = false;
                t.interrupt();
                t = null;
                listenButton.setText("Start Listen");
                SocketUtil.area.setText(null);
            } else {
                String portStr = portText.getText().trim();
                int port = Integer.parseInt(portStr);
                t = new Thread(() -> SocketUtil.serve(port, terminalArea));
                t.start();
                isRunning = true;
                listenButton.setText("Stop Listen");
            }
        });
        sendButton.addActionListener(e -> SocketUtil.sendServe(sendText.getText()));
    }

    public static void start() {
        JFrame frame = new JFrame("Jar Analyzer - Listener");
        frame.setContentPane(new ListenUtilForm().listenUtilPanel);
        frame.setResizable(false);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        listenUtilPanel = new JPanel();
        SwingLayout.configureGrid(listenUtilPanel, 3, 1, new Insets(0, 0, 0, 0), -1, -1);
        listenUtilPanel.setBackground(new Color(-1120293));
        centerPanel = new JPanel();
        SwingLayout.configureGrid(centerPanel, 1, 3, new Insets(0, 0, 0, 0), -1, -1);
        centerPanel.setBackground(new Color(-1120293));
        SwingLayout.add(listenUtilPanel, centerPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        portLabel = new JLabel();
        portLabel.setText("监听端口");
        SwingLayout.add(centerPanel, portLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        portText = new JTextField();
        SwingLayout.add(centerPanel, portText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        listenButton = new JButton();
        listenButton.setText("开始监听端口");
        SwingLayout.add(centerPanel, listenButton, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        terminalScroll = new JScrollPane();
        terminalScroll.setBackground(new Color(-1120293));
        terminalScroll.setForeground(new Color(-12828863));
        SwingLayout.add(listenUtilPanel, terminalScroll, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 300), new Dimension(500, 300), new Dimension(500, 300), 0);
        terminalScroll.setBorder(BorderFactory.createTitledBorder(null, "terminal", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        terminalArea = new JTextArea();
        terminalArea.setBackground(new Color(-12828863));
        terminalArea.setEditable(true);
        terminalArea.setEnabled(true);
        terminalArea.setForeground(new Color(-16711895));
        terminalScroll.setViewportView(terminalArea);
        sendPanel = new JPanel();
        SwingLayout.configureGrid(sendPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        sendPanel.setBackground(new Color(-1120293));
        SwingLayout.add(listenUtilPanel, sendPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        sendButton = new JButton();
        sendButton.setText("发送");
        SwingLayout.add(sendPanel, sendButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        sendText = new JTextField();
        SwingLayout.add(sendPanel, sendText, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return listenUtilPanel;
    }

}
