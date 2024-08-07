package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.sca.dto.CVEData;
import me.n1ar4.jar.analyzer.sca.dto.SCARule;
import me.n1ar4.jar.analyzer.sca.log.SCALogger;

import java.util.List;
import java.util.Map;

public class SCAAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }
        instance.getScaOutHtmlRadio().setSelected(true);
        instance.getScaOutConsoleRadio().setSelected(false);
        instance.getOutputFileText().setEnabled(false);
        // 设置输出
        if (SCALogger.logger == null) {
            SCALogger.logger = new SCALogger(instance.getScaConsoleArea());
        }
        instance.getScaLog4jBox().setEnabled(true);
        instance.getScaLog4jBox().setSelected(true);
        instance.getScaFastjsonBox().setEnabled(true);
        instance.getScaFastjsonBox().setSelected(true);
        instance.getScaShiroBox().setEnabled(true);
        instance.getScaShiroBox().setSelected(true);
        // 解析 CVE 数据库
        Map<String, CVEData> cveMap = SCAVulDB.getCVEMap();
        // 解析规则
        List<SCARule> log4jRuleList = SCAParser.getApacheLog4j2Rules();
        List<SCARule> fastjsonRuleList = SCAParser.getFastjsonRules();
        List<SCARule> shiroRuleList = SCAParser.getShiroRules();
        // 按钮绑定
        instance.getScaOpenBtn().addActionListener(new SCAOpenActionListener());
        instance.getScaResultOpenBtn().addActionListener(new SCAOpenResultListener());
        instance.getScaStartBtn().addActionListener(
                new SCAStartActionListener(
                        cveMap,
                        log4jRuleList,
                        fastjsonRuleList,
                        shiroRuleList));
    }
}
