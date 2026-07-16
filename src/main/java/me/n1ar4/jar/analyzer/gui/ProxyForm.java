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

import me.n1ar4.jar.analyzer.core.others.Proxy;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ProxyForm {
    private JPanel rootPanel;
    private JPanel proxyPanel;
    private JPanel opPanel;
    private JTextField socksHostText;
    private JTextField socksPortText;
    private JButton socksBtn;
    private JTextField httpHostText;
    private JTextField httpPortText;
    private JButton httpBtn;
    private JCheckBox httpCheckBox;
    private JCheckBox httpsCheckBox;
    private JCheckBox systemCheckBox;
    private JPanel socksPanel;
    private JLabel socksHostLabel;
    private JLabel socksPortLabel;
    private JPanel socksBtnPanel;
    private JPanel httpPanel;
    private JLabel httpHostPanel;
    private JLabel httpPortPanel;
    private JPanel httpBtnPanel;
    private JPanel httpOrHttpsPanel;
    private JButton systemBtn;
    private static ProxyForm instance;

    public static ProxyForm getInstance() {
        return instance;
    }

    public static void start() {
        JFrame frame = new JFrame("proxy config");
        instance = new ProxyForm();
        frame.setContentPane(instance.rootPanel);
        instance.init();
        frame.setAlwaysOnTop(true);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void init() {
        if (Proxy.isSystemProxyOpen()) {
            systemCheckBox.setSelected(true);
        }
        httpHostText.setText(Proxy.getHttpHost());
        httpPortText.setText(Proxy.getHttpPort());
        socksHostText.setText(Proxy.getSocksProxyHost());
        socksPortText.setText(Proxy.getSocksProxyPort());
        httpCheckBox.setSelected(true);
        httpsCheckBox.setSelected(true);

        String text = socksHostText.getText();
        if (text == null || text.isEmpty()) {
            socksHostText.setText("127.0.0.1");
            socksPortText.setText("10808");
        }

        httpBtn.addActionListener(e -> {
            Proxy.setHttpProxy(
                    httpHostText.getText(), httpPortText.getText(), httpsCheckBox.isSelected());
            show("SET HTTP PROXY SUCCESS", instance.proxyPanel);
        });

        socksBtn.addActionListener(e -> {
            Proxy.setSocks(
                    socksHostText.getText(), socksPortText.getText());
            show("SET SOCKS PROXY SUCCESS", instance.proxyPanel);
        });

        systemBtn.addActionListener(e -> {
            if (systemCheckBox.isSelected()) {
                Proxy.setSystemProxy();
                show("SET SYSTEM PROXY SUCCESS", instance.proxyPanel);
            }
        });
    }

    private static void show(String msg, JPanel panel) {
        JOptionPane.showMessageDialog(panel, msg);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        proxyPanel = new JPanel();
        SwingLayout.configureGrid(proxyPanel, 2, 3, new Insets(3, 3, 3, 3), -1, -1);
        proxyPanel.setBackground(new Color(-1120293));
        SwingLayout.add(rootPanel, proxyPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(360, -1), null, null, 0);
        opPanel = new JPanel();
        SwingLayout.configureGrid(opPanel, 2, 1, new Insets(0, 0, 0, 0), -1, -1);
        opPanel.setBackground(new Color(-1120293));
        SwingLayout.add(proxyPanel, opPanel, 1, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        socksPanel = new JPanel();
        SwingLayout.configureGrid(socksPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        socksPanel.setBackground(new Color(-1120293));
        SwingLayout.add(opPanel, socksPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        socksPanel.setBorder(BorderFactory.createTitledBorder(null, "SOCKS PROXY", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        socksHostLabel = new JLabel();
        socksHostLabel.setText("HOST");
        SwingLayout.add(socksPanel, socksHostLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        socksPortLabel = new JLabel();
        socksPortLabel.setText("PORT");
        SwingLayout.add(socksPanel, socksPortLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        socksHostText = new JTextField();
        SwingLayout.add(socksPanel, socksHostText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        socksPortText = new JTextField();
        SwingLayout.add(socksPanel, socksPortText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        socksBtnPanel = new JPanel();
        SwingLayout.configureGrid(socksBtnPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        socksBtnPanel.setBackground(new Color(-1120293));
        SwingLayout.add(socksPanel, socksBtnPanel, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        socksBtn = new JButton();
        socksBtn.setText("CONFIRM");
        SwingLayout.add(socksBtnPanel, socksBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(socksBtnPanel, spacer1, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        httpPanel = new JPanel();
        SwingLayout.configureGrid(httpPanel, 4, 2, new Insets(0, 0, 0, 0), -1, -1);
        httpPanel.setBackground(new Color(-1120293));
        httpPanel.setEnabled(true);
        SwingLayout.add(opPanel, httpPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        httpPanel.setBorder(BorderFactory.createTitledBorder(null, "HTTP PROXY", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        httpHostPanel = new JLabel();
        httpHostPanel.setText("HOST");
        SwingLayout.add(httpPanel, httpHostPanel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        httpPortPanel = new JLabel();
        httpPortPanel.setText("PORT");
        SwingLayout.add(httpPanel, httpPortPanel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        httpHostText = new JTextField();
        SwingLayout.add(httpPanel, httpHostText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        httpPortText = new JTextField();
        SwingLayout.add(httpPanel, httpPortText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        httpBtnPanel = new JPanel();
        SwingLayout.configureGrid(httpBtnPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        httpBtnPanel.setBackground(new Color(-1120293));
        SwingLayout.add(httpPanel, httpBtnPanel, 3, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        httpBtn = new JButton();
        httpBtn.setText("CONFIRM");
        SwingLayout.add(httpBtnPanel, httpBtn, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        final Component spacer2 = Box.createGlue();
        SwingLayout.add(httpBtnPanel, spacer2, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        httpOrHttpsPanel = new JPanel();
        SwingLayout.configureGrid(httpOrHttpsPanel, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        httpOrHttpsPanel.setBackground(new Color(-1120293));
        SwingLayout.add(httpPanel, httpOrHttpsPanel, 2, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        httpCheckBox = new JCheckBox();
        httpCheckBox.setBackground(new Color(-1120293));
        httpCheckBox.setText("HTTP");
        SwingLayout.add(httpOrHttpsPanel, httpCheckBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        httpsCheckBox = new JCheckBox();
        httpsCheckBox.setBackground(new Color(-1120293));
        httpsCheckBox.setText("HTTPS");
        SwingLayout.add(httpOrHttpsPanel, httpsCheckBox, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        systemCheckBox = new JCheckBox();
        systemCheckBox.setBackground(new Color(-1120293));
        systemCheckBox.setText("USE SYSTEM PROXY");
        SwingLayout.add(proxyPanel, systemCheckBox, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        systemBtn = new JButton();
        systemBtn.setText("CONFIRM");
        SwingLayout.add(proxyPanel, systemBtn, 0, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
