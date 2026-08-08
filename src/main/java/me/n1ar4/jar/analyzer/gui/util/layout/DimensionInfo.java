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
import java.util.ArrayList;

public abstract class DimensionInfo {
    private final int[] myCell;
    private final int[] mySpan;
    protected final LayoutState myLayoutState;
    private final int[] myStretches;
    private final int[] mySpansAfterElimination;
    private final int[] myCellSizePolicies;
    private final int myGap;

    public DimensionInfo(LayoutState layoutState, int gap) {
        if (layoutState == null) {
            throw new IllegalArgumentException("layoutState cannot be null");
        }
        if (gap < 0) {
            throw new IllegalArgumentException("invalid gap: " + gap);
        }
        this.myLayoutState = layoutState;
        this.myGap = gap;
        this.myCell = new int[layoutState.getComponentCount()];
        this.mySpan = new int[layoutState.getComponentCount()];
        int i = 0;
        while (i < layoutState.getComponentCount()) {
            GridConstraints c = layoutState.getConstraints(i);
            this.myCell[i] = this.getOriginalCell(c);
            this.mySpan[i] = this.getOriginalSpan(c);
            ++i;
        }
        this.myStretches = new int[this.getCellCount()];
        int i2 = 0;
        while (i2 < this.myStretches.length) {
            this.myStretches[i2] = 1;
            ++i2;
        }
        ArrayList<Integer> eliminated = new ArrayList<>();
        this.mySpansAfterElimination = (int[]) this.mySpan.clone();
        Util.eliminate((int[]) this.myCell.clone(), this.mySpansAfterElimination, eliminated);
        this.myCellSizePolicies = new int[this.getCellCount()];
        int i3 = 0;
        while (i3 < this.myCellSizePolicies.length) {
            this.myCellSizePolicies[i3] = this.getCellSizePolicyImpl(i3, eliminated);
            ++i3;
        }
    }

    public final int getComponentCount() {
        return this.myLayoutState.getComponentCount();
    }

    public final Component getComponent(int componentIndex) {
        return this.myLayoutState.getComponent(componentIndex);
    }

    public final GridConstraints getConstraints(int componentIndex) {
        return this.myLayoutState.getConstraints(componentIndex);
    }

    public abstract int getCellCount();

    public abstract int getPreferredWidth(int var1);

    public abstract int getMinimumWidth(int var1);

    public abstract DimensionInfo getDimensionInfo(GridLayoutManager var1);

    public final int getCell(int componentIndex) {
        return this.myCell[componentIndex];
    }

    public final int getSpan(int componentIndex) {
        return this.mySpan[componentIndex];
    }

    public final int getStretch(int cellIndex) {
        return this.myStretches[cellIndex];
    }

    protected abstract int getOriginalCell(GridConstraints var1);

    protected abstract int getOriginalSpan(GridConstraints var1);

    abstract int getSizePolicy(int var1);

    abstract int getChildLayoutCellCount(GridLayoutManager var1);

    public final int getGap() {
        return this.myGap;
    }

    public boolean componentBelongsCell(int componentIndex, int cellIndex) {
        int componentStartCell = this.getCell(componentIndex);
        int span = this.getSpan(componentIndex);
        return componentStartCell <= cellIndex && cellIndex < componentStartCell + span;
    }

    public final int getCellSizePolicy(int cellIndex) {
        return this.myCellSizePolicies[cellIndex];
    }

    private int getCellSizePolicyImpl(
            int cellIndex, ArrayList<Integer> eliminatedCells) {
        int policyFromChild = this.getCellSizePolicyFromInheriting(cellIndex);
        if (policyFromChild != -1) {
            return policyFromChild;
        }
        int i = eliminatedCells.size() - 1;
        while (i >= 0) {
            if (cellIndex == (Integer) eliminatedCells.get(i)) {
                return 1;
            }
            --i;
        }
        return this.calcCellSizePolicy(cellIndex);
    }

    private int calcCellSizePolicy(int cellIndex) {
        boolean canShrink = true;
        boolean canGrow = false;
        boolean wantGrow = false;
        boolean weakCanGrow = true;
        boolean weakWantGrow = true;
        int countOfBelongingComponents = 0;
        int i = 0;
        while (i < this.getComponentCount()) {
            if (this.componentBelongsCell(i, cellIndex)) {
                boolean thisWantGrow;
                ++countOfBelongingComponents;
                int p = this.getSizePolicy(i);
                boolean thisCanShrink = (p & 1) != 0;
                boolean thisCanGrow = (p & 2) != 0;
                boolean bl = thisWantGrow = (p & 4) != 0;
                if (this.getCell(i) == cellIndex && this.mySpansAfterElimination[i] == 1) {
                    canShrink &= thisCanShrink;
                    canGrow |= thisCanGrow;
                    wantGrow |= thisWantGrow;
                }
                if (!thisCanGrow) {
                    weakCanGrow = false;
                }
                if (!thisWantGrow) {
                    weakWantGrow = false;
                }
            }
            ++i;
        }
        return (canShrink ? 1 : 0) | (canGrow || countOfBelongingComponents > 0 && weakCanGrow ? 2 : 0) | (wantGrow || countOfBelongingComponents > 0 && weakWantGrow ? 4 : 0);
    }

    private int getCellSizePolicyFromInheriting(int cellIndex) {
        int nonInheritingComponentsInCell = 0;
        int policyFromInheriting = -1;
        int i = this.getComponentCount() - 1;
        while (i >= 0) {
            if (this.componentBelongsCell(i, cellIndex)) {
                GridConstraints c;
                Component child = this.getComponent(i);
                Container container = DimensionInfo.findAlignedChild(child, c = this.getConstraints(i));
                if (container != null) {
                    GridLayoutManager grid = (GridLayoutManager) container.getLayout();
                    grid.validateInfos(container);
                    DimensionInfo info = this.getDimensionInfo(grid);
                    int policy = info.calcCellSizePolicy(cellIndex - this.getOriginalCell(c));
                    policyFromInheriting = policyFromInheriting == -1 ? policy : (policyFromInheriting |= policy);
                } else if (this.getOriginalCell(c) == cellIndex && this.getOriginalSpan(c) == 1 && !(child instanceof Spacer)) {
                    ++nonInheritingComponentsInCell;
                }
            }
            --i;
        }
        if (nonInheritingComponentsInCell > 0) {
            return -1;
        }
        return policyFromInheriting;
    }

    public static Container findAlignedChild(Component child, GridConstraints c) {
        if (c.isUseParentLayout() && child instanceof Container) {
            Container childContainer;
            Container container = (Container) child;
            if (container.getLayout() instanceof GridLayoutManager) {
                return container;
            }
            if (container.getComponentCount() == 1 && container.getComponent(0) instanceof Container && (childContainer = (Container) container.getComponent(0)).getLayout() instanceof GridLayoutManager) {
                return childContainer;
            }
        }
        return null;
    }

    protected final Dimension getPreferredSize(int componentIndex) {
        Dimension size = this.myLayoutState.myPreferredSizes[componentIndex];
        if (size == null) {
            this.myLayoutState.myPreferredSizes[componentIndex] = size = Util.getPreferredSize(this.myLayoutState.getComponent(componentIndex), this.myLayoutState.getConstraints(componentIndex), true);
        }
        return size;
    }

    protected final Dimension getMinimumSize(int componentIndex) {
        Dimension size = this.myLayoutState.myMinimumSizes[componentIndex];
        if (size == null) {
            this.myLayoutState.myMinimumSizes[componentIndex] = size = Util.getMinimumSize(this.myLayoutState.getComponent(componentIndex), this.myLayoutState.getConstraints(componentIndex), true);
        }
        return size;
    }
}


