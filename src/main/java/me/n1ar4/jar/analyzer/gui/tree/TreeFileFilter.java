/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

import me.n1ar4.jar.analyzer.gui.util.MenuUtil;
import me.n1ar4.jar.analyzer.starter.Const;

import java.io.File;
import java.nio.file.Path;

public class TreeFileFilter {
    private static final String INNER = "$";
    /**
     * 当前搜索跳转定位的内部类文件。show inner class 关闭时内部类
     * 整体隐藏，但被跳转的这一个必须单独显示，否则只能退回主类。
     * 跳转普通类时置回 null。
     */
    private static volatile Path exceptedInnerClass;
    private static final String DECOMPILE_DIR = "jar-analyzer-decompile";
    private static final String CONSOLE_DLL = "console.dll";
    /**
     * Working directory used by the Jar Diff feature. It lives directly
     * under {@link Const#tempDir} and is irrelevant to the user's analysis
     * targets, so it must not pollute the project file tree.
     */
    private static final String DIFF_DIR = "diff";
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

    public static void setExceptedInnerClass(Path target) {
        exceptedInnerClass = target;
    }

    @SuppressWarnings("all")
    public boolean shouldFilter() {
        boolean showInner = MenuUtil.getShowInnerConfig().getState();
        if (!showInner && file.getName().contains(INNER)
                && !file.toPath().equals(exceptedInnerClass)) {
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
        // Hide the diff working directory, but only when it sits directly
        // under jar-analyzer-temp/. A nested `diff` folder inside the user's
        // own jar contents must still be visible.
        if (file.isDirectory() && DIFF_DIR.equals(file.getName())) {
            File parent = file.getParentFile();
            if (parent != null && Const.tempDir.equals(parent.getName())) {
                return true;
            }
        }
        return false;
    }
}
