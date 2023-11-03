package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import javax.swing.*;
import java.awt.*;

public class MethodCallRender extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof MethodResult) {
            MethodResult result = (MethodResult) value;
            String className = result.getClassName().replace("/", ".");
            className = "<font style=\"color: orange; font-weight: bold;\">" + className + "</font>";
            String m = ASMUtil.convertMethodDesc(result.getMethodName(), result.getMethodDesc());
            setText("<html>" + className + "   " + m + "</html>");
        } else {
            return null;
        }
        return component;
    }
}
