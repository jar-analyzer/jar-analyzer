/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
                            "delete jar-analyzer.db file <br>" +
                            "delete .jar-analyzer file <br>" +
                            "delete jar-analyzer-lockfile file <br>" +
                            "delete JAR-ANALYZER-ERROR.txt file <br>" +
                            "delete jar-analyzer-temp dir <br>" +
                            "delete jar-analyzer-document dir <br>" +
                            "delete jar-analyzer-export dir <br>" +
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
                    Files.delete(Paths.get("jar-analyzer-lockfile"));
                } catch (Exception ignored) {
                }
                try {
                    Files.delete(Paths.get("JAR-ANALYZER-ERROR.txt"));
                } catch (Exception ignored) {
                }
                try {
                    DirUtil.removeDir(new File(Const.tempDir));
                } catch (Exception ignored) {
                }
                try {
                    DirUtil.removeDir(new File(Const.indexDir));
                } catch (Exception ignored) {
                }
                try {
                    DirUtil.removeDir(new File("jar-analyzer-export"));
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
