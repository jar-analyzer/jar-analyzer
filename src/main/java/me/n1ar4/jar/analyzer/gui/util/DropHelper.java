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

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.awt.*;

public class DropHelper {
    private static final Logger logger = LogManager.getLogger();

    public static void setDrop() {
        MainForm instance = MainForm.getInstance();
        // 递归为所有子组件设置拖拽目标，实现任意位置拖拽
        setDropRecursive(instance.getMasterPanel());
        logger.info("set drop target success");
    }

    private static void setDropRecursive(Component component) {
        component.setDropTarget(new DropInstance());
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                setDropRecursive(child);
            }
        }
    }
}
