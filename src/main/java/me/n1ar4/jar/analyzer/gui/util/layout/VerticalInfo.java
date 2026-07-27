/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

final class VerticalInfo
        extends DimensionInfo {
    public VerticalInfo(LayoutState layoutState, int gap) {
        super(layoutState, gap);
    }

    protected int getOriginalCell(GridConstraints constraints) {
        return constraints.getRow();
    }

    protected int getOriginalSpan(GridConstraints constraints) {
        return constraints.getRowSpan();
    }

    int getSizePolicy(int componentIndex) {
        return this.myLayoutState.getConstraints(componentIndex).getVSizePolicy();
    }

    int getChildLayoutCellCount(GridLayoutManager childLayout) {
        return childLayout.getRowCount();
    }

    public int getMinimumWidth(int componentIndex) {
        return this.getMinimumSize((int) componentIndex).height;
    }

    public DimensionInfo getDimensionInfo(GridLayoutManager grid) {
        return grid.myVerticalInfo;
    }

    public int getCellCount() {
        return this.myLayoutState.getRowCount();
    }

    public int getPreferredWidth(int componentIndex) {
        return this.getPreferredSize((int) componentIndex).height;
    }
}


