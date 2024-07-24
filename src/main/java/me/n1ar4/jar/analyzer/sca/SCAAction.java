package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.font.FontHelper;

import java.util.List;

public class SCAAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }
        instance.getScaOutConsoleRadio().setSelected(true);
        // 设置输出
        if (SCALogger.logger == null) {
            SCALogger.logger = new SCALogger(instance.getScaConsoleArea());
        }
        // 目前仅支持 Apache Log4j2
        instance.getScaLog4jBox().setEnabled(true);
        instance.getScaLog4jBox().setSelected(true);
        // 暂不支持
        instance.getScaSpringBox().setEnabled(false);
        instance.getScaStrutsBox().setEnabled(false);
        instance.getScaFastjsonBox().setEnabled(false);
        instance.getScaTomcatBox().setEnabled(false);
        instance.getScaShiroBox().setEnabled(false);
        // 解析规则
        List<SCARule> log4jRuleList = SCAParser.getApacheLog4j2Rules();
        // 按钮绑定
        instance.getScaOpenBtn().addActionListener(new SCAOpenActionListener());
        instance.getScaStartBtn().addActionListener(new SCAStartActionListener(log4jRuleList));
    }
}
