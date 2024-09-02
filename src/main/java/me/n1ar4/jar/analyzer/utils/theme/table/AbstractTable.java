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

package me.n1ar4.jar.analyzer.utils.theme.table;

import me.n1ar4.jar.analyzer.utils.canvas.Drawable;

public abstract class AbstractTable implements Drawable {
    public final int cell_inner_padding;

    public AbstractTable() {
        this(1);
    }

    public AbstractTable(int cell_inner_padding) {
        this.cell_inner_padding = cell_inner_padding;
    }

    protected abstract int getCellLength(int row, int col);

    public int[] getColWidthArray(int rowCount, int colCount) {
        int[] colWidthArray = new int[colCount];
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                int length = getCellLength(row, col) + 2 * cell_inner_padding;
                if (length > colWidthArray[col]) {
                    colWidthArray[col] = length;
                }
            }
        }
        return colWidthArray;
    }
}
