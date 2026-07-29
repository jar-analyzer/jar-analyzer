/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JarUtilPathTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsOnlyNormalizedPathsUnderConfiguredTempDir() throws Exception {
        Path target = JarUtil.safeExtractionPath(
                tempDir, "BOOT-INF/classes/demo/Test.class");

        Path root = tempDir.toAbsolutePath().normalize();
        assertEquals(root.resolve("BOOT-INF/classes/demo/Test.class"), target);
        assertTrue(target.startsWith(root));
        assertTrue(Files.isDirectory(target.getParent()));
    }

    @Test
    void rejectsParentTraversal() {
        assertRejected("../escape.xml");
        assertRejected("a/../../escape.class");
        assertRejected("a\\..\\..\\escape.jar");
    }

    @Test
    void rejectsAbsoluteAndPlatformSpecificPaths() {
        assertRejected("/tmp/escape.xml");
        assertRejected("\\Windows\\Temp\\escape.xml");
        assertRejected("C:\\Windows\\Temp\\escape.xml");
        assertRejected("C:/Windows/Temp/escape.xml");
        assertRejected("\\\\server\\share\\escape.xml");
        assertRejected("//server/share/escape.xml");
    }

    @Test
    void rejectsAbsoluteSiblingWithSameStringPrefix() {
        Path root = tempDir.toAbsolutePath().normalize();
        Path sibling = root.resolveSibling(
                root.getFileName().toString() + "-escape").resolve("pwn.xml");

        assertRejected(sibling.toString());
    }

    @Test
    void rejectsExistingSymbolicLinkTraversal() throws Exception {
        Path root = Files.createDirectory(tempDir.resolve("root"));
        Path outside = Files.createDirectory(tempDir.resolve("outside"));
        Path link = root.resolve("linked");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (IOException | UnsupportedOperationException
                 | SecurityException e) {
            return;
        }

        assertThrows(IOException.class,
                () -> JarUtil.safeExtractionPath(root, "linked/pwn.xml"));
    }

    private void assertRejected(String entryName) {
        assertThrows(IOException.class,
                () -> JarUtil.safeExtractionPath(tempDir, entryName));
    }
}
