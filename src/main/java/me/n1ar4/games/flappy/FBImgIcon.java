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

package me.n1ar4.games.flappy;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class FBImgIcon extends ImageIcon implements InDrawImage {
    private static final long serialVersionUID = 1L;
    protected int x, y;
    protected int width, height;
    protected FBMainFrame frame;

    public FBImgIcon(FBMainFrame frame, URL url, int x, int y, int width, int height) {
        this(frame, url);
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public FBImgIcon(FBMainFrame frame, String url) {
        super(url);
        this.frame = frame;
    }

    public FBImgIcon(FBMainFrame frame, URL url) {
        super(url);
        this.frame = frame;
    }

    @Override
    public void drawImage(Graphics g) {
        g.drawImage(getImage(), x, y, null);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
