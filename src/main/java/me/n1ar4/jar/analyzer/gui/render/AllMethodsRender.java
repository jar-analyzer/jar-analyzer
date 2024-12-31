/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import javax.swing.*;
import java.awt.*;

public class AllMethodsRender extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof MethodResult) {
            MethodResult result = (MethodResult) value;
            setText("<html>" + ASMUtil.convertMethodDesc(
                    result.getMethodName(), result.getMethodDesc()) + "</html>");
        } else {
            return null;
        }
        return component;
    }
}
