/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.SearchForm;
import me.n1ar4.jar.analyzer.lucene.LuceneSearchWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class GlobalKeyListener extends KeyAdapter {
    private static int shiftPressCount = 0;
    private static Timer resetTimer;

    private static void triggerGlobalSearch() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                        if (LuceneSearchForm.getInstanceFrame() != null &&
                                LuceneSearchForm.getInstanceFrame().isShowing() &&
                                !LuceneSearchForm.getInstanceFrame().getBounds().contains(
                                        mouseEvent.getLocationOnScreen())) {
                            LuceneSearchForm.getInstanceFrame().dispose();
                            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                        }
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        LuceneSearchWrapper.initEnv();
        LuceneSearchForm.start(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 ||
                (e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0) {
            if (e.getKeyCode() == KeyEvent.VK_X) {
                if (MainForm.getCurMethod() == null) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "<html>ctrl+x<br>" +
                                    "<b>you should select a method first</b></html>");
                    return;
                }
                MainForm.getInstance().getTabbedPanel().setSelectedIndex(2);
            }
        }
        if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 ||
                (e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0) {
            if (e.getKeyCode() == KeyEvent.VK_F) {
                SearchForm.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftPressCount++;
            if (shiftPressCount == 2) {
                triggerGlobalSearch();
                shiftPressCount = 0;
                if (resetTimer != null) {
                    resetTimer.stop();
                }
            } else {
                if (resetTimer == null || !resetTimer.isRunning()) {
                    resetTimer = new Timer(500, event -> shiftPressCount = 0);
                    resetTimer.setRepeats(false);
                    resetTimer.start();
                }
            }
        }
    }
}
