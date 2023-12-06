package me.n1ar4.jar.analyzer.plugins.chatgpt;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ChatGPTForm {
    private JPanel masterPanel;
    private JTextField hostText;
    private JTextField portText;
    private JButton setButton;
    private JPanel socksPanel;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JRadioButton openAIRadioButton;
    private JRadioButton chatAnywhereRadioButton;
    private JTextArea inputArea;
    private JTextArea resultArea;
    private JPanel gptPanel;
    private JPanel apiHostPanel;
    private JLabel apiHostLabel;
    private JScrollPane inputScroll;
    private JScrollPane resultPanel;
    private JTextField apiKeyText;
    private JButton startButton;
    private JButton cleanButton;
    private JLabel apiKeyLabel;
    private JLabel actionLabel;
    private JLabel gptImgLabel;

    private static ChatGPTForm instance;
    private ChatGPT chatGPT;
    private String socksHost;
    private int socksPort;

    public static void init() {
        instance.chatAnywhereRadioButton.setSelected(true);
        ConfigFile config = MainForm.getConfig();
        if (config != null) {
            if (config.getGptHost() != null &&
                    !config.getGptHost().isEmpty() &&
                    !config.getGptHost().equals("null")) {
                instance.hostText.setText(config.getGptProxyHost());
                instance.portText.setText(config.getGptProxyPort());
                if (config.getGptHost().equals(ChatGPT.chatAnywhereHost)) {
                    instance.chatAnywhereRadioButton.setSelected(true);
                } else {
                    instance.openAIRadioButton.setSelected(true);
                }
                instance.apiKeyText.setText(config.getGptKey());
            }
        }
        instance.gptImgLabel.setIcon(IconManager.chatIcon);
        instance.cleanButton.addActionListener(e -> {
            instance.inputArea.setText(null);
            instance.resultArea.setText(null);
        });
        instance.setButton.addActionListener(e -> {
            String host = instance.hostText.getText();
            String port = instance.portText.getText();
            if (host == null || host.isEmpty()) {
                JOptionPane.showMessageDialog(instance.masterPanel, "need host");
                return;
            }
            if (port == null || port.isEmpty()) {
                JOptionPane.showMessageDialog(instance.masterPanel, "need port");
                return;
            }
            instance.socksHost = host;
            instance.socksPort = Integer.parseInt(port);
            JOptionPane.showMessageDialog(instance.masterPanel, "set proxy success");

            MainForm.getConfig().setGptProxyHost(instance.socksHost);
            MainForm.getConfig().setGptProxyPort(port);
            ConfigEngine.saveConfig(MainForm.getConfig());
        });
        instance.startButton.addActionListener(e -> {
            String apiHost = instance.openAIRadioButton.isSelected() ?
                    ChatGPT.openaiHost : ChatGPT.chatAnywhereHost;
            String apiKey = instance.apiKeyText.getText();
            String input = instance.inputArea.getText();
            if (apiKey == null || apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(instance.masterPanel, "need api key");
                return;
            }
            if (input == null || input.isEmpty()) {
                JOptionPane.showMessageDialog(instance.masterPanel, "need input");
                return;
            }
            if (instance.socksHost != null && !instance.socksHost.isEmpty()) {
                instance.chatGPT = ChatGPT.builder()
                        .apiHost(apiHost)
                        .apiKey(apiKey)
                        .socksProxy(instance.socksHost, instance.socksPort)
                        .build();
                MainForm.getConfig().setGptProxyHost(instance.socksHost);
                MainForm.getConfig().setGptProxyPort(String.valueOf(instance.socksPort));
            } else {
                instance.chatGPT = ChatGPT.builder()
                        .apiHost(apiHost)
                        .apiKey(apiKey)
                        .build();
            }
            MainForm.getConfig().setGptHost(apiHost);
            MainForm.getConfig().setGptKey(apiKey);
            ConfigEngine.saveConfig(MainForm.getConfig());
            new Thread(() -> {
                instance.chatGPT.init();
                instance.resultArea.setText("please wait...");
                String res = instance.chatGPT.chat(input);
                instance.resultArea.setText(res);
            }).start();
        });
    }

    public static void start() {
        JFrame frame = new JFrame(Const.ChatGPTForm);
        instance = new ChatGPTForm();
        init();
        frame.setContentPane(instance.masterPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
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
        masterPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        masterPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        socksPanel = new JPanel();
        socksPanel.setLayout(new GridLayoutManager(2, 3, new Insets(1, 1, 1, 1), -1, -1));
        panel1.add(socksPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        socksPanel.setBorder(BorderFactory.createTitledBorder(null, "Socks Proxy", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        hostLabel = new JLabel();
        hostLabel.setText("Host");
        socksPanel.add(hostLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hostText = new JTextField();
        socksPanel.add(hostText, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portLabel = new JLabel();
        portLabel.setText("Port");
        socksPanel.add(portLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portText = new JTextField();
        socksPanel.add(portText, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        setButton = new JButton();
        setButton.setText("Set");
        socksPanel.add(setButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        gptPanel = new JPanel();
        gptPanel.setLayout(new GridLayoutManager(1, 2, new Insets(1, 1, 1, 1), -1, -1));
        panel1.add(gptPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        gptPanel.setBorder(BorderFactory.createTitledBorder(null, "ChatGPT", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        apiHostPanel = new JPanel();
        apiHostPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        gptPanel.add(apiHostPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        apiHostLabel = new JLabel();
        apiHostLabel.setText("API Host");
        apiHostPanel.add(apiHostLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openAIRadioButton = new JRadioButton();
        openAIRadioButton.setText("OpenAI");
        apiHostPanel.add(openAIRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chatAnywhereRadioButton = new JRadioButton();
        chatAnywhereRadioButton.setText("ChatAnywhere");
        apiHostPanel.add(chatAnywhereRadioButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        apiKeyLabel = new JLabel();
        apiKeyLabel.setText("API Key");
        apiHostPanel.add(apiKeyLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        apiKeyText = new JTextField();
        apiHostPanel.add(apiKeyText, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        actionLabel = new JLabel();
        actionLabel.setText("Action");
        apiHostPanel.add(actionLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setText("Start");
        apiHostPanel.add(startButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cleanButton = new JButton();
        cleanButton.setText("Clean");
        apiHostPanel.add(cleanButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        gptImgLabel = new JLabel();
        gptImgLabel.setText("");
        gptPanel.add(gptImgLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputScroll = new JScrollPane();
        panel1.add(inputScroll, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(600, 100), null, null, 0, false));
        inputScroll.setBorder(BorderFactory.createTitledBorder(null, "Input", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputScroll.setViewportView(inputArea);
        resultPanel = new JScrollPane();
        panel1.add(resultPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(600, 300), null, null, 0, false));
        resultPanel.setBorder(BorderFactory.createTitledBorder(null, "Reslt", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultPanel.setViewportView(resultArea);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(openAIRadioButton);
        buttonGroup.add(chatAnywhereRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}
