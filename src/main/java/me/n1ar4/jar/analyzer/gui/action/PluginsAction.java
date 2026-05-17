/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.config.UIPrefs;
import me.n1ar4.jar.analyzer.el.ELForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.plugins.bcel.BcelForm;
import me.n1ar4.jar.analyzer.plugins.encoder.EncodeUtilForm;
import me.n1ar4.jar.analyzer.plugins.listener.ListenUtilForm;
import me.n1ar4.jar.analyzer.plugins.serutil.SerUtilForm;
import me.n1ar4.jar.analyzer.plugins.sqlite.SQLiteForm;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PluginsAction {

    // Persisted geometry for the SpEL workbench window. Lives next to
    // the main frame's keys in .jar-analyzer-ui so the user doesn't
    // have to re-size the window every time they open it.
    private static final String K_EL_FRAME_X = "el.frame.x";
    private static final String K_EL_FRAME_Y = "el.frame.y";
    private static final String K_EL_FRAME_W = "el.frame.w";
    private static final String K_EL_FRAME_H = "el.frame.h";

    // Reasonable defaults: large enough to show toolbar + template
    // tree + ~12 lines of editor + a few output lines without any
    // scrollbars on a 1080p screen.
    private static final int EL_DEFAULT_W = 1100;
    private static final int EL_DEFAULT_H = 720;
    private static final int EL_MIN_W = 640;
    private static final int EL_MIN_H = 420;

    public static void startELForm() {
        JFrame frame = new JFrame(Const.SPELSearch);
        frame.setContentPane(new ELForm().elPanel);

        // The EL workbench is an IDE-style multi-pane layout: it must
        // be resizable, otherwise the inner JSplitPanes have nothing to
        // play with and the toolbar / output console get clipped.
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(EL_MIN_W, EL_MIN_H));

        // Restore saved size, otherwise pick a comfortable default.
        Integer savedW = UIPrefs.getInt(K_EL_FRAME_W);
        Integer savedH = UIPrefs.getInt(K_EL_FRAME_H);
        int w = (savedW != null && savedW >= EL_MIN_W) ? savedW : EL_DEFAULT_W;
        int h = (savedH != null && savedH >= EL_MIN_H) ? savedH : EL_DEFAULT_H;
        frame.setSize(w, h);

        // Restore position when it still hits a real screen, otherwise
        // fall back to "centered relative to main window". This guards
        // against secondary monitors that have since been unplugged.
        Integer savedX = UIPrefs.getInt(K_EL_FRAME_X);
        Integer savedY = UIPrefs.getInt(K_EL_FRAME_Y);
        if (savedX != null && savedY != null
                && intersectsAnyScreen(new Rectangle(savedX, savedY, w, h))) {
            frame.setLocation(savedX, savedY);
        } else {
            frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        }

        // Persist subsequent resize / move events with the same
        // debounced flush UIPrefs uses for the main frame.
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                capture(frame);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                capture(frame);
            }
        });

        frame.setVisible(true);
    }

    private static void capture(JFrame frame) {
        if (frame == null) {
            return;
        }
        // Only persist when in normal (non-maximized) state so a one-off
        // maximize doesn't poison the natural size.
        if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
            return;
        }
        Rectangle b = frame.getBounds();
        if (b == null || b.width <= 0 || b.height <= 0) {
            return;
        }
        UIPrefs.setInt(K_EL_FRAME_X, b.x);
        UIPrefs.setInt(K_EL_FRAME_Y, b.y);
        UIPrefs.setInt(K_EL_FRAME_W, b.width);
        UIPrefs.setInt(K_EL_FRAME_H, b.height);
        UIPrefs.scheduleSave();
    }

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

    public static void run() {
        MainForm.getInstance().getSqliteButton().addActionListener(e -> SQLiteForm.start());

        MainForm.getInstance().getEncoderBtn().addActionListener(e -> EncodeUtilForm.start());

        MainForm.getInstance().getListenerBtn().addActionListener(e -> ListenUtilForm.start());

        MainForm.getInstance().getSpringELButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getStartELSearchButton().addActionListener(e -> startELForm());

        MainForm.getInstance().getSerUtilBtn().addActionListener(e -> SerUtilForm.start());

        MainForm.getInstance().getBcelBtn().addActionListener(e -> BcelForm.start());
    }
}

