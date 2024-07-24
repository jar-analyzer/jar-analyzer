package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.List;
import java.util.Map;

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
        instance.getScaLog4jBox().setEnabled(true);
        instance.getScaLog4jBox().setSelected(true);
        instance.getScaFastjsonBox().setEnabled(true);
        instance.getScaFastjsonBox().setSelected(true);
        // 暂不支持
        instance.getScaSpringBox().setEnabled(false);
        instance.getScaStrutsBox().setEnabled(false);
        instance.getScaTomcatBox().setEnabled(false);
        instance.getScaShiroBox().setEnabled(false);
        // 解析 CVE 数据库
        Map<String, CVEData> cveMap = SCAVulDB.getCVEMap();
        // 解析规则
        List<SCARule> log4jRuleList = SCAParser.getApacheLog4j2Rules();
        List<SCARule> fastjsonRuleList = SCAParser.getFastjsonRules();
        // 按钮绑定
        instance.getScaOpenBtn().addActionListener(new SCAOpenActionListener());
        instance.getScaStartBtn().addActionListener(
                new SCAStartActionListener(cveMap, log4jRuleList, fastjsonRuleList));
    }
}
