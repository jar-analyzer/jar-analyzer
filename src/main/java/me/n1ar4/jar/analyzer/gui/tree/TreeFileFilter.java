package me.n1ar4.jar.analyzer.gui.tree;

import java.io.File;

public class TreeFileFilter {
    private static final String INNER = "$";
    private static final String DECOMPILE_DIR = "jar-analyzer-decompile";
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
        if (file.getName().contains(INNER)) {
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
        return false;
    }
}
