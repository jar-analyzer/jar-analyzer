/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

public class DropHelper {
    private static final Logger logger = LogManager.getLogger();

    public static void setDrop() {
        MainForm instance = MainForm.getInstance();
        instance.getMasterPanel().setDropTarget(new DropInstance());
        instance.getBlackArea().setDropTarget(new DropInstance());
        MainForm.getCodeArea().setDropTarget(new DropInstance());
        logger.info("set drop target success");
    }
}
