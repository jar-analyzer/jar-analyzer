/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
