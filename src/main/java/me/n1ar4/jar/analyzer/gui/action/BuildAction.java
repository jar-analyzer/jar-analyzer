/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.core.CoreRunner;
import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.adapter.SearchInputListener;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.gui.util.MenuUtil;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
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
                // 重建前必须关闭全部数据库连接（常驻会话 + 连接池），
                // 否则 macOS/Linux 上 unlink 后旧连接继续写孤儿文件、
                // 其他线程的新查询会打开空库；Windows 上直接删除失败
                DatabaseManager.closeForRebuild();
                SearchInputListener.resetSession();
                MainForm.setEngine(null);
                boolean deleted = false;
                try {
                    Files.delete(od);
                    // WAL 模式可能残留 sidecar 文件，一并删除
                    Files.deleteIfExists(Paths.get(Const.dbFile + "-wal"));
                    Files.deleteIfExists(Paths.get(Const.dbFile + "-shm"));
                    deleted = true;
                    LogUtil.info("delete old db success");
                } catch (Exception ex) {
                    LogUtil.error("cannot delete db : " + ex.getMessage());
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>" +
                                    "<p>无法删除之前的 <strong>jar-analyzer.db</strong> 请手动删除</p>" +
                                    "<p>" + ex.getMessage().trim() + "</p>" +
                                    "</html>");
                }
                // 无论删除是否成功都要恢复数据库可用：成功则打开新库，
                // 失败则重新挂回旧库文件
                DatabaseManager.reopen();
                if (!deleted) {
                    // engine 已置空，同步把引擎状态从 RUNNING 刷成 CLOSED，
                    // 避免标签误导用户以为旧库仍可查询
                    MainForm.getInstance().getEngineVal().setText("CLOSED");
                    MainForm.getInstance().getEngineVal().setForeground(Color.RED);
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

        JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());

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
            new Thread(() -> CoreRunner.run(Paths.get(path), rtJarPath, fixClass, dialog)).start();
        } else {
            new Thread(() -> CoreRunner.run(Paths.get(path), null, fixClass, dialog)).start();
        }
        MainForm.getInstance().getStartBuildDatabaseButton().setEnabled(false);
    }

    public static void run() {
        MainForm.getInstance().getStartBuildDatabaseButton().addActionListener(e -> {
            AnalyzeEnv.isCli = false;
            String path = MainForm.getInstance().getFileText().getText();
            start(path);
        });
    }
}
