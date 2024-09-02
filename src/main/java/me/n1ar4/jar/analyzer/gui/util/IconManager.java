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

package me.n1ar4.jar.analyzer.gui.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class IconManager {
    public static ImageIcon showIcon;
    public static ImageIcon javaIcon;
    public static ImageIcon dbIcon;
    public static ImageIcon startIcon;
    public static ImageIcon startUpIcon;
    public static ImageIcon jarIcon;
    public static ImageIcon curIcon;
    public static ImageIcon auIcon;
    public static ImageIcon githubIcon;
    public static ImageIcon whiteIcon;
    public static ImageIcon nextIcon;
    public static ImageIcon prevIcon;

    static {
        try {
            showIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/logo.png"))));
            Image image = showIcon.getImage();
            Image resizedImage = image.getScaledInstance(90, 75, Image.SCALE_SMOOTH);
            showIcon = new ImageIcon(resizedImage);

            javaIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/java.png"))));

            dbIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/db.png"))));

            startIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/start.png"))));

            startUpIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/startup.png"))));

            jarIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/jar.png"))));

            curIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/cur.png"))));

            auIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/au.png"))));
            image = auIcon.getImage();
            resizedImage = image.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            auIcon = new ImageIcon(resizedImage);

            githubIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/github.png"))));

            whiteIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/list.png"))));

            prevIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/prev.png"))));

            nextIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(IconManager.class
                            .getClassLoader().getResourceAsStream("img/next.png"))));
        } catch (Exception ignored) {
        }
    }
}
