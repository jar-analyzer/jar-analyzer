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

import me.n1ar4.jar.analyzer.gui.action.BuildAction;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

public class DropInstance extends DropTarget {
    private static final Logger logger = LogManager.getLogger();

    public DropInstance() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            Object obj = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            List<File> droppedFiles = (List<File>) obj;
            if (droppedFiles.size() != 1) {
                return;
            }
            String absPath = droppedFiles.get(0).getAbsolutePath();
            BuildAction.start(absPath);
        } catch (Exception ex) {
            logger.error("drop error: {}", ex.toString());
        }
    }
}
