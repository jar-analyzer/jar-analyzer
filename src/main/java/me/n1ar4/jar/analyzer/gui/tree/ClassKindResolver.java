/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.tree;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Determines the {@link ClassIconKind} of a {@code .class} file by
 * peeking at its access flags via ASM. Results are cached by absolute
 * path + (size, mtime) so the tree renderer can decide instantly on
 * subsequent paints; first-time lookups happen on a single background
 * thread to keep scrolling smooth.
 * <p>
 * Reading only access flags is constant-pool parsing; we never dispatch
 * a {@link org.objectweb.asm.ClassVisitor}, so there is no bytecode
 * "execution" path. Malformed class files map to
 * {@link ClassIconKind#UNKNOWN} and are NOT retried -- this keeps a
 * corrupt jar from spinning up an infinite reparse loop.
 */
public final class ClassKindResolver {
    private static final Logger logger = LogManager.getLogger();

    /**
     * ASM constant. Hard-coded so we keep compiling against ASM versions
     * that predate {@link Opcodes#ACC_RECORD} (added in ASM 7.2).
     */
    private static final int ACC_RECORD = 0x10000;

    /** Cap the cache so a huge tree doesn't pin unlimited memory. */
    private static final int MAX_CACHE = 50_000;

    /** Bound the input we read to defeat malicious / huge entries. */
    private static final int MAX_CLASS_BYTES = 8 * 1024 * 1024;

    private static final ConcurrentHashMap<String, Entry> CACHE = new ConcurrentHashMap<>();

    /**
     * Single-thread background executor. One thread is enough because
     * the work is I/O bound and we explicitly do NOT want to thrash the
     * disk with many concurrent reads while the user scrolls.
     */
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(new ThreadFactory() {
        private final AtomicInteger n = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "class-kind-resolver-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });

    private ClassKindResolver() {
    }

    /**
     * Returns a cached kind for {@code file}, or {@code null} if the
     * answer is not yet known. When {@code null}, the supplied
     * {@code onResolved} callback (if any) will be invoked on the EDT
     * once the background read completes.
     */
    public static ClassIconKind getCached(File file, Runnable onResolved) {
        if (file == null) {
            return null;
        }
        String key = file.getAbsolutePath();
        long size = file.length();
        long mtime = file.lastModified();
        Entry e = CACHE.get(key);
        if (e != null && !e.pending && e.size == size && e.mtime == mtime) {
            return e.kind;
        }
        scheduleResolve(file, key, size, mtime, onResolved);
        return null;
    }

    private static void scheduleResolve(File file, String key, long size, long mtime,
                                        Runnable onResolved) {
        Entry pending = new Entry(ClassIconKind.UNKNOWN, size, mtime, true);
        Entry prev = CACHE.putIfAbsent(key, pending);
        // Already being computed for the same (size,mtime) -- nothing
        // to do; the in-flight task will populate the cache and invoke
        // its callback. We deliberately don't chain callbacks here to
        // keep the contract simple: callers that miss are expected to
        // also receive a tree-wide repaint kick from the renderer.
        if (prev != null && prev.pending && prev.size == size && prev.mtime == mtime) {
            return;
        }
        if (prev != null && !prev.pending && prev.size == size && prev.mtime == mtime) {
            return;
        }
        if (prev != null) {
            // Stale entry, replace.
            CACHE.put(key, pending);
        }
        EXEC.submit(() -> {
            ClassIconKind kind;
            try {
                kind = resolveBlocking(file);
            } catch (Throwable t) {
                logger.debug("class kind resolve failed for {}: {}", key, t.toString());
                kind = ClassIconKind.UNKNOWN;
            }
            if (CACHE.size() > MAX_CACHE) {
                // Cheap LRU-ish eviction: drop a small batch so we never
                // pay for an actual LRU on the hot path.
                int drop = CACHE.size() - MAX_CACHE + 1024;
                int dropped = 0;
                for (String k : CACHE.keySet()) {
                    if (dropped++ >= drop) {
                        break;
                    }
                    CACHE.remove(k);
                }
            }
            CACHE.put(key, new Entry(kind, size, mtime, false));
            if (onResolved != null) {
                SwingUtilities.invokeLater(onResolved);
            }
        });
    }

    /**
     * Synchronous parse used by the background thread. Reads the class
     * bytes (capped at {@link #MAX_CLASS_BYTES}) and inspects access
     * flags + super name only. No visitor is dispatched.
     */
    private static ClassIconKind resolveBlocking(File file) throws IOException {
        if (!file.isFile()) {
            return ClassIconKind.UNKNOWN;
        }
        long size = file.length();
        if (size <= 0 || size > MAX_CLASS_BYTES) {
            return ClassIconKind.UNKNOWN;
        }
        byte[] bytes;
        try (InputStream in = Files.newInputStream(file.toPath())) {
            bytes = readAll(in, (int) size);
        }
        ClassReader cr;
        try {
            cr = new ClassReader(bytes);
        } catch (IllegalArgumentException ex) {
            // Not a valid class file (bad magic, truncated, etc).
            return ClassIconKind.UNKNOWN;
        }
        int access = cr.getAccess();
        // Order matters: annotation must beat interface, interface must
        // beat abstract (interfaces always carry ACC_ABSTRACT), enum
        // must beat plain class.
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            return ClassIconKind.ANNOTATION;
        }
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            return ClassIconKind.INTERFACE;
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            return ClassIconKind.ENUM;
        }
        if ((access & ACC_RECORD) != 0) {
            return ClassIconKind.RECORD;
        }
        // Direct-super based exception detection. Walking the inheritance
        // chain would require additional disk I/O per class; this catches
        // the common pattern of business classes that extend
        // RuntimeException / Exception directly.
        String sup = cr.getSuperName();
        if (sup != null) {
            switch (sup) {
                case "java/lang/Throwable":
                case "java/lang/Exception":
                case "java/lang/RuntimeException":
                case "java/lang/Error":
                    return ClassIconKind.EXCEPTION;
                default:
                    // fall through
            }
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            return ClassIconKind.ABSTRACT_CLASS;
        }
        return ClassIconKind.CLASS;
    }

    private static byte[] readAll(InputStream in, int expected) throws IOException {
        byte[] buf = new byte[expected];
        int off = 0;
        while (off < expected) {
            int n = in.read(buf, off, expected - off);
            if (n < 0) {
                if (off == expected) {
                    break;
                }
                byte[] trimmed = new byte[off];
                System.arraycopy(buf, 0, trimmed, 0, off);
                return trimmed;
            }
            off += n;
        }
        return buf;
    }

    /**
     * Drops every cached kind. Call this whenever the on-disk class
     * pool is invalidated (e.g. user clicked Clean / re-built).
     */
    public static void clear() {
        CACHE.clear();
    }

    private static final class Entry {
        final ClassIconKind kind;
        final long size;
        final long mtime;
        final boolean pending;

        Entry(ClassIconKind kind, long size, long mtime, boolean pending) {
            this.kind = kind;
            this.size = size;
            this.mtime = mtime;
            this.pending = pending;
        }
    }
}
