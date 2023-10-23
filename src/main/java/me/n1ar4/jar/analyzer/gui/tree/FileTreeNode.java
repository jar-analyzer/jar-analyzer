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
