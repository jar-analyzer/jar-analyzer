package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.dto.ClassResult;

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
