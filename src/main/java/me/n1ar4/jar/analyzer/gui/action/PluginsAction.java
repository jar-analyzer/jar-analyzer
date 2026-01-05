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

import me.n1ar4.jar.analyzer.el.ELForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
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
        MainForm.getInstance().getStartELSearchButton().addActionListener(e -> startELForm());
    }
}
