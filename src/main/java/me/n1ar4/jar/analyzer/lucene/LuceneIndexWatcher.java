/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;

public class LuceneIndexWatcher extends Thread {
    private final JLabel sizeLabel;
    private final File directoryToWatch;

    public LuceneIndexWatcher(JLabel label) {
        this.sizeLabel = label;
        this.directoryToWatch = Paths.get(Const.indexDir).toFile();
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (true) {
            long totalSize = calculateDirectorySize(directoryToWatch);
            updateSizeLabel(totalSize);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }

    private void updateSizeLabel(long size) {
        SwingUtilities.invokeLater(() -> sizeLabel.setText("当前索引大小：" + formatSize(size)));
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " Bytes";
        int exponent = (int) (Math.log(size) / Math.log(1024));
        String[] units = {"Bytes", "KB", "MB", "GB", "TB"};
        return String.format("%.2f %s", size / Math.pow(1024, exponent), units[exponent]);
    }
}
