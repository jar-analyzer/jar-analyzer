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

package me.n1ar4.jar.analyzer.sca.utils;

import cn.hutool.core.lang.UUID;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SCAExtractor {
    public static File extractNestedJar(JarFile jarFile, JarEntry entry) throws IOException {
        if (jarFile == null) {
            throw new IllegalArgumentException("JarFile cannot be null");
        }
        if (entry == null) {
            throw new IllegalArgumentException("JarEntry cannot be null");
        }
        if (jarFile.getJarEntry(entry.getName()) == null) {
            throw new IOException("JarEntry does not exist in the JarFile");
        }
        Path finalDir = Paths.get(Const.tempDir).resolve("SCA");
        try {
            Files.createDirectories(finalDir);
        } catch (Exception ignored) {
        }
        File tempFile = Files.createFile(finalDir.resolve(
                String.format("%s.jar", UUID.randomUUID()))).toFile();
        try (InputStream jarInputStream = jarFile.getInputStream(entry);
             FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (jarInputStream == null) {
                throw new IOException("Could not get InputStream for JarEntry");
            }
            IOUtil.copy(jarInputStream, fos);
        }
        return tempFile;
    }
}
