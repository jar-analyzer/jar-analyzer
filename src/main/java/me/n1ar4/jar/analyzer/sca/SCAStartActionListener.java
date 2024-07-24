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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> cveList = new HashSet<>();
        for (String s : jarList) {
            for (SCARule rule : log4j2RuleList) {
                byte[] data = SCAUtil.exploreJar(Paths.get(s).toFile(), rule.getKeyClassName());
                if (data == null) {
                    continue;
                }
                String hash = SCAHashUtil.sha256(data);
                if (hash.equals(rule.getHash())) {
                    cveList.add(rule.getCVE());
                }
            }
        }
        if (cveList.isEmpty()) {
            return;
        }
        SCALogger.logger.print("--- FIND Apache Log4j2 VULNERABILITY ---\n");
        for (String cve : cveList) {
            SCALogger.logger.print(cve + "\n");
        }
        SCALogger.logger.print("------------------------------------------------------\n");
    }
}