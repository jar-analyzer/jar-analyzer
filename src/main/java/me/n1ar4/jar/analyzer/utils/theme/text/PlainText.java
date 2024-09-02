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

package me.n1ar4.jar.analyzer.utils.theme.text;

import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.Drawable;

import java.util.ArrayList;
import java.util.List;

public class PlainText implements Drawable {
    public final List<String> lines = new ArrayList<>();

    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            if (line == null) line = "";
            canvas.moveTo(startRow + i, startCol)
                    .drawText(line);
        }
    }

    public static PlainText valueOf(String line) {
        PlainText text = new PlainText();
        text.lines.add(line);
        return text;
    }

    public static PlainText valueOf(List<String> lines) {
        PlainText text = new PlainText();
        text.lines.addAll(lines);
        return text;
    }
}
