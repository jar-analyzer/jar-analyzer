package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SCAStartActionListener implements ActionListener {
    private final List<SCARule> log4j2RuleList;
    private final List<SCARule> fastjsonRuleList;
    private final Map<String, CVEData> cveMap;

    public SCAStartActionListener(Map<String, CVEData> cve,
                                  List<SCARule> log4j,
                                  List<SCARule> fastjson) {
        this.cveMap = cve;
        this.log4j2RuleList = log4j;
        this.fastjsonRuleList = fastjson;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = MainForm.getInstance().getScaFileText().getText();
        if (StringUtil.isNull(text)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "PLEASE OPEN JAR/JARS DIR FIRST");
            return;
        }
        Path path = Paths.get(text);
        if (Files.notExists(path)) {
            SCALogger.logger.error("INPUT NOT EXIST");
            return;
        }
        List<String> jarList = new ArrayList<>();
        if (Files.isDirectory(path)) {
            SCALogger.logger.info("INPUT IS DIR");
            jarList = DirUtil.GetFiles(path.toAbsolutePath().toString());
        } else {
            SCALogger.logger.info("INPUT IS A FILE");
            jarList.add(path.toAbsolutePath().toString());
        }
        List<String> finalJarList = jarList;

        new Thread(() -> {
            List<SCAResult> cveList = new ArrayList<>();
            // 分析
            for (String s : finalJarList) {
                // 对于同一个 JAR 来说 CVE 不要重复
                List<String> exist = new ArrayList<>();
                execWithOneRule(cveList, s, exist, log4j2RuleList);
                execWithOneRule(cveList, s, exist, fastjsonRuleList);
            }
            if (cveList.isEmpty()) {
                SCALogger.logger.warn("NO VULNERABILITY FOUND");
                return;
            }
            for (SCAResult result : cveList) {
                String output = String.format(
                        "   CVE-ID: %s\n" +
                                "   DESC  : %s\n" +
                                "   CVSS  : %s\n" +
                                "   JAR   : %s\n" +
                                "   CLASS : %s\n" +
                                "   HASH  : %s\n\n",
                        result.getCVE(),
                        cveMap.get(result.getCVE()).getDesc(),
                        cveMap.get(result.getCVE()).getCvss(),
                        result.getJarPath(),
                        result.getKeyClass(),
                        result.getHash().substring(0, 16));
                SCALogger.logger.print(output);
            }
        }).start();
    }

    private void execWithOneRule(List<SCAResult> cveList,
                                 String s,
                                 List<String> exist,
                                 List<SCARule> log4j2RuleList) {
        for (SCARule rule : log4j2RuleList) {
            String keyClass = rule.getOnlyClassName();
            String keyHash = rule.getOnlyHash();
            byte[] data = SCAUtil.exploreJar(Paths.get(s).toFile(), keyClass);
            if (data == null) {
                continue;
            }
            String hash = SCAHashUtil.sha256(data);
            if (hash.equals(keyHash)) {
                if (exist.contains(rule.getCVE())) {
                    continue;
                }
                exist.add(rule.getCVE());
                SCAResult result = new SCAResult();
                result.setHash(hash);
                result.setCVE(rule.getCVE());
                result.setVersion(rule.getVersion());
                result.setJarPath(s);
                result.setProject(rule.getProjectName());
                result.setKeyClass(keyClass);
                cveList.add(result);
            }
        }
        SCAUtil.refresh();
    }
}