/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.entity.MemberEntity;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LeakAction {
    private static final Logger logger = LogManager.getLogger();

    // 规则配置类
    private static class RuleConfig {
        private final JCheckBox checkBox;
        private final Function<String, List<String>> ruleFunction;
        private final String typeName;
        private final String logName;

        public RuleConfig(JCheckBox checkBox, Function<String, List<String>> ruleFunction, 
                         String typeName, String logName) {
            this.checkBox = checkBox;
            this.ruleFunction = ruleFunction;
            this.typeName = typeName;
            this.logName = logName;
        }

        public JCheckBox getCheckBox() { return checkBox; }
        public Function<String, List<String>> getRuleFunction() { return ruleFunction; }
        public String getTypeName() { return typeName; }
        public String getLogName() { return logName; }
    }

    private static void log(String msg) {
        msg = "[LOG] " + msg + "\n";
        MainForm.getInstance().getLeakLogArea().append(msg);
        MainForm.getInstance().getLeakLogArea().setCaretPosition(
                MainForm.getInstance().getLeakLogArea().getDocument().getLength()
        );
    }

    /**
     * 通用规则处理器
     * @param config 规则配置
     * @param members 成员实体列表
     * @param stringMap 字符串映射
     * @param results 结果集合
     */
    private static void processRule(RuleConfig config, List<MemberEntity> members, 
                                   Map<String, String> stringMap, Set<LeakResult> results) {
        if (!config.getCheckBox().isSelected()) {
            return;
        }

        log(config.getLogName() + " leak start");
        
        // 处理成员实体
        for (MemberEntity member : members) {
            List<String> data = config.getRuleFunction().apply(member.getValue());
            if (data.isEmpty()) {
                continue;
            }
            for (String s : data) {
                LeakResult leakResult = new LeakResult();
                leakResult.setClassName(member.getClassName());
                leakResult.setValue(s.trim());
                leakResult.setTypeName(config.getTypeName());
                results.add(leakResult);
            }
        }
        
        // 处理字符串映射
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            String className = entry.getKey();
            String value = entry.getValue();
            List<String> data = config.getRuleFunction().apply(value);
            if (data.isEmpty()) {
                continue;
            }
            for (String s : data) {
                LeakResult leakResult = new LeakResult();
                leakResult.setClassName(className);
                leakResult.setValue(s.trim());
                leakResult.setTypeName(config.getTypeName());
                results.add(leakResult);
            }
        }
        
        log(config.getLogName() + " leak finish");
    }

    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }

        // 获取所有复选框
        JCheckBox jwtBox = instance.getLeakJWTBox();
        JCheckBox idCardBox = instance.getLeakIdBox();
        JCheckBox ipAddrBox = instance.getLeakIpBox();
        JCheckBox emailBox = instance.getLeakEmailBox();
        JCheckBox urlBox = instance.getLeakUrlBox();
        JCheckBox jdbcBox = instance.getLeakJdbcBox();
        JCheckBox filePathBox = instance.getLeakFileBox();
        JCheckBox macAddrBox = instance.getLeakMacBox();
        JCheckBox phoneBox = instance.getLeakPhoneBox();
        JCheckBox apiKeyBox = instance.getAPIKeyCheckBox();
        JCheckBox bankBox = instance.getBankCardCheckBox();
        JCheckBox cloudAkSkBox = instance.getAKSKCheckBox();
        JCheckBox cryptoBox = instance.getCryptoKeyCheckBox();
        JCheckBox aiKeyBox = instance.getAIKeyCheckBox();
        JCheckBox passBox = instance.getPasswordCheckBox();

        // 设置默认选中状态
        jwtBox.setSelected(true);
        idCardBox.setSelected(true);
        ipAddrBox.setSelected(true);
        emailBox.setSelected(true);
        urlBox.setSelected(true);
        jdbcBox.setSelected(true);
        filePathBox.setSelected(true);
        macAddrBox.setSelected(true);
        phoneBox.setSelected(true);
        apiKeyBox.setSelected(true);
        bankBox.setSelected(true);
        cloudAkSkBox.setSelected(true);
        cryptoBox.setSelected(true);
        aiKeyBox.setSelected(true);
        passBox.setSelected(true);

        JList<LeakResult> leakList = instance.getLeakResultList();

        logger.info("registering leak action");
        instance.getLeakStartBtn().addActionListener(e -> new Thread(() -> {
            CoreEngine engine = MainForm.getEngine();
            List<MemberEntity> members = engine.getAllMembersInfo();
            Map<String, String> stringMap = engine.getStringMap();

            Set<LeakResult> results = new LinkedHashSet<>();

            // 配置所有规则
            RuleConfig[] ruleConfigs = {
                new RuleConfig(jwtBox, JWTRule::match, "JWT-TOKEN", "jwt-token"),
                new RuleConfig(idCardBox, IDCardRule::match, "ID-CARD", "id-card"),
                new RuleConfig(ipAddrBox, IPAddressRule::match, "IP-ADDR", "ip-addr"),
                new RuleConfig(emailBox, EmailRule::match, "EMAIL", "email"),
                new RuleConfig(urlBox, UrlRule::match, "URL", "url"),
                new RuleConfig(jdbcBox, JDBCRule::match, "JDBC", "jdbc"),
                new RuleConfig(filePathBox, FilePathRule::match, "FILE-PATH", "file-path"),
                new RuleConfig(macAddrBox, MacAddressRule::match, "MAC-ADDR", "mac-addr"),
                new RuleConfig(phoneBox, PhoneRule::match, "PHONE", "phone"),
                new RuleConfig(apiKeyBox, ApiKeyRule::match, "API-KEY", "api-key"),
                new RuleConfig(bankBox, BankCardRule::match, "BANK-CARD", "bank-card"),
                new RuleConfig(cloudAkSkBox, CloudAKSKRule::match, "CLOUD-AKSK", "cloud-aksk"),
                new RuleConfig(cryptoBox, CryptoKeyRule::match, "CRYPTO-KEY", "crypto-key"),
                new RuleConfig(aiKeyBox, OpenAITokenRule::match, "AI-KEY", "ai-key"),
                new RuleConfig(passBox, PasswordRule::match, "PASSWORD", "password")
            };

            // 处理所有规则
            for (RuleConfig config : ruleConfigs) {
                processRule(config, members, stringMap, results);
            }

            // 更新UI
            DefaultListModel<LeakResult> model = new DefaultListModel<>();
            for (LeakResult leakResult : results) {
                model.addElement(leakResult);
            }
            leakList.setModel(model);
        }).start());
        
        instance.getLeakCleanBtn().addActionListener(e -> {
            leakList.setModel(new DefaultListModel<>());
            JOptionPane.showMessageDialog(instance.getMasterPanel(), "clean data finish");
        });
    }
}
