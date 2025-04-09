/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

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
import java.util.HashSet;
import java.util.Set;

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

        JRadioButton equalsRadio = MainForm.getInstance().getEqualsSearchRadioButton();
        JRadioButton likeRadio = MainForm.getInstance().getLikeSearchRadioButton();

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
                if (equalsRadio.isSelected()) {
                    new Thread(() -> CoreHelper.refreshCallSearch(
                            scText.getText(), smText.getText(), null)).start();
                }
                if (likeRadio.isSelected()) {
                    new Thread(() -> CoreHelper.refreshCallSearchLike(
                            scText.getText(), smText.getText(), null)).start();
                }
            }

            if (methodDefRadio.isSelected()) {
                if (equalsRadio.isSelected()) {
                    new Thread(() -> CoreHelper.refreshDefSearch(
                            scText.getText(), smText.getText(), null)).start();
                }
                if (likeRadio.isSelected()) {
                    new Thread(() -> CoreHelper.refreshDefSearchLike(
                            scText.getText(), smText.getText(), null)).start();
                }
            }

            if (stringRadio.isSelected()) {
                new Thread(() -> CoreHelper.refreshStrSearch(scText.getText(), ssText.getText())).start();
            }

            if (binaryRadio.isSelected()) {
                String search = ssText.getText();
                ArrayList<String> jars = engine.getJarsPath();

                Set<String> result = new HashSet<>();

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
                                        // FIX 2024/11/19
                                        // 可能弹出一大堆很多次
                                        // 去重保证一次即可
                                        result.add(jarPath);
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
                                    // FIX 2024/11/19
                                    // 可能弹出一大堆很多次
                                    // 去重保证一次即可
                                    result.add(jarPath);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (result.isEmpty()) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>not found</html>");
                    return;
                }

                StringBuilder jarBuilder = new StringBuilder();
                for (String data : result) {
                    jarBuilder.append(data);
                    jarBuilder.append("<br>");
                }

                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "<html>search string [" + search + "] result:<br>"
                                + jarBuilder + "</html>");

                // not need to select search panel
                return;
            }

            MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);
        });
    }
}
