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
    private static AWTEventListener globalMouseListener = null;
    private static boolean globalKeyDispatcherInstalled = false;

    /**
     * 安装全局键盘事件分发器，专门处理双 Shift 触发全局搜索。
     * 不依赖焦点所在组件，在 MainForm 初始化时调用一次即可。
     */
    public static void installGlobalKeyDispatcher() {
        if (globalKeyDispatcherInstalled) return;
        globalKeyDispatcherInstalled = true;
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED
                            && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        handleShiftPress();
                    }
                    // 返回 false：不消费事件，让其继续正常传递
                    return false;
                });
    }

    private static void handleShiftPress() {
        shiftPressCount++;
        if (shiftPressCount == 2) {
            triggerGlobalSearch();
            shiftPressCount = 0;
            if (resetTimer != null) resetTimer.stop();
        } else {
            if (resetTimer == null || !resetTimer.isRunning()) {
                resetTimer = new Timer(500, ev -> shiftPressCount = 0);
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        }
    }

    private static void triggerGlobalSearch() {
        // 若窗口已显示，直接移到鼠标位置并置顶
        if (LuceneSearchForm.getInstanceFrame() != null
                && LuceneSearchForm.getInstanceFrame().isShowing()) {
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            LuceneSearchForm.getInstanceFrame().setLocation(mouseLocation.x + 10, mouseLocation.y + 10);
            LuceneSearchForm.getInstanceFrame().toFront();
            LuceneSearchForm.getInstanceFrame().requestFocus();
            return;
        }

        // 防止重复注册：先移除旧的监听器
        if (globalMouseListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
        }
        globalMouseListener = event -> {
            if (event instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent) event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                    JFrame frame = LuceneSearchForm.getInstanceFrame();
                    if (frame == null || !frame.isShowing()) {
                        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
                        globalMouseListener = null;
                        return;
                    }
                    // 忽略来自其他弹窗（ProcessDialog 等）的事件
                    Component source = mouseEvent.getComponent();
                    if (source != null) {
                        Window sourceWindow = SwingUtilities.getWindowAncestor(source);
                        if (sourceWindow != null && sourceWindow != frame) {
                            return;
                        }
                    }
                    if (!frame.getBounds().contains(mouseEvent.getLocationOnScreen())) {
                        LuceneSearchForm.closeInstanceFrame();
                        Toolkit.getDefaultToolkit().removeAWTEventListener(globalMouseListener);
                        globalMouseListener = null;
                    }
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(globalMouseListener, AWTEvent.MOUSE_EVENT_MASK);
        LuceneSearchWrapper.initEnv();
        LuceneSearchForm.start(0);
    }

    // Ctrl+X / Ctrl+F 由各组件自己注册的 KeyListener 处理
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
    }
}
