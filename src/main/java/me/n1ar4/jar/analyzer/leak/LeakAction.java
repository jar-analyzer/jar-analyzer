/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.leak;

import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.jar.analyzer.ai.AIConfigManager;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.entity.LeakResult;
import me.n1ar4.jar.analyzer.entity.LeakTriageEntry;
import me.n1ar4.jar.analyzer.entity.MemberEntity;
import me.n1ar4.jar.analyzer.exporter.LeakCsvExporter;
import me.n1ar4.jar.analyzer.gui.LeakAITriageForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.JarUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class LeakAction {
    private static final Logger logger = LogManager.getLogger();

    /**
     * 最近一次 AI 研判结果（用于"AI VIEW"按钮回看；包含通过+未通过+失败三类）
     */
    private static volatile List<LeakTriageEntry> lastTriageEntries = Collections.emptyList();

    public static List<LeakTriageEntry> getLastTriageEntries() {
        return lastTriageEntries;
    }

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

        public JCheckBox getCheckBox() {
            return checkBox;
        }

        public Function<String, List<String>> getRuleFunction() {
            return ruleFunction;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getLogName() {
            return logName;
        }
    }

    private static void log(String msg) {
        LogUtil.info("[leak] " + msg);
    }

    private interface ProgressCallback {
        void onProgress(String phase);
    }

    private static class ProgressState {
        private final JProgressBar progressBar;
        private final int total;
        private int done;
        private int lastPercent = -1;
        private long lastUpdateTime;

        private ProgressState(JProgressBar progressBar, int total) {
            this.progressBar = progressBar;
            this.total = Math.max(total, 0);
        }

        private void start(String text) {
            updateProgressBar(progressBar, 0, text);
        }

        private void advance(String phase) {
            done++;
            int percent = calcPercent(done, total);
            long now = System.currentTimeMillis();
            if (percent != lastPercent || now - lastUpdateTime > 200 || done >= total) {
                lastPercent = percent;
                lastUpdateTime = now;
                updateProgressBar(progressBar, percent,
                        phase + " (" + Math.min(done, total) + "/" + total + ")");
            }
        }

        private void finish(String text) {
            updateProgressBar(progressBar, 100, text);
        }
    }

    private static int calcPercent(int done, int total) {
        if (total <= 0) {
            return 0;
        }
        int percent = (int) Math.round(done * 100.0d / total);
        if (percent < 0) {
            return 0;
        }
        if (percent > 100) {
            return 100;
        }
        return percent;
    }

    private static void updateProgressBar(JProgressBar progressBar, int value, String text) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(value);
            progressBar.setString(text);
            progressBar.setStringPainted(true);
        });
    }

    private static List<Path> collectConfigFiles(Path tempDir) {
        List<Path> configFiles = new ArrayList<>();
        try {
            List<String> allFiles = DirUtil.GetFiles(tempDir.toString());
            for (String filePath : allFiles) {
                Path file = Paths.get(filePath);
                String fileName = file.getFileName().toString().toLowerCase();
                for (String ext : JarUtil.CONFIG_EXTENSIONS) {
                    if (fileName.endsWith(ext.toLowerCase())) {
                        configFiles.add(file);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("error while traversing static files: {}", e.toString());
        }
        return configFiles;
    }

    /**
     * 通用规则处理器
     *
     * @param config    规则配置
     * @param members   成员实体列表
     * @param stringMap 字符串映射
     * @param results   结果集合
     */
    private static void processRule(RuleConfig config, List<MemberEntity> members,
                                    Map<String, String> stringMap, List<Path> configFiles,
                                    Path tempDir, Set<LeakResult> results,
                                    ProgressCallback progress) {
        if (!config.getCheckBox().isSelected()) {
            return;
        }

        log(config.getLogName() + " leak start");

        // 处理成员实体
        for (MemberEntity member : members) {
            try {
                List<String> data = config.getRuleFunction().apply(member.getValue());
                if (!data.isEmpty()) {
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(member.getClassName());
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName(config.getTypeName());
                        results.add(leakResult);
                    }
                }
            } finally {
                progress.onProgress(config.getLogName() + " members");
            }
        }

        try {
            List<Path> allFiles = configFiles;
            for (Path file : allFiles) {
                String filePath = file.toString();
                String fileName = file.getFileName().toString().toLowerCase();

                // 检查文件是否符合配置文件扩展名规则
                boolean isConfigFile = false;
                for (String ext : JarUtil.CONFIG_EXTENSIONS) {
                    if (fileName.endsWith(ext.toLowerCase())) {
                        isConfigFile = true;
                        break;
                    }
                }

                if (isConfigFile) {
                    try {
                        // 读取文件内容
                        byte[] fileBytes = Files.readAllBytes(file);
                        String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

                        // 应用规则函数进行匹配
                        List<String> data = config.getRuleFunction().apply(fileContent);
                        if (!data.isEmpty()) {
                            for (String s : data) {
                                LeakResult leakResult = new LeakResult();
                                // 使用相对于 tempDir 的路径作为类名
                                String relativePath = tempDir.relativize(file).toString()
                                        .replace("\\", "/");
                                leakResult.setClassName(relativePath);
                                leakResult.setValue(s.trim());
                                leakResult.setTypeName(config.getTypeName());
                                results.add(leakResult);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("read config file failed: {} - {}", filePath, e.getMessage());
                    }
                }
                progress.onProgress(config.getLogName() + " config files");
            }
        } catch (Exception e) {
            logger.error("error while traversing static files: {}", e.toString());
        }

        // 处理字符串映射
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            try {
                String className = entry.getKey();
                String value = entry.getValue();
                List<String> data = config.getRuleFunction().apply(value);
                if (!data.isEmpty()) {
                    for (String s : data) {
                        LeakResult leakResult = new LeakResult();
                        leakResult.setClassName(className);
                        leakResult.setValue(s.trim());
                        leakResult.setTypeName(config.getTypeName());
                        results.add(leakResult);
                    }
                }
            } finally {
                progress.onProgress(config.getLogName() + " strings");
            }
        }

        log(config.getLogName() + " leak finish");
    }

    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }

        JButton export = instance.getExportLeakBtn();

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
        JCheckBox aiTriageBox = instance.getLeakAITriageBox();
        JButton aiTriageViewBtn = instance.getLeakAITriageViewBtn();
        JProgressBar progressBar = instance.getLeakProgressBar();
        updateProgressBar(progressBar, 0, "ready");

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
        // AI 研判默认关闭，避免用户在未配置 AI 时误开
        aiTriageBox.setSelected(false);

        // 勾选 AI Triage 时立即检查 LLM 配置，未配置则提示并取消勾选
        aiTriageBox.addActionListener(e -> {
            if (!aiTriageBox.isSelected()) {
                return;
            }
            AIConfig cfg = AIConfigManager.getActive();
            if (!cfg.isReady()) {
                aiTriageBox.setSelected(false);
                JOptionPane.showMessageDialog(instance.getMasterPanel(),
                        "<html>AI 尚未配置，无法启用研判。<br><br>" +
                                "请先在菜单栏 <b>AI</b> → <b>AI 配置</b> 中填写：<br>" +
                                "&nbsp;&nbsp;• Base URL（OpenAI 兼容协议）<br>" +
                                "&nbsp;&nbsp;• Model<br>" +
                                "&nbsp;&nbsp;• API Key<br><br>" +
                                "并选择启用一条配置后，再回来勾选。</html>",
                        "AI 未配置",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                // 简单回显当前生效的 provider/model（不显示 apiKey）
                String name = cfg.getName() == null || cfg.getName().isEmpty() ? "(未命名)" : cfg.getName();
                log("ai triage enabled, profile=" + name +
                        ", provider=" + cfg.getProvider() +
                        ", model=" + cfg.getModel());
            }
        });


        JList<LeakResult> leakList = instance.getLeakResultList();

        logger.info("registering leak action");

        // 添加导出功能
        export.addActionListener(e -> {
            DefaultListModel<LeakResult> model = (DefaultListModel<LeakResult>) leakList.getModel();
            if (model == null || model.isEmpty()) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "没有泄露检测结果可以导出");
                return;
            }

            List<LeakResult> results = new ArrayList<>();
            for (int i = 0; i < model.getSize(); i++) {
                results.add(model.getElementAt(i));
            }

            LeakCsvExporter exporter = new LeakCsvExporter(results);
            boolean success = exporter.doExport();
            if (success) {
                String fileName = exporter.getFileName();
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "导出成功: " + fileName);
            } else {
                JOptionPane.showMessageDialog(instance.getMasterPanel(), "导出失败");
            }
        });

        // AI 研判面板查看
        aiTriageViewBtn.addActionListener(e -> {
            List<LeakTriageEntry> entries = lastTriageEntries;
            if (entries == null || entries.isEmpty()) {
                JOptionPane.showMessageDialog(instance.getMasterPanel(),
                        "暂无 AI 研判数据，请先勾选 AI Triage 并执行 START");
                return;
            }
            LeakAITriageForm.start(entries);
        });

        instance.getLeakStartBtn().addActionListener(e -> {
            // START 前再做一次 AI 配置校验（双保险：勾选后用户可能又改/删了配置）
            if (aiTriageBox.isSelected()) {
                AIConfig cfg = AIConfigManager.getActive();
                if (!cfg.isReady()) {
                    int choice = JOptionPane.showConfirmDialog(instance.getMasterPanel(),
                            "<html>已勾选 <b>AI Triage</b>，但当前 AI 未配置或配置不完整。<br><br>" +
                                    "<b>是</b>：取消 AI 研判，继续按原始规则扫描<br>" +
                                    "<b>否</b>：本次不执行扫描，去配置 AI</html>",
                            "AI 未配置",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                    aiTriageBox.setSelected(false);
                }
            }
            instance.getLeakStartBtn().setEnabled(false);
            updateProgressBar(progressBar, 0, "leak scan starting");
            new Thread(() -> {
                try {
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
                    List<RuleConfig> activeConfigs = new ArrayList<>();
                    for (RuleConfig config : ruleConfigs) {
                        if (config.getCheckBox().isSelected()) {
                            activeConfigs.add(config);
                        }
                    }
                    if (activeConfigs.isEmpty()) {
                        log("no leak rule selected");
                        updateProgressBar(progressBar, 0, "no leak rule selected");
                        return;
                    }

                    Path tempDir = Paths.get(Const.tempDir).toAbsolutePath();
                    List<Path> configFiles = collectConfigFiles(tempDir);
                    int scanTotal = activeConfigs.size() *
                            (members.size() + configFiles.size() + stringMap.size());
                    ProgressState scanProgress = new ProgressState(progressBar, scanTotal);
                    scanProgress.start("leak scan start");

                    for (RuleConfig config : activeConfigs) {
                        processRule(config, members, stringMap, configFiles, tempDir, results,
                                scanProgress::advance);
                    }
                    scanProgress.finish("leak scan finished, found=" + results.size());

                    // AI 研判（可选）
                    List<LeakResult> finalResults;
                    if (aiTriageBox.isSelected() && !results.isEmpty()) {
                        List<LeakResult> raw = new ArrayList<>(results);
                        final int total = raw.size();
                        log("ai triage start (total=" + total + ")");
                        ProgressState aiProgress = new ProgressState(progressBar, total);
                        aiProgress.start("ai triage start");
                        List<LeakTriageEntry> entries = LeakAITriageService.triageAll(raw,
                                (done, t, current) -> {
                                    aiProgress.advance("ai triage");
                                    // 每 5 条或最后一条刷新一次进度
                                    if (done == t || done % 5 == 0) {
                                        log("ai triage progress: " + done + "/" + t);
                                    }
                                });
                        aiProgress.finish("ai triage finished");
                        lastTriageEntries = entries;
                        // 仅保留 AI 判定为敏感的（未通过的从最终结果剔除）
                        finalResults = new ArrayList<>(entries.size());
                        int filtered = 0;
                        for (LeakTriageEntry entry : entries) {
                            if (entry.isSensitive()) {
                                finalResults.add(entry.getResult());
                            } else {
                                filtered++;
                            }
                        }
                        log("ai triage finish, filtered=" + filtered + ", kept=" + finalResults.size());
                    } else {
                        // 未启用 AI：清空旧的研判记录，避免误以为是当前结果
                        lastTriageEntries = Collections.emptyList();
                        finalResults = new ArrayList<>(results);
                    }

                    // 更新UI
                    DefaultListModel<LeakResult> model = new DefaultListModel<>();
                    for (LeakResult leakResult : finalResults) {
                        model.addElement(leakResult);
                    }
                    SwingUtilities.invokeLater(() -> leakList.setModel(model));
                } catch (Exception ex) {
                    logger.error("leak scan failed: {}", ex.toString());
                    log("leak scan failed: " + ex.getMessage());
                    updateProgressBar(progressBar, 0, "leak scan failed");
                } finally {
                    SwingUtilities.invokeLater(() -> instance.getLeakStartBtn().setEnabled(true));
                }
            }).start();
        });

        instance.getLeakCleanBtn().addActionListener(e -> {
            leakList.setModel(new DefaultListModel<>());
            lastTriageEntries = Collections.emptyList();
            updateProgressBar(progressBar, 0, "ready");
            JOptionPane.showMessageDialog(instance.getMasterPanel(), "clean data finish");
        });
    }
}
