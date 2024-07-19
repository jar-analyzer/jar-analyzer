package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.CoreRunner;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.gui.util.MenuUtil;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BuildAction {
    public static void start(String path) {
        Path od = Paths.get(Const.dbFile);
        MainForm.getInstance().getFileText().setText(path);

        if (Files.exists(od)) {
            LogUtil.info("jar-analyzer database exist");
            int res = JOptionPane.showConfirmDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "file <b>jar-analyzer.db</b> exist<br>" +
                            "do you want to delete the old db file?" +
                            "</html>");
            if (res == JOptionPane.OK_OPTION) {
                LogUtil.info("delete old db");
                try {
                    Files.delete(od);
                    LogUtil.info("delete old db success");
                } catch (Exception ex) {
                    LogUtil.error("cannot delete db : " + ex.getMessage());
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>" +
                                    "<p>无法删除之前的 <strong>jar-analyzer.db</strong> 请手动删除</p>" +
                                    "<p>" + ex.getMessage().trim() + "</p>" +
                                    "</html>");
                    return;
                }
            }
            if (res == JOptionPane.NO_OPTION) {
                LogUtil.info("overwrite database");
            }
            if (res == JOptionPane.CANCEL_OPTION) {
                LogUtil.info("cancel build process");
                return;
            }
        }

        if (MainForm.getInstance().getDeleteTempCheckBox().isSelected()) {
            LogUtil.info("start delete temp");
            DirUtil.removeDir(new File(Const.tempDir));
            // REFRESH TREE
            MainForm.getInstance().getFileTree().refresh();
            LogUtil.info("delete temp success");
        }

        if (StringUtil.isNull(path)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "cannot start build - jar is null");
            return;
        }

        boolean fixClass = MenuUtil.getFixClassPathConfig().getState();

        if (MainForm.getInstance().getAddRtJarWhenCheckBox().isSelected()) {
            String text = MainForm.getInstance().getRtText().getText();
            if (StringUtil.isNull(text)) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "rt.jar file is null");
                return;
            }
            Path rtJarPath = Paths.get(text);
            if (!Files.exists(rtJarPath)) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "rt.jar file not exist");
                return;
            }
            new Thread(() -> CoreRunner.run(Paths.get(path), rtJarPath, fixClass)).start();
        } else {
            new Thread(() -> CoreRunner.run(Paths.get(path), null, fixClass)).start();
        }
        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);
    }

    public static void run() {
        MainForm.getInstance().getStartBuildDatabaseButton().addActionListener(e -> {
            String path = MainForm.getInstance().getFileText().getText();
            start(path);
        });
    }
}
