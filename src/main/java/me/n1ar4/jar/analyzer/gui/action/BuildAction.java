package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.Runner;
import me.n1ar4.jar.analyzer.env.Const;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;
import org.apache.ibatis.logging.Log;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BuildAction {
    public static void run() {
        MainForm.getInstance().getStartBuildDatabaseButton().addActionListener(e -> {
            Path od = Paths.get(Const.dbFile);

            if (Files.exists(od)) {
                LogUtil.log("jar-analyzer database exist");
                int res = JOptionPane.showConfirmDialog(MainForm.getInstance().getMasterPanel(),
                        "<html>" +
                                "file <b>jar-analyzer.db</b> exist<br>" +
                                "do you want to delete the old db file?" +
                                "</html>");
                if (res == JOptionPane.OK_OPTION) {
                    LogUtil.log("delete old db");
                    try {
                        Files.delete(od);
                        LogUtil.log("delete old db success");
                    } catch (Exception ignored) {
                        LogUtil.log("cannot delete db");
                    }
                }
                if (res == JOptionPane.NO_OPTION) {
                    LogUtil.log("overwrite database");
                }
                if (res == JOptionPane.CANCEL_OPTION) {
                    LogUtil.log("cancel build process");
                    return;
                }
            }

            if(MainForm.getInstance().getDeleteTempCheckBox().isSelected()){
                LogUtil.log("start delete temp");
                DirUtil.removeDir(new File(Const.tempDir));
                // REFRESH TREE
                MainForm.getInstance().getFileTree().refresh();
                LogUtil.log("delete temp success");
            }

            String path = MainForm.getInstance().getFileText().getText();
            if (StringUtil.isNull(path)) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "cannot start build - jar is null");
                return;
            }
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
                new Thread(() -> Runner.run(Paths.get(path), rtJarPath)).start();
            } else {
                new Thread(() -> Runner.run(Paths.get(path), null)).start();
            }
            MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);
        });
    }
}
