/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

import me.n1ar4.jar.analyzer.gui.util.MenuUtil;

import java.io.File;

public class TreeFileFilter {
    private static final String INNER = "$";
    private static final String DECOMPILE_DIR = "jar-analyzer-decompile";
    private static final String CONSOLE_DLL = "console.dll";
    private final File file;
    private final boolean showFiles;
    private final boolean showHiddenFiles;

    public TreeFileFilter(File file,
                          boolean showFiles,
                          boolean showHiddenFiles) {
        this.file = file;
        this.showFiles = showFiles;
        this.showHiddenFiles = showHiddenFiles;
    }

    @SuppressWarnings("all")
    public boolean shouldFilter() {
        boolean showInner = MenuUtil.getShowInnerConfig().getState();
        if (!showInner && file.getName().contains(INNER)) {
            return true;
        }
        if (file.isFile() && !showFiles) {
            return true;
        }
        if (!showHiddenFiles && file.isHidden()) {
            return true;
        }
        if (file.getName().equals(DECOMPILE_DIR)) {
            return true;
        }
        if (file.getName().equals(CONSOLE_DLL)) {
            return true;
        }
        return false;
    }
}
