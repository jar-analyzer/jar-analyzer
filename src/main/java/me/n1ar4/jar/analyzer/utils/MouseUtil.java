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

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * 鼠标 / 修饰键 跨平台兼容工具
 * <p>
 * macOS 与 Windows / Linux 的差异：
 * 1. macOS 早期 Mac 没有右键，规范的右键弹出在 mousePressed 时触发；
 * Windows / Linux 在 mouseReleased 时触发。
 * 所以右键菜单逻辑应当同时在 mousePressed 和 mouseReleased 中检查 isPopupTrigger()。
 * 2. macOS 上"按住 Ctrl + 左键"会被系统视为 BUTTON3（右键），
 * 无法用 isControlDown() + 左键 实现 Ctrl+点击 跳转。
 * macOS 的"跳转"修饰键应使用 Command（Meta）键。
 * 3. macOS 的"菜单快捷键"也是 Command 而不是 Control。
 */
public final class MouseUtil {

    private MouseUtil() {
    }

    /**
     * 跨平台 popup trigger 判定。
     * <p>
     * 必须仅信任 {@link MouseEvent#isPopupTrigger()}：
     * <ul>
     *   <li>Windows / Linux：仅在 mouseReleased 时返回 true</li>
     *   <li>macOS：仅在 mousePressed 时返回 true</li>
     * </ul>
     * 同一次右键操作中只会有一个事件返回 true，因此把本方法
     * 同时挂在 mousePressed 和 mouseReleased 上是安全的，不会重复弹出菜单。
     * <p>
     * <b>不能</b>额外加上 {@code isRightMouseButton(e)} 这种兜底判断，否则
     * 在 Windows 上 mousePressed 和 mouseReleased 都会判 true，
     * 导致弹菜单触发两次出现明显闪烁。
     */
    public static boolean isPopupTrigger(MouseEvent e) {
        return e != null && e.isPopupTrigger();
    }

    /**
     * 是否按下了"菜单/跳转"修饰键（IDEA 风格的 Ctrl+Click 跳转）。
     * macOS 使用 Command（Meta）键，其它平台使用 Control。
     */
    public static boolean isMenuShortcutDown(MouseEvent e) {
        if (e == null) {
            return false;
        }
        if (OSUtil.isMac()) {
            return e.isMetaDown();
        }
        return e.isControlDown();
    }

    /**
     * 真正的"左键单击 + 跳转修饰键"（IDEA 风格）。
     * <p>
     * 关键点：在 macOS 上 Ctrl+左键 会被识别为 BUTTON3（右键），
     * 因此必须用 Cmd（Meta）作为修饰键，并且必须额外校验是真正的左键。
     */
    public static boolean isNavigateClick(MouseEvent e) {
        if (e == null) {
            return false;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return false;
        }
        return isMenuShortcutDown(e);
    }

    /**
     * 获取当前平台的"菜单快捷键"修饰位（用于 KeyStroke）。
     * macOS 返回 META_DOWN_MASK，其它返回 CTRL_DOWN_MASK。
     * 项目最低支持 JDK 8，因此通过反射兼容调用 JDK 9+ 的
     * Toolkit.getMenuShortcutKeyMaskEx()，调用失败时回退到平台默认。
     */
    public static int getMenuShortcutKeyMask() {
        try {
            java.lang.reflect.Method m =
                    Toolkit.class.getMethod("getMenuShortcutKeyMaskEx");
            Object v = m.invoke(Toolkit.getDefaultToolkit());
            if (v instanceof Integer) {
                return (Integer) v;
            }
        } catch (Throwable ignored) {
        }
        return OSUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }
}
