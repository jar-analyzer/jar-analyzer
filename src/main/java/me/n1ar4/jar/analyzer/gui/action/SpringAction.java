package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;

public class SpringAction {
    public static void run() {
        JButton spRefreshBtn = MainForm.getInstance().getRefreshButton();
        spRefreshBtn.addActionListener(e -> CoreHelper.refreshSpringC());

        JButton pathSearchButton = MainForm.getInstance().getPathSearchButton();
        pathSearchButton.addActionListener(e -> CoreHelper.pathSearchC());
    }
}
