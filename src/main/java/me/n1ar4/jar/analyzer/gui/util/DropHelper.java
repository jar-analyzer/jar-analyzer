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
