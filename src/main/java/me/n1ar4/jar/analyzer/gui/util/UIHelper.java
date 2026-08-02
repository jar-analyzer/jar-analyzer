/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import javax.swing.*;

public class UIHelper {
    public static void setup() {
        UIManager.put("Tree.showDefaultIcons", true);
    }

    /**
     * Keep lists that are designed as single-line lists at a stable row height.
     * Swing's HTML label renderer otherwise reports a taller preferred size when
     * long content wraps, which makes individual JList rows uneven.
     */
    public static void fixSingleLineListCellHeight(JList<?>... lists) {
        for (JList<?> list : lists) {
            if (list == null) {
                continue;
            }
            int fontHeight = list.getFontMetrics(list.getFont()).getHeight() + 6;
            int lafHeight = UIManager.getInt("List.rowHeight");
            list.setFixedCellHeight(Math.max(fontHeight, lafHeight));
        }
    }
}
