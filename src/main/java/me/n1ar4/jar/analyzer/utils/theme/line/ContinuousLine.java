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

package me.n1ar4.jar.analyzer.utils.theme.line;

import me.n1ar4.jar.analyzer.utils.canvas.Box;
import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.Drawable;
import me.n1ar4.jar.analyzer.utils.canvas.TextDirection;

public class ContinuousLine implements Drawable {
    private final Canvas localCanvas = new Canvas();

    private TextDirection direction = TextDirection.RIGHT;

    public void setDirection(TextDirection direction) {
        this.direction = direction;
    }

    public ContinuousLine turn(TextDirection direction) {
        if (this.direction == TextDirection.UP && direction == TextDirection.LEFT) {
            localCanvas.drawPixel(Box.DOWN_AND_LEFT);
            localCanvas.left(1);
        } else if (this.direction == TextDirection.UP && direction == TextDirection.RIGHT) {
            localCanvas.drawPixel(Box.DOWN_AND_RIGHT);
            localCanvas.right(1);
        } else if (this.direction == TextDirection.RIGHT && direction == TextDirection.UP) {
            localCanvas.drawPixel(Box.UP_AND_LEFT);
            localCanvas.up(1);
        } else if (this.direction == TextDirection.RIGHT && direction == TextDirection.DOWN) {
            localCanvas.drawPixel(Box.DOWN_AND_LEFT);
            localCanvas.down(1);
        } else if (this.direction == TextDirection.DOWN && direction == TextDirection.RIGHT) {
            localCanvas.drawPixel(Box.UP_AND_RIGHT);
            localCanvas.right(1);
        } else if (this.direction == TextDirection.DOWN && direction == TextDirection.LEFT) {
            localCanvas.drawPixel(Box.UP_AND_LEFT);
            localCanvas.left(1);
        } else if (this.direction == TextDirection.LEFT && direction == TextDirection.DOWN) {
            localCanvas.drawPixel(Box.DOWN_AND_RIGHT);
            localCanvas.down(1);
        } else if (this.direction == TextDirection.LEFT && direction == TextDirection.UP) {
            localCanvas.drawPixel(Box.UP_AND_RIGHT);
            localCanvas.up(1);
        } else {
            assert false : "impossible here";
        }
        this.direction = direction;
        return this;
    }

    public ContinuousLine drawLine(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("'count' should be greater than zero: " + count);
        }

        switch (direction) {
            case UP:
                localCanvas.drawVerticalLine(-count);
                break;
            case RIGHT:
                localCanvas.drawHorizontalLine(count);
                break;
            case DOWN:
                localCanvas.drawVerticalLine(count);
                break;
            case LEFT:
                localCanvas.drawHorizontalLine(-count);
                break;
            default:
                assert false : "impossible here";
        }
        return this;
    }

    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        localCanvas.updatePosition(startRow, startCol);
        localCanvas.rectifyPosition();

        canvas.overlay(localCanvas);
    }
}
