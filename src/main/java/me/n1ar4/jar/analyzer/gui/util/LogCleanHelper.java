/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LogCleanHelper {
    public static MainForm instance = MainForm.getInstance();

    public static void build() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem cleanItem = new JMenuItem("CLEAN");
        cleanItem.setIcon(IconManager.cleanIcon);
        cleanItem.addActionListener(e -> instance.getLogArea().setText(null));
        popupMenu.add(cleanItem);

        instance.getLogArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}
