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
import java.awt.*;

public class ZebraListCellRenderer extends DefaultListCellRenderer {

    /**
     * 根据主题基础背景色计算奇数行交替色。
     * 亮色主题：略微变暗；暗色主题：略微变亮。
     * 供所有 JList 渲染器复用。
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

    /**
     * 获取当前主题 List 背景色。
     */
    public static Color listBase(JList<?> list) {
        Color base = UIManager.getColor("List.background");
        return base != null ? base : list.getBackground();
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!isSelected) {
            Color base = listBase(list);
            c.setBackground(index % 2 == 0 ? base : zebraOdd(base));
        }
        return c;
    }
}
