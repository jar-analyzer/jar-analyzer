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

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class DirUtil {
    private static final Logger logger = LogManager.getLogger();
    private static final List<String> filenames = new ArrayList<>();

    public static List<String> GetFiles(String path) {
        filenames.clear();
        return getFiles(path);
    }

    private static List<String> getFiles(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return filenames;
            }
            for (File value : files) {
                if (value.isDirectory()) {
                    getFiles(value.getPath());
                } else {
                    filenames.add(value.getAbsolutePath());
                }
            }
        } else {
            filenames.add(file.getAbsolutePath());
        }
        return filenames;
    }

    public static boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = removeDir(new File(dir, child));
                    if (!success) {
                        logger.debug("remove dir {} not success", dir.toString());
                        // 由于 DLL 文件不能删除
                        // 这里应该继续删除不能返回
                    }
                }
            }
        }
        if (!dir.delete()) {
            logger.debug("remove dir {} not success", dir.toString());
            return false;
        } else {
            return true;
        }
    }
}
