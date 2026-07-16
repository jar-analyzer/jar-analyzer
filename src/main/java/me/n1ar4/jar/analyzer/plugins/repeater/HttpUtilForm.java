/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.repeater;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class HttpUtilForm {
    public JPanel httpUtilPanel;
    private JTextArea reqArea;
    private JTextArea respArea;
    private JButton reqButton;
    private JTextField ipText;
    private JTextField portText;
    private JLabel reqLabel;
    private JLabel ipLabel;
    private JLabel portLabel;
    private JPanel ipPanel;
    private JScrollPane reqScroll;
    private JScrollPane respScroll;

    private void initLang() {
        reqLabel.setText("  Request");
        ipLabel.setText("  Target IP");
        portLabel.setText("  Target Port");
        reqButton.setText("Send");
    }

    public HttpUtilForm() {
        initLang();
        reqButton.addActionListener(e -> {
            String ip = ipText.getText().trim();
            String port = portText.getText().trim();
            String req = reqArea.getText().trim();
            if (!StringUtil.notEmpty(ip) || !StringUtil.notEmpty(port)) {
                JOptionPane.showMessageDialog(this.httpUtilPanel, "need ip port");
                return;
            }
            int portInt = Integer.parseInt(port);
            new Thread(() -> {
                String finalReq = req + "\r\n\r\n";
                String resp = SocketUtil.sendRaw(ip, portInt, finalReq);
                respArea.setText(resp);
                respArea.setCaretPosition(0);
            }).start();
        });
    }

    public static void start() {
        JFrame frame = new JFrame("Jar Analyzer V2 - Repeater");
        frame.setContentPane(new HttpUtilForm().httpUtilPanel);
        frame.setResizable(false);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        httpUtilPanel = new JPanel();
        SwingLayout.configureGrid(httpUtilPanel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        httpUtilPanel.setBackground(new Color(-1120293));
        ipPanel = new JPanel();
        SwingLayout.configureGrid(ipPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        ipPanel.setBackground(new Color(-1120293));
        SwingLayout.add(httpUtilPanel, ipPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        ipLabel = new JLabel();
        ipLabel.setText("  目标IP");
        SwingLayout.add(ipPanel, ipLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        ipText = new JTextField();
        SwingLayout.add(ipPanel, ipText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, new Dimension(150, -1), null, 0);
        portLabel = new JLabel();
        portLabel.setText("  目标端口");
        SwingLayout.add(ipPanel, portLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        portText = new JTextField();
        SwingLayout.add(ipPanel, portText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, new Dimension(150, -1), null, 0);
        reqLabel = new JLabel();
        reqLabel.setText("请求");
        SwingLayout.add(ipPanel, reqLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        reqButton = new JButton();
        reqButton.setEnabled(true);
        reqButton.setText("发送");
        SwingLayout.add(ipPanel, reqButton, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        reqScroll = new JScrollPane();
        reqScroll.setBackground(new Color(-1120293));
        reqScroll.setToolTipText("");
        SwingLayout.add(httpUtilPanel, reqScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 600), new Dimension(500, 600), new Dimension(500, 600), 0);
        reqScroll.setBorder(BorderFactory.createTitledBorder(null, "request", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        reqArea = new JTextArea();
        reqArea.setBackground(new Color(-1));
        Font reqAreaFont = this.resolveFont("Consolas", -1, 18, reqArea.getFont());
        if (reqAreaFont != null) reqArea.setFont(reqAreaFont);
        reqArea.setLineWrap(true);
        reqArea.setText("");
        reqScroll.setViewportView(reqArea);
        respScroll = new JScrollPane();
        respScroll.setBackground(new Color(-1120293));
        SwingLayout.add(httpUtilPanel, respScroll, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 600), new Dimension(500, 600), new Dimension(500, 600), 0);
        respScroll.setBorder(BorderFactory.createTitledBorder(null, "response", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        respArea = new JTextArea();
        respArea.setBackground(new Color(-1));
        Font respAreaFont = this.resolveFont("Consolas", -1, 18, respArea.getFont());
        if (respAreaFont != null) respArea.setFont(respAreaFont);
        respArea.setLineWrap(true);
        respArea.setText("");
        respArea.setWrapStyleWord(false);
        respScroll.setViewportView(respArea);
    }

    /**
     * @noinspection ALL
     */
    private Font resolveFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return httpUtilPanel;
    }

}
