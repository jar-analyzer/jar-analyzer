package me.n1ar4.jar.analyzer.gui.render;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.utils.ASMUtil;

import javax.swing.*;
import java.awt.*;

public class SpringMethodRender extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof MethodResult) {
            MethodResult result = (MethodResult) value;
            String m = ASMUtil.convertMethodDesc(result.getMethodName(), result.getMethodDesc());
            m = "<font style=\"color: blue; font-weight: bold;\">" + m + "</font>";
            String path = "<font style=\"color: green; font-weight: bold;\">" + result.getPath() + "</font>";
            setText("<html>" + m + "   " + path + "</html>");
        } else {
            return null;
        }
        return component;
    }
}
