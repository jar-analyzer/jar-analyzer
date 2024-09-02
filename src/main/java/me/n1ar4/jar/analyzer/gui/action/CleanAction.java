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

import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CleanAction {
    public static void run() {
        MainForm.getInstance().getCleanButton().addActionListener(e -> {
            LogUtil.info("clean jar-analyzer");
            int res = JOptionPane.showConfirmDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "do you want to clean jar-analyzer?<br>" +
                            "delete jar-analyzer.db <br>" +
                            "delete jar-analyzer-temp <br>" +
                            "delete .jar-analyzer file <br>" +
                            "</html>");
            if (res == JOptionPane.OK_OPTION) {
                try {
                    Files.delete(Paths.get(Const.dbFile));
                } catch (Exception ignored) {
                }
                try {
                    Files.delete(Paths.get(ConfigEngine.CONFIG_FILE_PATH));
                } catch (Exception ignored) {
                }
                try {
                    DirUtil.removeDir(new File(Const.tempDir));
                } catch (Exception ignored) {
                }
                JOptionPane.showMessageDialog(
                        MainForm.getInstance().getMasterPanel(), "please restart");
                System.exit(0);
            }
            if (res == JOptionPane.NO_OPTION) {
                LogUtil.info("cancel clean");
            }
            if (res == JOptionPane.CANCEL_OPTION) {
                LogUtil.info("cancel clean");
            }
        });
    }
}
