/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.config;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.nio.file.*;
import java.util.Properties;

/**
 * Persists Swing UI preferences (window geometry, split-pane divider
 * positions, ...) to a small properties file beside the existing
 * {@code .jar-analyzer} project config so that the IDE-style state
 * survives across launches.
 * <p>
 * Why a separate file? {@link ConfigEngine} performs a full rewrite of
 * its Properties on every save and therefore drops any unknown keys.
 * Mixing UI keys in there would make them disappear the next time the
 * user touches a "project" setting. Keeping the two files orthogonal
 * also keeps responsibilities clean.
 */
public final class UIPrefs {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Lives next to {@link ConfigEngine#CONFIG_FILE_PATH}. Same relative
     * convention (process working directory).
     */
    public static final String FILE = ".jar-analyzer-ui";

    // ---- known keys -----------------------------------------------------
    public static final String K_FRAME_X = "main.frame.x";
    public static final String K_FRAME_Y = "main.frame.y";
    public static final String K_FRAME_W = "main.frame.w";
    public static final String K_FRAME_H = "main.frame.h";
    public static final String K_FRAME_STATE = "main.frame.state";
    public static final String K_SPLIT_ROOT = "main.split.root";
    public static final String K_SPLIT_TREE = "main.split.tree";
    public static final String K_SPLIT_CORE = "main.split.core";

    private static final long DEBOUNCE_MS = 800L;

    private static final Properties PROPS = new Properties();
    private static volatile boolean loaded = false;
    private static volatile boolean shutdownHookInstalled = false;
    // Coalesces bursts of resize / move / divider events so we hit the
    // disk at most once per ~1s of activity.
    private static volatile Timer flushTimer;

    private UIPrefs() {
    }

    // ---- I/O ------------------------------------------------------------

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        installShutdownHookOnce();
        try {
            Path p = Paths.get(FILE);
            if (!Files.exists(p)) {
                return;
            }
            byte[] data = Files.readAllBytes(p);
            PROPS.load(new ByteArrayInputStream(data));
        } catch (Exception ex) {
            logger.warn("load ui prefs failed: {}", ex.toString());
        }
    }

    private static void installShutdownHookOnce() {
        if (shutdownHookInstalled) {
            return;
        }
        shutdownHookInstalled = true;
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    save();
                } catch (Throwable ignored) {
                }
            }, "ui-prefs-shutdown"));
        } catch (Exception ignored) {
        }
    }

    /**
     * Synchronous save. Uses a sibling .part + atomic move so we never
     * leave a half-written prefs file behind on a crash.
     */
    public static synchronized void save() {
        try {
            Path p = Paths.get(FILE).toAbsolutePath().normalize();
            Path parent = p.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Path tmp = p.resolveSibling(p.getFileName() + ".part");
            try (java.io.OutputStream os = Files.newOutputStream(tmp,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                PROPS.store(os, "jar-analyzer UI preferences -- do not edit by hand");
            }
            try {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            logger.warn("save ui prefs failed: {}", ex.toString());
        }
    }

    /**
     * Schedules a save ~1s in the future, coalescing repeated calls.
     * Safe to invoke from any thread; the actual write happens on the EDT
     * to avoid racing concurrent property updates.
     */
    public static void scheduleSave() {
        Runnable arm = new Runnable() {
            @Override
            public void run() {
                if (flushTimer == null) {
                    flushTimer = new Timer((int) DEBOUNCE_MS, e -> save());
                    flushTimer.setRepeats(false);
                }
                flushTimer.restart();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            arm.run();
        } else {
            SwingUtilities.invokeLater(arm);
        }
    }

    // ---- typed accessors ------------------------------------------------

    public static Integer getInt(String key) {
        load();
        String v = PROPS.getProperty(key);
        if (v == null) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public static void setInt(String key, int value) {
        load();
        PROPS.setProperty(key, Integer.toString(value));
    }

    // ---- frame geometry helpers ----------------------------------------

    /**
     * Applies any saved geometry to the given frame BEFORE it is first
     * shown. Called from {@code MainForm.start()} after {@code pack()}.
     * <p>
     * Position is dropped if it would make the frame open outside every
     * known monitor's visible bounds (e.g. user removed a second screen
     * since the last session). Size has a small lower bound to guard
     * against absurdly tiny saved values.
     */
    public static void applyFrameGeometry(JFrame frame) {
        load();
        Integer w = getInt(K_FRAME_W);
        Integer h = getInt(K_FRAME_H);
        Integer x = getInt(K_FRAME_X);
        Integer y = getInt(K_FRAME_Y);
        Integer state = getInt(K_FRAME_STATE);

        if (w != null && h != null && w >= 400 && h >= 300) {
            frame.setSize(w, h);
        }
        if (x != null && y != null) {
            // 32x32 of frame must overlap a real screen, otherwise the
            // window can be rendered offscreen with no way to recover.
            Rectangle probe = new Rectangle(x, y, Math.max(32, w == null ? 200 : w),
                    Math.max(32, h == null ? 200 : h));
            if (intersectsAnyScreen(probe)) {
                frame.setLocation(x, y);
            }
        }
        if (state != null && (state & java.awt.Frame.MAXIMIZED_BOTH) != 0
                && Toolkit_isMaxSupported()) {
            frame.setExtendedState(frame.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        }
    }

    /**
     * Snapshots the frame's current geometry into the in-memory props.
     * Caller decides when to actually flush via {@link #save()} or
     * {@link #scheduleSave()}.
     */
    public static void captureFrameGeometry(JFrame frame) {
        if (frame == null) {
            return;
        }
        int state = frame.getExtendedState();
        setInt(K_FRAME_STATE, state);
        // Only persist x/y/w/h when the frame is in normal state, so a
        // maximized session does not poison the saved "natural" size.
        if ((state & java.awt.Frame.MAXIMIZED_BOTH) == 0) {
            Rectangle b = frame.getBounds();
            if (b != null && b.width > 0 && b.height > 0) {
                setInt(K_FRAME_X, b.x);
                setInt(K_FRAME_Y, b.y);
                setInt(K_FRAME_W, b.width);
                setInt(K_FRAME_H, b.height);
            }
        }
    }

    /**
     * Wires resize / move / state-change listeners on the frame so that
     * every interactive change schedules a debounced flush. The frame
     * listener is also a safety net when the JVM exits cleanly.
     */
    public static void installFrameListeners(JFrame frame) {
        ComponentAdapter ca = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                captureFrameGeometry(frame);
                scheduleSave();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                captureFrameGeometry(frame);
                scheduleSave();
            }
        };
        frame.addComponentListener(ca);
        frame.addPropertyChangeListener("extendedState",
                (PropertyChangeListener) evt -> {
                    captureFrameGeometry(frame);
                    scheduleSave();
                });
    }

    // ---- split-pane helpers --------------------------------------------

    /**
     * Restores a saved divider location for the given split pane. Must
     * run after the split's parent is laid out, otherwise Swing silently
     * clamps the value -- we therefore defer via an AncestorListener
     * that fires once the pane is added to a showing window.
     */
    public static void bindSplit(JSplitPane split, String key) {
        if (split == null || key == null) {
            return;
        }
        Integer saved = getInt(key);
        if (saved != null && saved > 0) {
            // Defer until the split has a non-zero size, otherwise the
            // value is clamped to the pane's current (zero) extent.
            split.addAncestorListener(new AncestorListener() {
                boolean applied = false;

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    if (applied) {
                        return;
                    }
                    applied = true;
                    SwingUtilities.invokeLater(() -> {
                        try {
                            split.setDividerLocation(saved);
                        } catch (Exception ignored) {
                        }
                    });
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }
        // Persist any future drag of the divider.
        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                evt -> {
                    int loc = split.getDividerLocation();
                    if (loc > 0) {
                        setInt(key, loc);
                        scheduleSave();
                    }
                });
    }

    // ---- internals ------------------------------------------------------

    private static boolean intersectsAnyScreen(Rectangle r) {
        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (GraphicsDevice gd : env.getScreenDevices()) {
                for (GraphicsConfiguration gc : gd.getConfigurations()) {
                    Rectangle b = gc.getBounds();
                    if (b != null && b.intersects(r)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean Toolkit_isMaxSupported() {
        try {
            return java.awt.Toolkit.getDefaultToolkit()
                    .isFrameStateSupported(java.awt.Frame.MAXIMIZED_BOTH);
        } catch (Exception e) {
            return false;
        }
    }
}
