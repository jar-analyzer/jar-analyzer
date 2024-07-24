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

public class SCAStartActionListener implements ActionListener {
    public List<SCARule> log4j2RuleList;

    public SCAStartActionListener(List<SCARule> log4j) {
        this.log4j2RuleList = log4j;
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
            // APACHE LOG4J2
            List<SCAResult> cveList = new ArrayList<>();
            // 分析
            for (String s : finalJarList) {
                // 对于同一个 JAR 来说 CVE 不要重复
                List<String> exist = new ArrayList<>();
                for (SCARule rule : log4j2RuleList) {
                    byte[] data = SCAUtil.exploreJar(Paths.get(s).toFile(), rule.getKeyClassName());
                    if (data == null) {
                        continue;
                    }
                    String hash = SCAHashUtil.sha256(data);
                    if (hash.equals(rule.getHash())) {
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
                        result.setKeyClass(rule.getKeyClassName());
                        cveList.add(result);
                    }
                }
            }
            if (cveList.isEmpty()) {
                SCALogger.logger.warn("NO VULNERABILITY FOUND");
                return;
            }
            SCALogger.logger.print("--------------- FIND VULNERABILITY ---------------\n");
            for (SCAResult result : cveList) {
                SCALogger.logger.print(result + "\n");
            }
            SCALogger.logger.print("--------------------------------------------------\n");
        }).start();
    }
}