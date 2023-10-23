package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.dto.MethodResult;
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
