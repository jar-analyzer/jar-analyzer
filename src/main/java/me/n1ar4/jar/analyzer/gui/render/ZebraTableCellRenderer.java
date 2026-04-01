/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.render;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ZebraTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * 根据当前主题背景色计算奇数行的交替色。
     * 亮色主题：略微变暗；暗色主题：略微变亮。
     * 供外部渲染器复用。
     */
    public static Color zebraOdd(Color base) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        if (hsb[2] > 0.5f) {
            hsb[2] = Math.max(0f, hsb[2] - 0.04f);
        } else {
            hsb[2] = Math.min(1f, hsb[2] + 0.06f);
        }
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            // 选中色也从主题读取，fallback 到固定值
            Color selBg = UIManager.getColor("Table.selectionBackground");
            Color selFg = UIManager.getColor("Table.selectionForeground");
            c.setBackground(selBg != null ? selBg : new Color(184, 207, 229));
            c.setForeground(selFg != null ? selFg : Color.BLACK);
        } else {
            Color base = UIManager.getColor("Table.background");
            if (base == null) base = table.getBackground();
            c.setBackground(row % 2 == 0 ? base : zebraOdd(base));
            c.setForeground(table.getForeground());
        }
        return c;
    }
}
