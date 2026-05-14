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

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class DiffJob {
    private static final Logger logger = LogManager.getLogger();

    private final Path leftRoot;
    private final Path rightRoot;
    private final Path workDir;
    private final boolean directoryMode;

    private final List<JarDiffEntry> entries = new ArrayList<>();
    private final Map<String, JarDiffer> differs = new LinkedHashMap<>();
    private final Map<String, Boolean> orphanLeft = new LinkedHashMap<>();
    private final Map<String, Boolean> orphanRight = new LinkedHashMap<>();

    private JarDiffer.ProgressListener progressListener;

    public DiffJob(Path leftRoot, Path rightRoot, Path workDir) {
        this.leftRoot = leftRoot;
        this.rightRoot = rightRoot;
        this.workDir = workDir;
        this.directoryMode = Files.isDirectory(leftRoot) && Files.isDirectory(rightRoot);
    }

    public boolean isDirectoryMode() {
        return directoryMode;
    }

    public List<JarDiffEntry> getEntries() {
        return entries;
    }

    public void setProgressListener(JarDiffer.ProgressListener listener) {
        this.progressListener = listener;
    }

    public void run() throws IOException {
        Files.createDirectories(workDir);
        if (!directoryMode) {
            JarDiffer d = new JarDiffer(leftRoot, rightRoot, workDir);
            if (progressListener != null) {
                d.setProgressListener(progressListener);
            }
            d.run();
            entries.addAll(d.getEntries());
            differs.put("", d);
            return;
        }

        Map<String, Path> leftJars = collectJars(leftRoot);
        Map<String, Path> rightJars = collectJars(rightRoot);
        TreeSet<String> all = new TreeSet<>();
        all.addAll(leftJars.keySet());
        all.addAll(rightJars.keySet());

        int totalEntries = 0;
        for (String rel : all) {
            Path l = leftJars.get(rel);
            Path r = rightJars.get(rel);
            if (l == null) {
                totalEntries += countJarEntries(r) + 1;
                orphanRight.put(rel, true);
            } else if (r == null) {
                totalEntries += countJarEntries(l) + 1;
                orphanLeft.put(rel, true);
            } else {
                totalEntries += unionJarEntries(l, r);
            }
        }
        if (progressListener != null) {
            progressListener.onTotal(totalEntries);
        }

        final int[] processed = {0};
        for (String rel : all) {
            Path l = leftJars.get(rel);
            Path r = rightJars.get(rel);
            if (l == null) {
                addOrphanJar(rel, r, JarDiffEntry.Status.ADDED);
                processed[0] += 1;
                advance(processed[0], rel);
                continue;
            }
            if (r == null) {
                addOrphanJar(rel, l, JarDiffEntry.Status.REMOVED);
                processed[0] += 1;
                advance(processed[0], rel);
                continue;
            }
            Path subWork = workDir.resolve(safe(rel));
            Files.createDirectories(subWork);
            JarDiffer pair = new JarDiffer(l, r, subWork);
            pair.setProgressListener(new JarDiffer.ProgressListener() {
                int last = 0;

                @Override
                public void onTotal(int t) {
                }

                @Override
                public void onAdvance(int p, String currentPath) {
                    int delta = p - last;
                    last = p;
                    processed[0] += delta;
                    advance(processed[0], rel + "!" + currentPath);
                }
            });
            pair.run();
            for (JarDiffEntry e : pair.getEntries()) {
                entries.add(new JarDiffEntry(
                        e.getEntryPath(), e.getKind(), e.getStatus(),
                        e.getLeftSize(), e.getRightSize(), rel));
            }
            differs.put(rel, pair);
        }

        entries.sort(Comparator
                .comparingInt(DiffJob::priority)
                .thenComparing(JarDiffEntry::getDisplayPath));
    }

    public String loadDisplayText(JarDiffEntry entry, char side) throws IOException {
        String key = entry.getJarRelative() == null ? "" : entry.getJarRelative();
        if (orphanLeft.containsKey(key) && side == 'R') {
            return "// (whole jar missing on RIGHT)\n";
        }
        if (orphanRight.containsKey(key) && side == 'L') {
            return "// (whole jar missing on LEFT)\n";
        }
        if (orphanLeft.containsKey(key) || orphanRight.containsKey(key)) {
            return "// orphan jar: " + key + "\n";
        }
        JarDiffer d = differs.get(key);
        if (d == null) {
            return "// (no differ available for " + key + ")\n";
        }
        return d.loadDisplayText(entry, side);
    }

    private void advance(int n, String path) {
        if (progressListener != null) {
            progressListener.onAdvance(n, path);
        }
    }

    private void addOrphanJar(String rel, Path jar, JarDiffEntry.Status status) {
        long size = 0L;
        try {
            size = Files.size(jar);
        } catch (Exception ignored) {
        }
        long l = (status == JarDiffEntry.Status.REMOVED) ? size : 0L;
        long r = (status == JarDiffEntry.Status.ADDED) ? size : 0L;
        entries.add(new JarDiffEntry(
                "<entire jar>", JarDiffEntry.Kind.RESOURCE, status, l, r, rel));
    }

    private static String safe(String relative) {
        return relative.replace('/', '_').replace('\\', '_').replace(':', '_');
    }

    private static int countJarEntries(Path jar) {
        try (ZipFile zf = new ZipFile(jar.toFile())) {
            int count = 0;
            Enumeration<ZipArchiveEntry> es = zf.getEntries();
            while (es.hasMoreElements()) {
                ZipArchiveEntry e = es.nextElement();
                if (e.isDirectory() || JarDiffer.isModuleInfo(e.getName())) {
                    continue;
                }
                count++;
            }
            return count;
        } catch (Exception e) {
            logger.warn("count jar entries failed for {}: {}", jar, e.toString());
            return 0;
        }
    }

    private static int unionJarEntries(Path left, Path right) {
        TreeSet<String> set = new TreeSet<>();
        for (Path p : new Path[]{left, right}) {
            try (ZipFile zf = new ZipFile(p.toFile())) {
                Enumeration<ZipArchiveEntry> es = zf.getEntries();
                while (es.hasMoreElements()) {
                    ZipArchiveEntry e = es.nextElement();
                    if (e.isDirectory() || JarDiffer.isModuleInfo(e.getName())) {
                        continue;
                    }
                    set.add(e.getName());
                }
            } catch (Exception e) {
                logger.warn("count union for {}: {}", p, e.toString());
            }
        }
        return set.size();
    }

    private static Map<String, Path> collectJars(Path root) throws IOException {
        Map<String, Path> map = new LinkedHashMap<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String name = file.getFileName().toString().toLowerCase();
                if (name.endsWith(".jar") || name.endsWith(".war")) {
                    String rel = root.relativize(file).toString().replace('\\', '/');
                    map.put(rel, file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return map;
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
}
