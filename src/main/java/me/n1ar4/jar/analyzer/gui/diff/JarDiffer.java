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

import me.n1ar4.jar.analyzer.engine.CFRDecompileEngine;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class JarDiffer {
    private static final Logger logger = LogManager.getLogger();
    private static final String CLASS_EXT = ".class";

    private final Path leftJar;
    private final Path rightJar;
    private final Path workDir;

    private final List<JarDiffEntry> entries = new ArrayList<>();

    private final Map<String, byte[]> bytesCache = new LinkedHashMap<>();

    private ProgressListener progressListener;

    public JarDiffer(Path leftJar, Path rightJar, Path workDir) {
        this.leftJar = leftJar;
        this.rightJar = rightJar;
        this.workDir = workDir;
    }

    public List<JarDiffEntry> getEntries() {
        return entries;
    }

    public interface ProgressListener {
        void onTotal(int total);

        void onAdvance(int processed, String currentPath);
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    public void run() throws IOException {
        Files.createDirectories(workDir);

        try (ZipFile left = new ZipFile(leftJar.toFile());
             ZipFile right = new ZipFile(rightJar.toFile())) {

            Map<String, ZipArchiveEntry> leftMap = indexEntries(left);
            Map<String, ZipArchiveEntry> rightMap = indexEntries(right);

            TreeSet<String> all = new TreeSet<>();
            all.addAll(leftMap.keySet());
            all.addAll(rightMap.keySet());

            int total = all.size();
            if (progressListener != null) {
                progressListener.onTotal(total);
            }
            int processed = 0;

            for (String path : all) {
                ZipArchiveEntry le = leftMap.get(path);
                ZipArchiveEntry re = rightMap.get(path);
                JarDiffEntry.Kind kind = isClass(path)
                        ? JarDiffEntry.Kind.CLASS
                        : JarDiffEntry.Kind.RESOURCE;
                if (le == null) {
                    entries.add(new JarDiffEntry(path, kind, JarDiffEntry.Status.ADDED,
                            0L, re.getSize()));
                } else if (re == null) {
                    entries.add(new JarDiffEntry(path, kind, JarDiffEntry.Status.REMOVED,
                            le.getSize(), 0L));
                } else {
                    JarDiffEntry.Status status = compareEntry(left, right, le, re, kind, path);
                    entries.add(new JarDiffEntry(path, kind, status,
                            le.getSize(), re.getSize()));
                }
                processed++;
                if (progressListener != null) {
                    progressListener.onAdvance(processed, path);
                }
            }
        }

        entries.sort(Comparator
                .comparingInt(JarDiffer::priority)
                .thenComparing(JarDiffEntry::getEntryPath));
    }

    private static int priority(JarDiffEntry e) {
        JarDiffEntry.Status s = e.getStatus();
        boolean isClass = e.getKind() == JarDiffEntry.Kind.CLASS;
        if (s == JarDiffEntry.Status.EQUAL) {
            return 7;
        }
        if (s == JarDiffEntry.Status.EQUAL_BYTES_DIFFER) {
            return 6;
        }
        if (isClass) {
            switch (s) {
                case MODIFIED:
                    return 0;
                case ADDED:
                    return 1;
                case REMOVED:
                    return 2;
                default:
                    return 8;
            }
        } else {
            switch (s) {
                case MODIFIED:
                    return 3;
                case ADDED:
                    return 4;
                case REMOVED:
                    return 5;
                default:
                    return 8;
            }
        }
    }

    public String loadDisplayText(JarDiffEntry entry, char side) throws IOException {
        if (side != 'L' && side != 'R') {
            throw new IllegalArgumentException("side must be 'L' or 'R'");
        }
        Path jar = (side == 'L') ? leftJar : rightJar;
        boolean isLeft = (side == 'L');
        if ((isLeft && entry.getStatus() == JarDiffEntry.Status.ADDED)
                || (!isLeft && entry.getStatus() == JarDiffEntry.Status.REMOVED)) {
            return "// (entry not present on this side)\n";
        }
        byte[] bytes = readEntryBytes(jar, entry.getEntryPath());
        if (bytes == null) {
            return "// (failed to read entry)\n";
        }
        if (entry.getKind() == JarDiffEntry.Kind.CLASS) {
            Path classFile = extractToTemp(entry.getEntryPath(), bytes,
                    isLeft ? "left" : "right");
            String code = CFRDecompileEngine.decompile(classFile.toAbsolutePath().toString());
            if (code == null || code.isEmpty()) {
                return "// (CFR decompile failed)\n";
            }
            return stripDecompilerHeader(code);
        }
        return renderResource(bytes);
    }

    private static Map<String, ZipArchiveEntry> indexEntries(ZipFile file) {
        Map<String, ZipArchiveEntry> map = new LinkedHashMap<>();
        Enumeration<ZipArchiveEntry> entries = file.getEntries();
        while (entries.hasMoreElements()) {
            ZipArchiveEntry e = entries.nextElement();
            if (e.isDirectory()) {
                continue;
            }
            if (isModuleInfo(e.getName())) {
                continue;
            }
            map.put(e.getName(), e);
        }
        return map;
    }

    static boolean isModuleInfo(String entryPath) {
        if (entryPath == null) {
            return false;
        }
        return entryPath.endsWith("module-info.class");
    }

    private JarDiffEntry.Status compareEntry(ZipFile left, ZipFile right,
                                             ZipArchiveEntry le, ZipArchiveEntry re,
                                             JarDiffEntry.Kind kind, String path) throws IOException {
        // fast path 1: size differs -> definitely MODIFIED, skip byte read & decompile.
        // sizes come straight from the central directory of the ZIP, no I/O on entry payload.
        long ls = le.getSize();
        long rs = re.getSize();
        if (ls >= 0 && rs >= 0 && ls != rs) {
            return JarDiffEntry.Status.MODIFIED;
        }

        // sizes equal (or unknown): read bytes and compare.
        byte[] lb = readAll(left, le);
        byte[] rb = readAll(right, re);
        if (sameBytes(lb, rb)) {
            return JarDiffEntry.Status.EQUAL;
        }
        if (kind == JarDiffEntry.Kind.RESOURCE) {
            return JarDiffEntry.Status.MODIFIED;
        }
        // bytes differ but sizes match -> could just be timestamp / constant-pool noise.
        // decompile to confirm whether the source is actually different.
        try {
            Path leftFile = extractToTemp(path, lb, "left");
            Path rightFile = extractToTemp(path, rb, "right");
            String leftCode = CFRDecompileEngine.decompile(leftFile.toAbsolutePath().toString());
            String rightCode = CFRDecompileEngine.decompile(rightFile.toAbsolutePath().toString());
            if (leftCode == null || rightCode == null) {
                return JarDiffEntry.Status.MODIFIED;
            }
            String l = stripDecompilerHeader(leftCode);
            String r = stripDecompilerHeader(rightCode);
            if (l.equals(r)) {
                return JarDiffEntry.Status.EQUAL_BYTES_DIFFER;
            }
            return JarDiffEntry.Status.MODIFIED;
        } catch (Exception ex) {
            logger.warn("decompile compare failed for {}: {}", path, ex.toString());
            return JarDiffEntry.Status.MODIFIED;
        }
    }

    private byte[] readEntryBytes(Path jar, String entryPath) throws IOException {
        try (ZipFile z = new ZipFile(jar.toFile())) {
            ZipArchiveEntry e = z.getEntry(entryPath);
            if (e == null) {
                return null;
            }
            return readAll(z, e);
        }
    }

    private static byte[] readAll(ZipFile zf, ZipArchiveEntry entry) throws IOException {
        try (InputStream is = zf.getInputStream(entry)) {
            int size = (int) Math.min(entry.getSize(), Integer.MAX_VALUE);
            byte[] buf;
            if (size > 0) {
                buf = new byte[size];
                int read = 0;
                while (read < size) {
                    int n = is.read(buf, read, size - read);
                    if (n < 0) {
                        break;
                    }
                    read += n;
                }
                if (read != size) {
                    byte[] trimmed = new byte[read];
                    System.arraycopy(buf, 0, trimmed, 0, read);
                    return trimmed;
                }
                return buf;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) > 0) {
                baos.write(chunk, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private Path extractToTemp(String entryPath, byte[] bytes, String side) throws IOException {
        if (entryPath.contains("../") || entryPath.contains("..\\")) {
            throw new IOException("zip slip detected: " + entryPath);
        }
        Path base = workDir.resolve(side).toAbsolutePath().normalize();
        Files.createDirectories(base);
        Path target = base.resolve(entryPath).toAbsolutePath().normalize();
        if (!target.startsWith(base)) {
            throw new IOException("zip slip detected: " + entryPath);
        }
        Files.createDirectories(target.getParent());
        String cacheKey = side + ":" + entryPath;
        if (!Files.exists(target) || !sameBytes(bytesCache.get(cacheKey), bytes)) {
            Files.write(target, bytes);
            bytesCache.put(cacheKey, bytes);
        }
        return target;
    }

    private static boolean sameBytes(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length != b.length) {
            return false;
        }
        if (a.length > 64 * 1024) {
            return sha256(a).equals(sha256(b));
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(data);
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isClass(String entryPath) {
        return entryPath.toLowerCase().endsWith(CLASS_EXT);
    }

    private static String stripDecompilerHeader(String code) {
        if (code == null) {
            return "";
        }
        String s = code.replace("\r\n", "\n");
        if (s.startsWith("//\n")) {
            int idx = 0;
            int line = 0;
            while (line < 4 && idx < s.length()) {
                int nl = s.indexOf('\n', idx);
                if (nl < 0) {
                    break;
                }
                idx = nl + 1;
                line++;
            }
            if (idx > 0 && idx <= s.length()) {
                return s.substring(idx);
            }
        }
        return s;
    }

    private static String renderResource(byte[] bytes) {
        if (bytes.length == 0) {
            return "// (empty)\n";
        }
        int printable = 0;
        int limit = Math.min(bytes.length, 4096);
        for (int i = 0; i < limit; i++) {
            int v = bytes[i] & 0xFF;
            if (v == 9 || v == 10 || v == 13 || (v >= 32 && v < 127) || v >= 0x80) {
                printable++;
            }
        }
        if (printable * 100 / Math.max(1, limit) >= 95) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        StringBuilder sb = new StringBuilder(bytes.length * 4);
        sb.append("// binary content - hex preview (first 4 KiB)\n");
        for (int i = 0; i < limit; i += 16) {
            sb.append(String.format("%08x  ", i));
            int len = Math.min(16, limit - i);
            for (int j = 0; j < 16; j++) {
                if (j < len) {
                    sb.append(String.format("%02x ", bytes[i + j]));
                } else {
                    sb.append("   ");
                }
            }
            sb.append(' ');
            for (int j = 0; j < len; j++) {
                int v = bytes[i + j] & 0xFF;
                sb.append((v >= 32 && v < 127) ? (char) v : '.');
            }
            sb.append('\n');
        }
        if (bytes.length > limit) {
            sb.append("... (").append(bytes.length - limit).append(" more bytes)\n");
        }
        return sb.toString();
    }
}
