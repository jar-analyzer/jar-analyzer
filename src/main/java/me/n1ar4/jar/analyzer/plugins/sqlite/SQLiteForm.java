/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.sqlite;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.plugins.sqlite.ui.SQLitePanel;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;

public class SQLiteForm {
    private static SQLiteForm instance;
    private static SQLiteHelper helper = null;
    private final SQLitePanel panel;

    private SQLiteForm() {
        panel = new SQLitePanel();
    }

    public static SQLiteHelper getHelper() {
        return helper;
    }

    public static void setHelper(SQLiteHelper helper) {
        SQLiteForm.helper = helper;
    }

    public static SQLiteForm getInstance() {
        return instance;
    }

    public SQLitePanel getPanel() {
        return panel;
    }

    public JPanel getMasterPanel() {
        return panel;
    }

    public JButton getConnectButton() {
        return panel.getConnectButton();
    }

    public JTextArea getErrArea() {
        return panel.getErrArea();
    }

    public JTextField getSqliteText() {
        return panel.getDbFileField();
    }

    public JComboBox<String> getTablesBox() {
        return panel.getTablesBox();
    }

    public JTable getResultTable() {
        return panel.getResultTable();
    }

    public JButton getRunButton() {
        return panel.getRunButton();
    }

    public static JTextArea getSqlArea() {
        if (instance == null) {
            return null;
        }
        return instance.panel.getSqlArea();
    }

    public static void start() {
        JFrame frame = new JFrame(Const.SQLiteForm);
        instance = new SQLiteForm();
        ConnectAction.register();
        RunAction.register();
        frame.setContentPane(instance.panel);
        frame.setMinimumSize(new java.awt.Dimension(900, 700));
        frame.pack();
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
    }
}
