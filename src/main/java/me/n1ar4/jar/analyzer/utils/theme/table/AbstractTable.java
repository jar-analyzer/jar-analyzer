/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
