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

package me.n1ar4.jar.analyzer.gui.adapter;

import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.SearchForm;

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
        LuceneSearchForm.start();
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

            // 如果检测到两次 Shift 按键，则触发搜索
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
