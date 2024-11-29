/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.entity.ClassResult;

import javax.swing.*;
import java.awt.*;

public class ClassRender extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof ClassResult) {
            ClassResult result = (ClassResult) value;
            String className = result.getClassName().replace("/", ".");
            className = "<font style=\"color: orange; font-weight: bold;\">" + className + "</font>";
            setText("<html>" + className + "</html>");
        } else {
            return null;
        }
        return component;
    }
}
