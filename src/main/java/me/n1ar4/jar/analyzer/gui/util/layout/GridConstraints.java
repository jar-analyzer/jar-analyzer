/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import java.awt.*;

public final class GridConstraints
        implements Cloneable {
    public static final GridConstraints[] EMPTY_ARRAY = new GridConstraints[0];
    public static final int FILL_NONE = 0;
    public static final int FILL_HORIZONTAL = 1;
    public static final int FILL_VERTICAL = 2;
    public static final int FILL_BOTH = 3;
    public static final int ANCHOR_CENTER = 0;
    public static final int ANCHOR_NORTH = 1;
    public static final int ANCHOR_SOUTH = 2;
    public static final int ANCHOR_EAST = 4;
    public static final int ANCHOR_WEST = 8;
    public static final int ANCHOR_NORTHEAST = 5;
    public static final int ANCHOR_SOUTHEAST = 6;
    public static final int ANCHOR_SOUTHWEST = 10;
    public static final int ANCHOR_NORTHWEST = 9;
    public static final int SIZEPOLICY_FIXED = 0;
    public static final int SIZEPOLICY_CAN_SHRINK = 1;
    public static final int SIZEPOLICY_CAN_GROW = 2;
    public static final int SIZEPOLICY_WANT_GROW = 4;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int ALIGN_FILL = 3;
    private int myRow;
    private int myColumn;
    private int myRowSpan;
    private int myColSpan;
    private int myVSizePolicy;
    private int myHSizePolicy;
    private int myFill;
    private int myAnchor;
    public final Dimension myMinimumSize;
    public final Dimension myPreferredSize;
    public final Dimension myMaximumSize;
    private int myIndent;
    private boolean myUseParentLayout;

    public GridConstraints() {
        this.myRowSpan = 1;
        this.myColSpan = 1;
        this.myVSizePolicy = 3;
        this.myHSizePolicy = 3;
        this.myFill = 0;
        this.myAnchor = 0;
        this.myMinimumSize = new Dimension(-1, -1);
        this.myPreferredSize = new Dimension(-1, -1);
        this.myMaximumSize = new Dimension(-1, -1);
        this.myIndent = 0;
    }

    public GridConstraints(int row, int column, int rowSpan, int colSpan, int anchor, int fill, int HSizePolicy, int VSizePolicy, Dimension minimumSize, Dimension preferredSize, Dimension maximumSize) {
        this.myRow = row;
        this.myColumn = column;
        this.myRowSpan = rowSpan;
        this.myColSpan = colSpan;
        this.myAnchor = anchor;
        this.myFill = fill;
        this.myHSizePolicy = HSizePolicy;
        this.myVSizePolicy = VSizePolicy;
        this.myMinimumSize = minimumSize != null ? new Dimension(minimumSize) : new Dimension(-1, -1);
        this.myPreferredSize = preferredSize != null ? new Dimension(preferredSize) : new Dimension(-1, -1);
        this.myMaximumSize = maximumSize != null ? new Dimension(maximumSize) : new Dimension(-1, -1);
        this.myIndent = 0;
    }

    public GridConstraints(int row, int column, int rowSpan, int colSpan, int anchor, int fill, int HSizePolicy, int VSizePolicy, Dimension minimumSize, Dimension preferredSize, Dimension maximumSize, int indent) {
        this(row, column, rowSpan, colSpan, anchor, fill, HSizePolicy, VSizePolicy, minimumSize, preferredSize, maximumSize);
        this.myIndent = indent;
    }

    public GridConstraints(int row, int column, int rowSpan, int colSpan, int anchor, int fill, int HSizePolicy, int VSizePolicy, Dimension minimumSize, Dimension preferredSize, Dimension maximumSize, int indent, boolean useParentLayout) {
        this(row, column, rowSpan, colSpan, anchor, fill, HSizePolicy, VSizePolicy, minimumSize, preferredSize, maximumSize, indent);
        this.myUseParentLayout = useParentLayout;
    }

    public Object clone() {
        return new GridConstraints(this.getRow(), this.getColumn(), this.getRowSpan(), this.getColSpan(), this.getAnchor(), this.getFill(), this.getHSizePolicy(), this.getVSizePolicy(), new Dimension(this.myMinimumSize), new Dimension(this.myPreferredSize), new Dimension(this.myMaximumSize), this.getIndent(), this.isUseParentLayout());
    }

    public int getColumn() {
        return this.myColumn;
    }

    public void setColumn(int column) {
        if (column < 0) {
            throw new IllegalArgumentException("wrong column: " + column);
        }
        this.myColumn = column;
    }

    public int getRow() {
        return this.myRow;
    }

    public void setRow(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("wrong row: " + row);
        }
        this.myRow = row;
    }

    public int getRowSpan() {
        return this.myRowSpan;
    }

    public void setRowSpan(int rowSpan) {
        if (rowSpan <= 0) {
            throw new IllegalArgumentException("wrong rowSpan: " + rowSpan);
        }
        this.myRowSpan = rowSpan;
    }

    public int getColSpan() {
        return this.myColSpan;
    }

    public void setColSpan(int colSpan) {
        if (colSpan <= 0) {
            throw new IllegalArgumentException("wrong colSpan: " + colSpan);
        }
        this.myColSpan = colSpan;
    }

    public int getHSizePolicy() {
        return this.myHSizePolicy;
    }

    public void setHSizePolicy(int sizePolicy) {
        if (sizePolicy < 0 || sizePolicy > 7) {
            throw new IllegalArgumentException("invalid sizePolicy: " + sizePolicy);
        }
        this.myHSizePolicy = sizePolicy;
    }

    public int getVSizePolicy() {
        return this.myVSizePolicy;
    }

    public void setVSizePolicy(int sizePolicy) {
        if (sizePolicy < 0 || sizePolicy > 7) {
            throw new IllegalArgumentException("invalid sizePolicy: " + sizePolicy);
        }
        this.myVSizePolicy = sizePolicy;
    }

    public int getAnchor() {
        return this.myAnchor;
    }

    public void setAnchor(int anchor) {
        if (anchor < 0 || anchor > 15) {
            throw new IllegalArgumentException("invalid anchor: " + anchor);
        }
        this.myAnchor = anchor;
    }

    public int getFill() {
        return this.myFill;
    }

    public void setFill(int fill) {
        if (fill != 0 && fill != 1 && fill != 2 && fill != 3) {
            throw new IllegalArgumentException("invalid fill: " + fill);
        }
        this.myFill = fill;
    }

    public int getIndent() {
        return this.myIndent;
    }

    public void setIndent(int indent) {
        this.myIndent = indent;
    }

    public boolean isUseParentLayout() {
        return this.myUseParentLayout;
    }

    public void setUseParentLayout(boolean useParentLayout) {
        this.myUseParentLayout = useParentLayout;
    }

    public GridConstraints store() {
        GridConstraints copy = new GridConstraints();
        copy.setRow(this.myRow);
        copy.setColumn(this.myColumn);
        copy.setColSpan(this.myColSpan);
        copy.setRowSpan(this.myRowSpan);
        copy.setVSizePolicy(this.myVSizePolicy);
        copy.setHSizePolicy(this.myHSizePolicy);
        copy.setFill(this.myFill);
        copy.setAnchor(this.myAnchor);
        copy.setIndent(this.myIndent);
        copy.setUseParentLayout(this.myUseParentLayout);
        copy.myMinimumSize.setSize(this.myMinimumSize);
        copy.myPreferredSize.setSize(this.myPreferredSize);
        copy.myMaximumSize.setSize(this.myMaximumSize);
        return copy;
    }

    public void restore(GridConstraints constraints) {
        this.myRow = constraints.myRow;
        this.myColumn = constraints.myColumn;
        this.myRowSpan = constraints.myRowSpan;
        this.myColSpan = constraints.myColSpan;
        this.myHSizePolicy = constraints.myHSizePolicy;
        this.myVSizePolicy = constraints.myVSizePolicy;
        this.myFill = constraints.myFill;
        this.myAnchor = constraints.myAnchor;
        this.myIndent = constraints.myIndent;
        this.myUseParentLayout = constraints.myUseParentLayout;
        this.myMinimumSize.setSize(constraints.myMinimumSize);
        this.myPreferredSize.setSize(constraints.myPreferredSize);
        this.myMaximumSize.setSize(constraints.myMaximumSize);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GridConstraints)) {
            return false;
        }
        GridConstraints gridConstraints = (GridConstraints) o;
        if (this.myAnchor != gridConstraints.myAnchor) {
            return false;
        }
        if (this.myColSpan != gridConstraints.myColSpan) {
            return false;
        }
        if (this.myColumn != gridConstraints.myColumn) {
            return false;
        }
        if (this.myFill != gridConstraints.myFill) {
            return false;
        }
        if (this.myHSizePolicy != gridConstraints.myHSizePolicy) {
            return false;
        }
        if (this.myRow != gridConstraints.myRow) {
            return false;
        }
        if (this.myRowSpan != gridConstraints.myRowSpan) {
            return false;
        }
        if (this.myVSizePolicy != gridConstraints.myVSizePolicy) {
            return false;
        }
        boolean bl = this.myMaximumSize != null ? !this.myMaximumSize.equals(gridConstraints.myMaximumSize) : gridConstraints.myMaximumSize != null;
        if (bl) {
            return false;
        }
        boolean bl2 = this.myMinimumSize != null ? !this.myMinimumSize.equals(gridConstraints.myMinimumSize) : gridConstraints.myMinimumSize != null;
        if (bl2) {
            return false;
        }
        boolean bl3 = this.myPreferredSize != null ? !this.myPreferredSize.equals(gridConstraints.myPreferredSize) : gridConstraints.myPreferredSize != null;
        if (bl3) {
            return false;
        }
        if (this.myIndent != gridConstraints.myIndent) {
            return false;
        }
        return this.myUseParentLayout == gridConstraints.myUseParentLayout;
    }

    public int hashCode() {
        int result = this.myRow;
        result = 29 * result + this.myColumn;
        result = 29 * result + this.myRowSpan;
        result = 29 * result + this.myColSpan;
        result = 29 * result + this.myVSizePolicy;
        result = 29 * result + this.myHSizePolicy;
        result = 29 * result + this.myFill;
        result = 29 * result + this.myAnchor;
        result = 29 * result + (this.myMinimumSize != null ? this.myMinimumSize.hashCode() : 0);
        result = 29 * result + (this.myPreferredSize != null ? this.myPreferredSize.hashCode() : 0);
        result = 29 * result + (this.myMaximumSize != null ? this.myMaximumSize.hashCode() : 0);
        result = 29 * result + this.myIndent;
        result = 29 * result + (this.myUseParentLayout ? 1 : 0);
        return result;
    }

    public int getCell(boolean isRow) {
        return isRow ? this.getRow() : this.getColumn();
    }

    public void setCell(boolean isRow, int value) {
        if (isRow) {
            this.setRow(value);
        } else {
            this.setColumn(value);
        }
    }

    public int getSpan(boolean isRow) {
        return isRow ? this.getRowSpan() : this.getColSpan();
    }

    public void setSpan(boolean isRow, int value) {
        if (isRow) {
            this.setRowSpan(value);
        } else {
            this.setColSpan(value);
        }
    }

    public boolean contains(boolean isRow, int cell) {
        if (isRow) {
            return cell >= this.myRow && cell < this.myRow + this.myRowSpan;
        }
        return cell >= this.myColumn && cell < this.myColumn + this.myColSpan;
    }

    public String toString() {
        return "GridConstraints (row=" + this.myRow + ", col=" + this.myColumn + ", rowspan=" + this.myRowSpan + ", colspan=" + this.myColSpan + ")";
    }
}


