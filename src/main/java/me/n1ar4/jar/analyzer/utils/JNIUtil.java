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

import me.n1ar4.jar.analyzer.starter.Const;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JNI Utils
 */
public class JNIUtil {
    private static final String lib = "java.library.path";

    /**
     * Make new JNI lib effective
     *
     * @return success or not
     */
    private static boolean deleteUrls() {
        try {
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Load JNI lib
     *
     * @param path dll/so path
     * @return success or not
     */
    public static boolean loadLib(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {

            return false;
        }
        if (Files.isDirectory(p)) {

            return false;
        }
        String os = System.getProperty("os.name").toLowerCase();
        String libDirAbsPath = Paths.get(p.toFile().getParent()).toAbsolutePath().toString();
        String originLib = System.getProperty(lib);
        if (os.contains("windows")) {
            originLib = originLib + String.format(";%s;", libDirAbsPath);
            System.setProperty(lib, originLib);
            if (!deleteUrls()) {
                return false;
            }
            String dll = p.toFile().getName().toLowerCase();
            if (!dll.endsWith(".dll")) {
                return false;
            }
            System.load(p.toFile().getAbsolutePath());
        } else {
            String so = p.toFile().getAbsolutePath();
            if (!so.endsWith(".so")) {
                return false;
            }
            System.load(so);
        }
        return true;
    }

    /**
     * Write dll/so file to temp directory and load it
     *
     * @param filename dll/so file name in resources
     */
    public static boolean extractDllSo(String filename, String dir, boolean load) {
        try (InputStream is = JNIUtil.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                return false;
            }
            if (dir == null || dir.isEmpty()) {
                dir = Const.tempDir;
            }
            Path targetDir = Paths.get(dir);
            Path outputFile;
            if (!Files.exists(targetDir)) {
                Path dirPath = Files.createDirectories(targetDir);
                outputFile = dirPath.resolve(filename);
            } else {
                outputFile = targetDir.resolve(filename);
            }
            if (!Files.exists(outputFile)) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                Files.write(outputFile, buffer.toByteArray());
            }
            if (load) {
                return loadLib(outputFile.toAbsolutePath().toString());
            }
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
