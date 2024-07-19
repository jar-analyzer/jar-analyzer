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
