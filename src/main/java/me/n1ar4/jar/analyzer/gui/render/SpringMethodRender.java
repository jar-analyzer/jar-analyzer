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

import cn.hutool.core.util.StrUtil;
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
            // 使restfulType的长度固定在6个字符(Delete)，如果不足则用空格填充，并且适配DefaultListCellRenderer.setText的html格式,html请使用HTML_NBSP代替空格
            String restfulType = "<font style=\"color: #996666; font-weight: bold;\">" + StrUtil.fixLength(result.getRestfulType(), ' ', 8)
                    .replace(" ", StrUtil.HTML_NBSP) + "</font>";
            m = "<font style=\"color: blue; font-weight: bold;\">" + m + "</font>";
            String path = "<font style=\"color: green; font-weight: bold;\">" + result.getPath() + "</font>";
            setText("<html>" + " " + restfulType  + m + " " + path + "</html>");
        } else {
            return null;
        }
        return component;
    }
}
