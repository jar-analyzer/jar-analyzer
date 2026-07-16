/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.encoder;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncodeUtilForm {
    private JTextField baseEncodeText;
    private JButton baseEncodeButton;
    private JTextField baseDecodeText;
    private JButton baseDecodeButton;
    private JTextField urlEncodeText;
    private JButton urlEncodeButton;
    private JTextField urlDecodeText;
    private JButton urlDecodeButton;
    private JTextField md5EncodeText;
    private JButton md5EncodeButton;
    private JTextField md5ResultText;
    private JTextField cmdBashEncodeText;
    private JButton cmdBashEncodeButton;
    private JTextField cmdBashResultText;
    private JTextField cmdPwEncodeText;
    private JButton cmdPwEncodeButton;
    private JTextField cmdPwResultText;
    public JPanel encodeUtilPanel;
    private JPanel basePanel;
    private JPanel urlPanel;
    private JPanel md5Panel;
    private JPanel cmdBashPanel;
    private JLabel bashLabel;
    private JPanel cmdPwPanel;
    private JLabel pwLabel;
    private JTextField stringCmdText;
    private JButton stringCmdButton;
    private JTextField stringCmdResultText;
    private JPanel stringCmdPanel;
    private JLabel stringCmdLabel;

    private void initBase64() {
        baseEncodeButton.addActionListener(e -> {
            String source = baseEncodeText.getText();
            String result = Base64.getEncoder().encodeToString(source.getBytes());
            baseDecodeText.setText(result);
        });
        baseDecodeButton.addActionListener(e -> {
            String source = baseDecodeText.getText();
            String result = new String(Base64.getDecoder().decode(source));
            baseEncodeText.setText(result);
        });
    }

    private void initUrl() {
        urlEncodeButton.addActionListener(e -> {
            String source = urlEncodeText.getText();
            String result;
            try {
                result = URLEncoder.encode(source, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            urlDecodeText.setText(result);
        });
        urlDecodeButton.addActionListener(e -> {
            String source = urlDecodeText.getText();
            String result;
            try {
                result = URLDecoder.decode(source, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            urlEncodeText.setText(result);
        });
    }

    private void initMd5() {
        md5EncodeButton.addActionListener(e -> {
            String str = md5EncodeText.getText();
            byte[] digest = null;
            try {
                MessageDigest md5 = MessageDigest.getInstance("md5");
                digest = md5.digest(str.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException ignored) {
            }
            String md5Str = new BigInteger(1,
                    digest != null ? digest : new byte[0]).toString(16);
            md5ResultText.setText(md5Str);
        });
    }

    private void initBash() {
        cmdBashEncodeButton.addActionListener(e -> {
            String source = cmdBashEncodeText.getText();
            cmdBashResultText.setText(CmdUtil.getBashCommand(source));
        });
    }

    private void initPowershell() {
        cmdPwEncodeButton.addActionListener(e -> {
            String source = cmdPwEncodeText.getText();
            cmdPwResultText.setText(CmdUtil.getPowershellCommand(source));
        });
    }

    private void initStringCmd() {
        stringCmdButton.addActionListener(e -> {
            String source = stringCmdText.getText();
            stringCmdResultText.setText(CmdUtil.getStringCommand(source));
        });
    }

    private void initLang() {
        bashLabel.setText("  Java commands cannot use redirection and pipeline symbols");
        pwLabel.setText("  Java commands cannot use redirection and pipeline symbols");
        stringCmdLabel.setText("   String.fromCharCode");
        stringCmdPanel.setBorder(BorderFactory.createTitledBorder(null,
                "Other Encode", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, null, null));
        baseDecodeButton.setText("Decode");
        baseEncodeButton.setText("Encode");
        cmdBashEncodeButton.setText("Generate");
        md5EncodeButton.setText("Encrypt");
        cmdPwEncodeButton.setText("Generate");
        urlEncodeButton.setText("Encode");
        urlDecodeButton.setText("Decode");
        stringCmdButton.setText("Generate");
    }

    public EncodeUtilForm() {
        initLang();
        initBase64();
        initUrl();
        initMd5();
        initBash();
        initPowershell();
        initStringCmd();
    }

    public static void start() {
        JFrame frame = new JFrame("Jar Analyzer - Encoder");
        frame.setContentPane(new EncodeUtilForm().encodeUtilPanel);
        frame.setResizable(false);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        encodeUtilPanel = new JPanel();
        SwingLayout.configureGrid(encodeUtilPanel, 6, 1, new Insets(0, 0, 0, 0), -1, -1);
        encodeUtilPanel.setBackground(new Color(-1120293));
        basePanel = new JPanel();
        SwingLayout.configureGrid(basePanel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        basePanel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, basePanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        basePanel.setBorder(BorderFactory.createTitledBorder(null, "Base64", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        baseEncodeText = new JTextField();
        SwingLayout.add(basePanel, baseEncodeText, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        baseEncodeButton = new JButton();
        baseEncodeButton.setText("");
        SwingLayout.add(basePanel, baseEncodeButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        baseDecodeText = new JTextField();
        SwingLayout.add(basePanel, baseDecodeText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        baseDecodeButton = new JButton();
        baseDecodeButton.setText("");
        SwingLayout.add(basePanel, baseDecodeButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        urlPanel = new JPanel();
        SwingLayout.configureGrid(urlPanel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        urlPanel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, urlPanel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        urlPanel.setBorder(BorderFactory.createTitledBorder(null, "URL", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        urlEncodeText = new JTextField();
        SwingLayout.add(urlPanel, urlEncodeText, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        urlEncodeButton = new JButton();
        urlEncodeButton.setText("");
        SwingLayout.add(urlPanel, urlEncodeButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        urlDecodeText = new JTextField();
        SwingLayout.add(urlPanel, urlDecodeText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        urlDecodeButton = new JButton();
        urlDecodeButton.setText("");
        SwingLayout.add(urlPanel, urlDecodeButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        md5Panel = new JPanel();
        SwingLayout.configureGrid(md5Panel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        md5Panel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, md5Panel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        md5Panel.setBorder(BorderFactory.createTitledBorder(null, "MD5", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        md5EncodeText = new JTextField();
        SwingLayout.add(md5Panel, md5EncodeText, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        md5EncodeButton = new JButton();
        md5EncodeButton.setText("");
        SwingLayout.add(md5Panel, md5EncodeButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        md5ResultText = new JTextField();
        SwingLayout.add(md5Panel, md5ResultText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        cmdBashPanel = new JPanel();
        SwingLayout.configureGrid(cmdBashPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        cmdBashPanel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, cmdBashPanel, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        cmdBashPanel.setBorder(BorderFactory.createTitledBorder(null, "Bash Base64 CMD", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        cmdBashEncodeText = new JTextField();
        SwingLayout.add(cmdBashPanel, cmdBashEncodeText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        cmdBashEncodeButton = new JButton();
        cmdBashEncodeButton.setText("");
        SwingLayout.add(cmdBashPanel, cmdBashEncodeButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        cmdBashResultText = new JTextField();
        cmdBashResultText.setText("");
        SwingLayout.add(cmdBashPanel, cmdBashResultText, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        bashLabel = new JLabel();
        bashLabel.setEnabled(true);
        bashLabel.setText("  解决 Java 执行命令无法使用重定向和管道符号问题");
        SwingLayout.add(cmdBashPanel, bashLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        cmdPwPanel = new JPanel();
        SwingLayout.configureGrid(cmdPwPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        cmdPwPanel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, cmdPwPanel, 4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        cmdPwPanel.setBorder(BorderFactory.createTitledBorder(null, "Powershell Base64 CMD", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        cmdPwEncodeText = new JTextField();
        SwingLayout.add(cmdPwPanel, cmdPwEncodeText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        cmdPwEncodeButton = new JButton();
        cmdPwEncodeButton.setText("");
        SwingLayout.add(cmdPwPanel, cmdPwEncodeButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        cmdPwResultText = new JTextField();
        SwingLayout.add(cmdPwPanel, cmdPwResultText, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        pwLabel = new JLabel();
        pwLabel.setText("  解决 Java 执行命令无法使用重定向和管道符号问题");
        SwingLayout.add(cmdPwPanel, pwLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        stringCmdPanel = new JPanel();
        SwingLayout.configureGrid(stringCmdPanel, 3, 2, new Insets(0, 0, 0, 0), -1, -1);
        stringCmdPanel.setBackground(new Color(-1120293));
        SwingLayout.add(encodeUtilPanel, stringCmdPanel, 5, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        stringCmdPanel.setBorder(BorderFactory.createTitledBorder(null, "特殊命令字符串生成", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        stringCmdText = new JTextField();
        SwingLayout.add(stringCmdPanel, stringCmdText, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        stringCmdButton = new JButton();
        stringCmdButton.setText("");
        SwingLayout.add(stringCmdPanel, stringCmdButton, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        stringCmdResultText = new JTextField();
        SwingLayout.add(stringCmdPanel, stringCmdResultText, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(500, -1), new Dimension(500, -1), new Dimension(500, -1), 0);
        stringCmdLabel = new JLabel();
        stringCmdLabel.setText("用于生成可以绕WAF的Java命令");
        SwingLayout.add(stringCmdPanel, stringCmdLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return encodeUtilPanel;
    }

}
