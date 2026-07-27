/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import me.n1ar4.jar.analyzer.gui.util.layout.DimensionInfo;
import me.n1ar4.jar.analyzer.gui.util.layout.GridConstraints;
import me.n1ar4.jar.analyzer.gui.util.layout.GridLayoutManager;
import me.n1ar4.jar.analyzer.gui.util.layout.LayoutState;

final class HorizontalInfo
extends DimensionInfo {
    public HorizontalInfo(LayoutState layoutState, int gap) {
        super(layoutState, gap);
    }

    protected int getOriginalCell(GridConstraints constraints) {
        return constraints.getColumn();
    }

    protected int getOriginalSpan(GridConstraints constraints) {
        return constraints.getColSpan();
    }

    int getSizePolicy(int componentIndex) {
        return this.myLayoutState.getConstraints(componentIndex).getHSizePolicy();
    }

    int getChildLayoutCellCount(GridLayoutManager childLayout) {
        return childLayout.getColumnCount();
    }

    public int getMinimumWidth(int componentIndex) {
        return this.getMinimumSize((int)componentIndex).width;
    }

    public DimensionInfo getDimensionInfo(GridLayoutManager grid) {
        return grid.myHorizontalInfo;
    }

    public int getCellCount() {
        return this.myLayoutState.getColumnCount();
    }

    public int getPreferredWidth(int componentIndex) {
        return this.getPreferredSize((int)componentIndex).width;
    }
}


