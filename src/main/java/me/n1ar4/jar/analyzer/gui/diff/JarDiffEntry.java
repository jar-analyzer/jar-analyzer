/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.diff;

public class JarDiffEntry {

    public enum Kind {
        CLASS,
        RESOURCE
    }

    public enum Status {
        REMOVED,
        ADDED,
        MODIFIED,
        EQUAL_BYTES_DIFFER,
        EQUAL
    }

    private final String entryPath;
    private final Kind kind;
    private final Status status;
    private final long leftSize;
    private final long rightSize;
    private final String jarRelative;

    public JarDiffEntry(String entryPath, Kind kind, Status status,
                        long leftSize, long rightSize) {
        this(entryPath, kind, status, leftSize, rightSize, null);
    }

    public JarDiffEntry(String entryPath, Kind kind, Status status,
                        long leftSize, long rightSize, String jarRelative) {
        this.entryPath = entryPath;
        this.kind = kind;
        this.status = status;
        this.leftSize = leftSize;
        this.rightSize = rightSize;
        this.jarRelative = jarRelative;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public Kind getKind() {
        return kind;
    }

    public Status getStatus() {
        return status;
    }

    public long getLeftSize() {
        return leftSize;
    }

    public long getRightSize() {
        return rightSize;
    }

    public String getJarRelative() {
        return jarRelative;
    }

    public String getDisplayPath() {
        if (jarRelative == null || jarRelative.isEmpty()) {
            return entryPath;
        }
        return jarRelative + "!" + entryPath;
    }

    public String getSizeDelta() {
        if (status == Status.ADDED) {
            return "+" + rightSize;
        }
        if (status == Status.REMOVED) {
            return "-" + leftSize;
        }
        long delta = rightSize - leftSize;
        if (delta > 0) {
            return "+" + delta;
        }
        return Long.toString(delta);
    }
}
