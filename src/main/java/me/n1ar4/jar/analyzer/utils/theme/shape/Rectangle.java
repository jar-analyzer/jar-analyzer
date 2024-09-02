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

package me.n1ar4.jar.analyzer.utils.theme.shape;

import me.n1ar4.jar.analyzer.utils.canvas.Box;
import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.Drawable;

public class Rectangle implements Drawable {
    public final int width;
    public final int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates"})
    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        int left = startCol;
        int right = left + width + 1;
        int top = startRow;
        int bottom = top + height + 1;


        canvas.moveTo(top, left);
        canvas.drawPixel(Box.DOWN_AND_RIGHT);
        canvas.moveTo(top, right);
        canvas.drawPixel(Box.DOWN_AND_LEFT);
        canvas.moveTo(bottom, left);
        canvas.drawPixel(Box.UP_AND_RIGHT);
        canvas.moveTo(bottom, right);
        canvas.drawPixel(Box.UP_AND_LEFT);


        canvas.moveTo(top, left + 1);
        canvas.drawHorizontalLine(width);
        canvas.moveTo(bottom, left + 1);
        canvas.drawHorizontalLine(width);
        canvas.moveTo(top + 1, left);
        canvas.drawVerticalLine(height);
        canvas.moveTo(top + 1, right);
        canvas.drawVerticalLine(height);
    }
}
