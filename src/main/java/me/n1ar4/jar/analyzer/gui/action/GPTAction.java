package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.plugins.chatgpt.ChatGPT;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;

public class GPTAction {
    private static final String GPT_PREFIX = "请你分析这个Java类的作用是什么： ";

    public static void run() {
        MainForm instance = MainForm.getInstance();
        JButton gptRunBtn = instance.getGptStartBtn();
        JTextArea gptResult = instance.getGptResultArea();
        JTextField apiText = instance.getApiKeyText();
        JTextField hostText = instance.getGptHostText();

        gptRunBtn.addActionListener(e -> {
            String api = apiText.getText();
            String host = hostText.getText();
            if (StringUtil.isNull(api) || StringUtil.isNull(host)) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "need api key and host");
                return;
            }
            MethodResult cur = MainForm.getCurMethod();
            if (cur == null || StringUtil.isNull(cur.getClassName())) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "current method is null");
                return;
            }

            new Thread(() -> {
                LogUtil.log("start run chat gpt");
                ChatGPT chatGPT = ChatGPT.builder()
                        .apiKey(api)
                        .apiHost(host)
                        .build()
                        .init();
                gptRunBtn.setEnabled(false);
                String res = chatGPT.chat(GPT_PREFIX + cur.getClassName());
                LogUtil.log("chat gpt run finish");
                gptResult.setText(res);

                if (MainForm.getInstance().getAutoSaveCheckBox().isSelected()) {
                    ConfigFile config = ConfigEngine.parseConfig();
                    if (config == null) {
                        LogUtil.log("auto save error");
                        return;
                    }
                    config.setGptKey(api);
                    config.setGptHost(host);
                    MainForm.setConfig(config);
                    ConfigEngine.saveConfig(config);
                    LogUtil.log("auto save finish");
                }

                gptResult.setEnabled(true);
            }).start();
        });
    }
}
