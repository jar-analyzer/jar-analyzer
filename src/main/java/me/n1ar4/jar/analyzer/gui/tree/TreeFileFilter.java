/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
