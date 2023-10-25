package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SearchAction {
    public static void run() {
        JButton searchBtn = MainForm.getInstance().getStartSearchButton();
        JTextField scText = MainForm.getInstance().getSearchClassText();
        JTextField smText = MainForm.getInstance().getSearchMethodText();
        JTextField ssText = MainForm.getInstance().getSearchStrText();
        JRadioButton methodDefRadio = MainForm.getInstance().getMethodDefinitionRadioButton();
        JRadioButton methodCallRadio = MainForm.getInstance().getMethodCallRadioButton();
        JRadioButton binaryRadio = MainForm.getInstance().getBinarySearchRadioButton();
        JRadioButton stringRadio = MainForm.getInstance().getStringContainsRadioButton();
        CoreEngine engine = MainForm.getEngine();
        searchBtn.addActionListener(e -> {
            if (methodCallRadio.isSelected() || methodDefRadio.isSelected()) {
                if (StringUtil.isNull(smText.getText())) {
                    JOptionPane.showMessageDialog(
                            MainForm.getInstance().getMasterPanel(), "need method data");
                    return;
                }
            }
            if (stringRadio.isSelected() || binaryRadio.isSelected()) {
                if (StringUtil.isNull(ssText.getText())) {
                    JOptionPane.showMessageDialog(
                            MainForm.getInstance().getMasterPanel(), "need search data");
                    return;
                }
            }

            if (methodCallRadio.isSelected()) {
                new Thread(() -> CoreHelper.refreshCallSearch(
                        scText.getText(), smText.getText(), null)).start();
            }

            if (methodDefRadio.isSelected()) {
                new Thread(() -> CoreHelper.refreshDefSearch(
                        scText.getText(), smText.getText(), null)).start();
            }

            if (stringRadio.isSelected()) {
                new Thread(() -> CoreHelper.refreshStrSearch(ssText.getText())).start();
            }

            if (binaryRadio.isSelected()) {
                String search = ssText.getText();
                ArrayList<String> jars = engine.getJarsPath();
                for (String jarPath : jars) {
                    try {
                        Path path = Paths.get(jarPath);
                        if (Files.size(path) > 1024 * 1024 * 50) {
                            FileInputStream fis = new FileInputStream(path.toFile());
                            byte[] searchContext = search.getBytes();
                            byte[] data = new byte[16384];
                            while (fis.read(data, 0, data.length) != -1) {
                                for (int i = 0; i < data.length - searchContext.length + 1; ++i) {
                                    boolean found = true;
                                    for (int j = 0; j < searchContext.length; ++j) {
                                        if (data[i + j] != searchContext[j]) {
                                            found = false;
                                            break;
                                        }
                                    }
                                    if (found) {
                                        fis.close();
                                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                                                "found: " + jarPath);
                                        return;
                                    }
                                }
                            }
                            fis.close();
                        } else {
                            byte[] searchContext = search.getBytes();
                            byte[] data = Files.readAllBytes(path);
                            for (int i = 0; i < data.length - searchContext.length + 1; ++i) {
                                boolean found = true;
                                for (int j = 0; j < searchContext.length; ++j) {
                                    if (data[i + j] != searchContext[j]) {
                                        found = false;
                                        break;
                                    }
                                }
                                if (found) {
                                    JOptionPane.showMessageDialog(
                                            MainForm.getInstance().getMasterPanel(), "found: " + jarPath);
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(), "not found");
                    } catch (Exception ignored) {
                    }
                }
                // not need to select search panel
                return;
            }

            MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);
        });
    }
}
