/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

import java.io.File;

public class FileTreeNode {
    public FileTreeNode(File file) {
        if (file == null) {
            throw new IllegalArgumentException("null file not allowed");
        }
        this.file = file;
    }

    public String toString() {
        return file.getName();
    }

    public File file;
}
