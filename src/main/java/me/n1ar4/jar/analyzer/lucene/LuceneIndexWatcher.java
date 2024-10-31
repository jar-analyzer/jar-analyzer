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
