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

import me.n1ar4.jar.analyzer.el.ELForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.plugins.bcel.BcelForm;
import me.n1ar4.jar.analyzer.plugins.encoder.EncodeUtilForm;
import me.n1ar4.jar.analyzer.plugins.listener.ListenUtilForm;
import me.n1ar4.jar.analyzer.plugins.serutil.SerUtilForm;
import me.n1ar4.jar.analyzer.plugins.sqlite.SQLiteForm;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;

public class PluginsAction {
    public static void startELForm() {
        JFrame frame = new JFrame(Const.SPELSearch);
        frame.setContentPane(new ELForm().elPanel);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void run() {
        MainForm.getInstance().getSqliteButton().addActionListener(e -> SQLiteForm.start());

        MainForm.getInstance().getEncoderBtn().addActionListener(e -> EncodeUtilForm.start());

        MainForm.getInstance().getListenerBtn().addActionListener(e -> ListenUtilForm.start());

        MainForm.getInstance().getSpringELButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getStartELSearchButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getSerUtilBtn().addActionListener(e -> SerUtilForm.start());

        MainForm.getInstance().getBcelBtn().addActionListener(e -> BcelForm.start());
    }
}
