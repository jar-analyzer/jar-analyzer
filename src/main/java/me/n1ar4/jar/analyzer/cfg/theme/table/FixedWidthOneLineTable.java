package me.n1ar4.jar.analyzer.cfg.theme.table;

import me.n1ar4.jar.analyzer.cfg.canvas.Drawable;
import me.n1ar4.jar.analyzer.cfg.canvas.TextAlign;

public class FixedWidthOneLineTable extends OneLineTable implements Drawable {
    public final int fixedWidth;

    public FixedWidthOneLineTable(String[][] matrix, TextAlign align, int fixedWidth) {
        super(matrix, align);
        this.fixedWidth = fixedWidth;
    }

    @Override
    protected int getCellLength(int row, int col) {
        return fixedWidth;
    }
}
