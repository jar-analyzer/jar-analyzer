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

package me.n1ar4.jar.analyzer.gui.font;

import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.awt.*;
import java.io.InputStream;

public class FontHelper {
    private static final Logger logger = LogManager.getLogger();

    public static void installFont() {
        try {
            InputStream is = FontHelper.class.getClassLoader().getResourceAsStream("consolas.ttf");
            if (is == null) {
                throw new RuntimeException("unknown error");
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            logger.error("install font error: {}", e.toString());
        }
    }

    public static Font getFont() {
        try {
            InputStream is = FontHelper.class.getClassLoader().getResourceAsStream("consolas.ttf");
            if (is == null) {
                throw new RuntimeException("unknown error");
            }
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            if (OSUtil.isLinux()) {
                return customFont.deriveFont(16f);
            } else {
                return customFont.deriveFont(12f);
            }
        } catch (Exception e) {
            logger.error("install font error: {}", e.toString());
        }
        return null;
    }
}
