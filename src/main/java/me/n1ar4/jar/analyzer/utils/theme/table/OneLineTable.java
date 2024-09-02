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

import me.n1ar4.jar.analyzer.utils.canvas.Canvas;
import me.n1ar4.jar.analyzer.utils.canvas.Drawable;
import me.n1ar4.jar.analyzer.utils.canvas.TextAlign;

public class OneLineTable extends AbstractTable implements Drawable {
    public final String[][] matrix;
    public final TextAlign align;

    private final int row_padding;
    private final int col_padding;

    public OneLineTable(String[][] matrix, TextAlign align) {
        this(matrix, align, 0, 1);
    }

    public OneLineTable(String[][] matrix, TextAlign align, int row_padding, int col_padding) {
        this.matrix = matrix;
        this.align = align;
        this.row_padding = row_padding;
        this.col_padding = col_padding;
    }

    @Override
    protected int getCellLength(int row, int col) {
        String item = matrix[row][col];
        int length = item == null ? 0 : item.length();
        return length + 2 * col_padding;
    }

    @Override
    public void draw(Canvas canvas, int startRow, int startCol) {
        int rowCount = matrix.length;
        int colCount = matrix[0].length;

        int[] rowHeightArray = new int[rowCount];
        int[] colWidthArray = getColWidthArray(rowCount, colCount);

        for (int i = 0; i < rowCount; i++) {
            rowHeightArray[i] = 1 + 2 * row_padding;
        }


        canvas.moveTo(startRow, startCol);
        canvas.drawTable(rowHeightArray, colWidthArray);


        int currentRow = startRow;
        for (int i = 0; i < rowCount; i++) {
            if (i > 0) {
                currentRow += rowHeightArray[i - 1] + 1;
            }

            int currentCol = startCol;
            for (int j = 0; j < colCount; j++) {
                if (j > 0) {
                    currentCol += colWidthArray[j - 1] + 1;
                }

                String item = matrix[i][j];
                if (item == null) item = "";

                int currentWidth = colWidthArray[j];

                int row = currentRow + 1 + row_padding;
                int left = currentCol;
                int right = left + currentWidth + 1;

                switch (align) {
                    case LEFT: {
                        canvas.moveTo(row, left + col_padding + 1);
                        canvas.drawText(item);
                        break;
                    }
                    case CENTER: {
                        canvas.moveTo(row, left + (currentWidth - item.length()) / 2 + 1);
                        canvas.drawText(item);
                        break;
                    }
                    case RIGHT: {
                        canvas.moveTo(row, right - col_padding - item.length());
                        canvas.drawText(item);
                        break;
                    }
                    default:
                        assert false : "impossible";
                }
            }

        }
    }
}
