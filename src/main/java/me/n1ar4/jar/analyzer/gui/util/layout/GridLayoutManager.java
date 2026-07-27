/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import me.n1ar4.jar.analyzer.gui.util.layout.AbstractLayout;
import me.n1ar4.jar.analyzer.gui.util.layout.DimensionInfo;
import me.n1ar4.jar.analyzer.gui.util.layout.GridConstraints;
import me.n1ar4.jar.analyzer.gui.util.layout.HorizontalInfo;
import me.n1ar4.jar.analyzer.gui.util.layout.LayoutState;
import me.n1ar4.jar.analyzer.gui.util.layout.Spacer;
import me.n1ar4.jar.analyzer.gui.util.layout.Util;
import me.n1ar4.jar.analyzer.gui.util.layout.VerticalInfo;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.JComponent;

public final class GridLayoutManager
extends AbstractLayout {
    private int myMinCellSize = 20;
    private final int[] myRowStretches;
    private final int[] myColumnStretches;
    private final int[] myYs;
    private final int[] myHeights;
    private final int[] myXs;
    private final int[] myWidths;
    private LayoutState myLayoutState;
    DimensionInfo myHorizontalInfo;
    DimensionInfo myVerticalInfo;
    private boolean mySameSizeHorizontally;
    private boolean mySameSizeVertically;
    public static Object DESIGN_TIME_INSETS = new Object();
    private static final int SKIP_ROW = 1;
    private static final int SKIP_COL = 2;

    public GridLayoutManager(int rowCount, int columnCount) {
        if (columnCount < 1) {
            throw new IllegalArgumentException("wrong columnCount: " + columnCount);
        }
        if (rowCount < 1) {
            throw new IllegalArgumentException("wrong rowCount: " + rowCount);
        }
        this.myRowStretches = new int[rowCount];
        int i = 0;
        while (i < rowCount) {
            this.myRowStretches[i] = 1;
            ++i;
        }
        this.myColumnStretches = new int[columnCount];
        int i2 = 0;
        while (i2 < columnCount) {
            this.myColumnStretches[i2] = 1;
            ++i2;
        }
        this.myXs = new int[columnCount];
        this.myWidths = new int[columnCount];
        this.myYs = new int[rowCount];
        this.myHeights = new int[rowCount];
    }

    public GridLayoutManager(int rowCount, int columnCount, Insets margin, int hGap, int vGap) {
        this(rowCount, columnCount);
        this.setMargin(margin);
        this.setHGap(hGap);
        this.setVGap(vGap);
        this.myMinCellSize = 0;
    }

    public GridLayoutManager(int rowCount, int columnCount, Insets margin, int hGap, int vGap, boolean sameSizeHorizontally, boolean sameSizeVertically) {
        this(rowCount, columnCount, margin, hGap, vGap);
        this.mySameSizeHorizontally = sameSizeHorizontally;
        this.mySameSizeVertically = sameSizeVertically;
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        GridConstraints c = (GridConstraints)constraints;
        int row = c.getRow();
        int rowSpan = c.getRowSpan();
        int rowCount = this.getRowCount();
        if (row < 0 || row >= rowCount) {
            throw new IllegalArgumentException("wrong row: " + row);
        }
        if (row + rowSpan - 1 >= rowCount) {
            throw new IllegalArgumentException("wrong row span: " + rowSpan + "; row=" + row + " rowCount=" + rowCount);
        }
        int column = c.getColumn();
        int colSpan = c.getColSpan();
        int columnCount = this.getColumnCount();
        if (column < 0 || column >= columnCount) {
            throw new IllegalArgumentException("wrong column: " + column);
        }
        if (column + colSpan - 1 >= columnCount) {
            throw new IllegalArgumentException("wrong col span: " + colSpan + "; column=" + column + " columnCount=" + columnCount);
        }
        super.addLayoutComponent(comp, constraints);
    }

    public int getRowCount() {
        return this.myRowStretches.length;
    }

    public int getColumnCount() {
        return this.myColumnStretches.length;
    }

    public int getRowStretch(int rowIndex) {
        return this.myRowStretches[rowIndex];
    }

    public void setRowStretch(int rowIndex, int stretch) {
        if (stretch < 1) {
            throw new IllegalArgumentException("wrong stretch: " + stretch);
        }
        this.myRowStretches[rowIndex] = stretch;
    }

    public int getColumnStretch(int columnIndex) {
        return this.myColumnStretches[columnIndex];
    }

    public void setColumnStretch(int columnIndex, int stretch) {
        if (stretch < 1) {
            throw new IllegalArgumentException("wrong stretch: " + stretch);
        }
        this.myColumnStretches[columnIndex] = stretch;
    }

    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public Dimension minimumLayoutSize(Container container) {
        this.validateInfos(container);
        DimensionInfo horizontalInfo = this.myHorizontalInfo;
        DimensionInfo verticalInfo = this.myVerticalInfo;
        Dimension result = this.getTotalGap(container, horizontalInfo, verticalInfo);
        int[] widths = this.getMinSizes(horizontalInfo);
        if (this.mySameSizeHorizontally) {
            GridLayoutManager.makeSameSizes(widths);
        }
        result.width += GridLayoutManager.sum(widths);
        int[] heights = this.getMinSizes(verticalInfo);
        if (this.mySameSizeVertically) {
            GridLayoutManager.makeSameSizes(heights);
        }
        result.height += GridLayoutManager.sum(heights);
        return result;
    }

    private static void makeSameSizes(int[] widths) {
        int max = widths[0];
        int i = 0;
        while (i < widths.length) {
            int width = widths[i];
            max = Math.max(width, max);
            ++i;
        }
        int i2 = 0;
        while (i2 < widths.length) {
            widths[i2] = max;
            ++i2;
        }
    }

    private static int[] getSameSizes(DimensionInfo info, int totalWidth) {
        int[] widths = new int[info.getCellCount()];
        int average = totalWidth / widths.length;
        int rest = totalWidth % widths.length;
        int i = 0;
        while (i < widths.length) {
            widths[i] = average;
            if (rest > 0) {
                int n = i;
                widths[n] = widths[n] + 1;
                --rest;
            }
            ++i;
        }
        return widths;
    }

    public Dimension preferredLayoutSize(Container container) {
        this.validateInfos(container);
        DimensionInfo horizontalInfo = this.myHorizontalInfo;
        DimensionInfo verticalInfo = this.myVerticalInfo;
        Dimension result = this.getTotalGap(container, horizontalInfo, verticalInfo);
        int[] widths = this.getPrefSizes(horizontalInfo);
        if (this.mySameSizeHorizontally) {
            GridLayoutManager.makeSameSizes(widths);
        }
        result.width += GridLayoutManager.sum(widths);
        int[] heights = this.getPrefSizes(verticalInfo);
        if (this.mySameSizeVertically) {
            GridLayoutManager.makeSameSizes(heights);
        }
        result.height += GridLayoutManager.sum(heights);
        return result;
    }

    private static int sum(int[] ints) {
        int result = 0;
        int i = ints.length - 1;
        while (i >= 0) {
            result += ints[i];
            --i;
        }
        return result;
    }

    private Dimension getTotalGap(Container container, DimensionInfo hInfo, DimensionInfo vInfo) {
        Insets insets = GridLayoutManager.getInsets(container);
        return new Dimension(insets.left + insets.right + GridLayoutManager.countGap(hInfo, 0, hInfo.getCellCount()) + this.myMargin.left + this.myMargin.right, insets.top + insets.bottom + GridLayoutManager.countGap(vInfo, 0, vInfo.getCellCount()) + this.myMargin.top + this.myMargin.bottom);
    }

    private static int getDesignTimeInsets(Container container) {
        while (container != null) {
            Integer designTimeInsets;
            if (container instanceof JComponent && (designTimeInsets = (Integer)((JComponent)container).getClientProperty(DESIGN_TIME_INSETS)) != null) {
                return designTimeInsets;
            }
            container = container.getParent();
        }
        return 0;
    }

    private static Insets getInsets(Container container) {
        Insets insets = container.getInsets();
        int insetsValue = GridLayoutManager.getDesignTimeInsets(container);
        if (insetsValue != 0) {
            return new Insets(insets.top + insetsValue, insets.left + insetsValue, insets.bottom + insetsValue, insets.right + insetsValue);
        }
        return insets;
    }

    private static int countGap(DimensionInfo info, int startCell, int cellCount) {
        int counter = 0;
        int cellIndex = startCell + cellCount - 2;
        while (cellIndex >= startCell) {
            if (GridLayoutManager.shouldAddGapAfterCell(info, cellIndex)) {
                ++counter;
            }
            --cellIndex;
        }
        return counter * info.getGap();
    }

    private static boolean shouldAddGapAfterCell(DimensionInfo info, int cellIndex) {
        if (cellIndex < 0 || cellIndex >= info.getCellCount()) {
            throw new IllegalArgumentException("wrong cellIndex: " + cellIndex + "; cellCount=" + info.getCellCount());
        }
        boolean endsInThis = false;
        boolean startsInNext = false;
        int indexOfNextNotEmpty = -1;
        int i = cellIndex + 1;
        while (i < info.getCellCount()) {
            if (!GridLayoutManager.isCellEmpty(info, i)) {
                indexOfNextNotEmpty = i;
                break;
            }
            ++i;
        }
        int i2 = 0;
        while (i2 < info.getComponentCount()) {
            Component component = info.getComponent(i2);
            if (!(component instanceof Spacer)) {
                if (info.componentBelongsCell(i2, cellIndex) && DimensionInfo.findAlignedChild(component, info.getConstraints(i2)) != null) {
                    return true;
                }
                if (info.getCell(i2) == indexOfNextNotEmpty) {
                    startsInNext = true;
                }
                if (info.getCell(i2) + info.getSpan(i2) - 1 == cellIndex) {
                    endsInThis = true;
                }
            }
            ++i2;
        }
        return startsInNext && endsInThis;
    }

    private static boolean isCellEmpty(DimensionInfo info, int cellIndex) {
        if (cellIndex < 0 || cellIndex >= info.getCellCount()) {
            throw new IllegalArgumentException("wrong cellIndex: " + cellIndex + "; cellCount=" + info.getCellCount());
        }
        int i = 0;
        while (i < info.getComponentCount()) {
            Component component = info.getComponent(i);
            if (info.getCell(i) == cellIndex && !(component instanceof Spacer)) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public void layoutContainer(Container container) {
        int i;
        this.validateInfos(container);
        LayoutState layoutState = this.myLayoutState;
        DimensionInfo horizontalInfo = this.myHorizontalInfo;
        DimensionInfo verticalInfo = this.myVerticalInfo;
        Insets insets = GridLayoutManager.getInsets(container);
        int skipLayout = this.checkSetSizesFromParent(container, insets);
        Dimension gap = this.getTotalGap(container, horizontalInfo, verticalInfo);
        Dimension size = container.getSize();
        size.width -= gap.width;
        size.height -= gap.height;
        Dimension prefSize = this.preferredLayoutSize(container);
        prefSize.width -= gap.width;
        prefSize.height -= gap.height;
        Dimension minSize = this.minimumLayoutSize(container);
        minSize.width -= gap.width;
        minSize.height -= gap.height;
        if ((skipLayout & 1) == 0) {
            int[] heights;
            if (this.mySameSizeVertically) {
                heights = GridLayoutManager.getSameSizes(verticalInfo, Math.max(size.height, minSize.height));
            } else if (size.height < prefSize.height) {
                heights = this.getMinSizes(verticalInfo);
                this.new_doIt(heights, 0, verticalInfo.getCellCount(), size.height, verticalInfo, true);
            } else {
                heights = this.getPrefSizes(verticalInfo);
                this.new_doIt(heights, 0, verticalInfo.getCellCount(), size.height, verticalInfo, false);
            }
            int y = insets.top + this.myMargin.top;
            i = 0;
            while (i < heights.length) {
                this.myYs[i] = y;
                this.myHeights[i] = heights[i];
                y += heights[i];
                if (GridLayoutManager.shouldAddGapAfterCell(verticalInfo, i)) {
                    y += verticalInfo.getGap();
                }
                ++i;
            }
        }
        if ((skipLayout & 2) == 0) {
            int[] widths;
            if (this.mySameSizeHorizontally) {
                widths = GridLayoutManager.getSameSizes(horizontalInfo, Math.max(size.width, minSize.width));
            } else if (size.width < prefSize.width) {
                widths = this.getMinSizes(horizontalInfo);
                this.new_doIt(widths, 0, horizontalInfo.getCellCount(), size.width, horizontalInfo, true);
            } else {
                widths = this.getPrefSizes(horizontalInfo);
                this.new_doIt(widths, 0, horizontalInfo.getCellCount(), size.width, horizontalInfo, false);
            }
            int x = insets.left + this.myMargin.left;
            i = 0;
            while (i < widths.length) {
                this.myXs[i] = x;
                this.myWidths[i] = widths[i];
                x += widths[i];
                if (GridLayoutManager.shouldAddGapAfterCell(horizontalInfo, i)) {
                    x += horizontalInfo.getGap();
                }
                ++i;
            }
        }
        int i2 = 0;
        while (i2 < layoutState.getComponentCount()) {
            GridConstraints c = layoutState.getConstraints(i2);
            Component component = layoutState.getComponent(i2);
            int column = horizontalInfo.getCell(i2);
            int colSpan = horizontalInfo.getSpan(i2);
            int row = verticalInfo.getCell(i2);
            int rowSpan = verticalInfo.getSpan(i2);
            int cellWidth = this.myXs[column + colSpan - 1] + this.myWidths[column + colSpan - 1] - this.myXs[column];
            int cellHeight = this.myYs[row + rowSpan - 1] + this.myHeights[row + rowSpan - 1] - this.myYs[row];
            Dimension componentSize = new Dimension(cellWidth, cellHeight);
            if ((c.getFill() & 1) == 0) {
                componentSize.width = Math.min(componentSize.width, horizontalInfo.getPreferredWidth(i2));
            }
            if ((c.getFill() & 2) == 0) {
                componentSize.height = Math.min(componentSize.height, verticalInfo.getPreferredWidth(i2));
            }
            Util.adjustSize(component, c, componentSize);
            int dx = 0;
            int dy = 0;
            if ((c.getAnchor() & 4) != 0) {
                dx = cellWidth - componentSize.width;
            } else if ((c.getAnchor() & 8) == 0) {
                dx = (cellWidth - componentSize.width) / 2;
            }
            if ((c.getAnchor() & 2) != 0) {
                dy = cellHeight - componentSize.height;
            } else if ((c.getAnchor() & 1) == 0) {
                dy = (cellHeight - componentSize.height) / 2;
            }
            int indent = 10 * c.getIndent();
            componentSize.width -= indent;
            component.setBounds(this.myXs[column] + (dx += indent), this.myYs[row] + dy, componentSize.width, componentSize.height);
            ++i2;
        }
    }

    private int checkSetSizesFromParent(Container container, Insets insets) {
        int skipLayout = 0;
        GridLayoutManager parentGridLayout = null;
        GridConstraints parentGridConstraints = null;
        Container parent = container.getParent();
        if (parent != null) {
            if (parent.getLayout() instanceof GridLayoutManager) {
                parentGridLayout = (GridLayoutManager)parent.getLayout();
                parentGridConstraints = parentGridLayout.getConstraintsForComponent(container);
            } else {
                Container parent2 = parent.getParent();
                if (parent2 != null && parent2.getLayout() instanceof GridLayoutManager) {
                    parentGridLayout = (GridLayoutManager)parent2.getLayout();
                    parentGridConstraints = parentGridLayout.getConstraintsForComponent(parent);
                }
            }
        }
        if (parentGridLayout != null && parentGridConstraints.isUseParentLayout()) {
            int i;
            if (this.myRowStretches.length == parentGridConstraints.getRowSpan()) {
                int row = parentGridConstraints.getRow();
                this.myYs[0] = insets.top + this.myMargin.top;
                this.myHeights[0] = parentGridLayout.myHeights[row] - this.myYs[0];
                i = 1;
                while (i < this.myRowStretches.length) {
                    this.myYs[i] = parentGridLayout.myYs[i + row] - parentGridLayout.myYs[row];
                    this.myHeights[i] = parentGridLayout.myHeights[i + row];
                    ++i;
                }
                int n = this.myRowStretches.length - 1;
                this.myHeights[n] = this.myHeights[n] - (insets.bottom + this.myMargin.bottom);
                skipLayout |= 1;
            }
            if (this.myColumnStretches.length == parentGridConstraints.getColSpan()) {
                int col = parentGridConstraints.getColumn();
                this.myXs[0] = insets.left + this.myMargin.left;
                this.myWidths[0] = parentGridLayout.myWidths[col] - this.myXs[0];
                i = 1;
                while (i < this.myColumnStretches.length) {
                    this.myXs[i] = parentGridLayout.myXs[i + col] - parentGridLayout.myXs[col];
                    this.myWidths[i] = parentGridLayout.myWidths[i + col];
                    ++i;
                }
                int n = this.myColumnStretches.length - 1;
                this.myWidths[n] = this.myWidths[n] - (insets.right + this.myMargin.right);
                skipLayout |= 2;
            }
        }
        return skipLayout;
    }

    public void invalidateLayout(Container container) {
        this.myLayoutState = null;
        this.myHorizontalInfo = null;
        this.myVerticalInfo = null;
    }

    void validateInfos(Container container) {
        if (this.myLayoutState == null) {
            this.myLayoutState = new LayoutState(this, GridLayoutManager.getDesignTimeInsets(container) == 0);
            this.myHorizontalInfo = new HorizontalInfo(this.myLayoutState, AbstractLayout.getHGapImpl(container));
            this.myVerticalInfo = new VerticalInfo(this.myLayoutState, AbstractLayout.getVGapImpl(container));
        }
    }

    public int[] getXs() {
        return this.myXs;
    }

    public int[] getWidths() {
        return this.myWidths;
    }

    public int[] getYs() {
        return this.myYs;
    }

    public int[] getHeights() {
        return this.myHeights;
    }

    public int[] getCoords(boolean isRow) {
        return isRow ? this.myYs : this.myXs;
    }

    public int[] getSizes(boolean isRow) {
        return isRow ? this.myHeights : this.myWidths;
    }

    private int[] getMinSizes(DimensionInfo info) {
        return this.getMinOrPrefSizes(info, true);
    }

    private int[] getPrefSizes(DimensionInfo info) {
        return this.getMinOrPrefSizes(info, false);
    }

    private int[] getMinOrPrefSizes(DimensionInfo info, boolean min) {
        int[] widths = new int[info.getCellCount()];
        int i = 0;
        while (i < widths.length) {
            widths[i] = this.myMinCellSize;
            ++i;
        }
        int i2 = info.getComponentCount() - 1;
        while (i2 >= 0) {
            if (info.getSpan(i2) == 1) {
                int size = min ? GridLayoutManager.getMin2(info, i2) : Math.max(info.getMinimumWidth(i2), info.getPreferredWidth(i2));
                int gap = GridLayoutManager.countGap(info, info.getCell(i2), info.getSpan(i2));
                size = Math.max(size - gap, 0);
                widths[info.getCell((int)i2)] = Math.max(widths[info.getCell(i2)], size);
            }
            --i2;
        }
        GridLayoutManager.updateSizesFromChildren(info, min, widths);
        boolean[] toProcess = new boolean[info.getCellCount()];
        int i3 = info.getComponentCount() - 1;
        while (i3 >= 0) {
            int size = min ? GridLayoutManager.getMin2(info, i3) : Math.max(info.getMinimumWidth(i3), info.getPreferredWidth(i3));
            int span = info.getSpan(i3);
            int cell = info.getCell(i3);
            int gap = GridLayoutManager.countGap(info, cell, span);
            size = Math.max(size - gap, 0);
            Arrays.fill(toProcess, false);
            int curSize = 0;
            int j = 0;
            while (j < span) {
                curSize += widths[j + cell];
                toProcess[j + cell] = true;
                ++j;
            }
            if (curSize < size) {
                boolean[] higherPriorityCells = new boolean[toProcess.length];
                this.getCellsWithHigherPriorities(info, toProcess, higherPriorityCells, false, widths);
                GridLayoutManager.distribute(higherPriorityCells, info, size - curSize, widths);
            }
            --i3;
        }
        return widths;
    }

    private static void updateSizesFromChildren(DimensionInfo info, boolean min, int[] widths) {
        int i = info.getComponentCount() - 1;
        while (i >= 0) {
            Component child = info.getComponent(i);
            GridConstraints c = info.getConstraints(i);
            if (c.isUseParentLayout() && child instanceof Container) {
                Container childContainer;
                Container container = (Container)child;
                if (container.getLayout() instanceof GridLayoutManager) {
                    GridLayoutManager.updateSizesFromChild(info, min, widths, container, i);
                } else if (container.getComponentCount() == 1 && container.getComponent(0) instanceof Container && (childContainer = (Container)container.getComponent(0)).getLayout() instanceof GridLayoutManager) {
                    GridLayoutManager.updateSizesFromChild(info, min, widths, childContainer, i);
                }
            }
            --i;
        }
    }

    private static void updateSizesFromChild(DimensionInfo info, boolean min, int[] widths, Container container, int childIndex) {
        GridLayoutManager childLayout = (GridLayoutManager)container.getLayout();
        if (info.getSpan(childIndex) == info.getChildLayoutCellCount(childLayout)) {
            childLayout.validateInfos(container);
            DimensionInfo childInfo = info instanceof HorizontalInfo ? childLayout.myHorizontalInfo : childLayout.myVerticalInfo;
            int[] sizes = childLayout.getMinOrPrefSizes(childInfo, min);
            int cell = info.getCell(childIndex);
            int j = 0;
            while (j < sizes.length) {
                widths[cell + j] = Math.max(widths[cell + j], sizes[j]);
                ++j;
            }
        }
    }

    private static int getMin2(DimensionInfo info, int componentIndex) {
        int s = (info.getSizePolicy(componentIndex) & 1) != 0 ? info.getMinimumWidth(componentIndex) : Math.max(info.getMinimumWidth(componentIndex), info.getPreferredWidth(componentIndex));
        return s;
    }

    private void new_doIt(int[] widths, int cell, int span, int minWidth, DimensionInfo info, boolean checkPrefs) {
        int toDistribute = minWidth;
        int i = cell;
        while (i < cell + span) {
            toDistribute -= widths[i];
            ++i;
        }
        if (toDistribute <= 0) {
            return;
        }
        boolean[] allowedCells = new boolean[info.getCellCount()];
        int i2 = cell;
        while (i2 < cell + span) {
            allowedCells[i2] = true;
            ++i2;
        }
        boolean[] higherPriorityCells = new boolean[info.getCellCount()];
        this.getCellsWithHigherPriorities(info, allowedCells, higherPriorityCells, checkPrefs, widths);
        GridLayoutManager.distribute(higherPriorityCells, info, toDistribute, widths);
    }

    private static void distribute(boolean[] higherPriorityCells, DimensionInfo info, int toDistribute, int[] widths) {
        int stretches = 0;
        int i = 0;
        while (i < info.getCellCount()) {
            if (higherPriorityCells[i]) {
                stretches += info.getStretch(i);
            }
            ++i;
        }
        int toDistributeFrozen = toDistribute;
        int i2 = 0;
        while (i2 < info.getCellCount()) {
            if (higherPriorityCells[i2]) {
                int addon = toDistributeFrozen * info.getStretch(i2) / stretches;
                int n = i2;
                widths[n] = widths[n] + addon;
                toDistribute -= addon;
            }
            ++i2;
        }
        if (toDistribute != 0) {
            int i3 = 0;
            while (i3 < info.getCellCount()) {
                if (higherPriorityCells[i3]) {
                    int n = i3;
                    widths[n] = widths[n] + 1;
                    if (--toDistribute == 0) break;
                }
                ++i3;
            }
        }
        if (toDistribute != 0) {
            throw new IllegalStateException("toDistribute = " + toDistribute);
        }
    }

    private void getCellsWithHigherPriorities(DimensionInfo info, boolean[] allowedCells, boolean[] higherPriorityCells, boolean checkPrefs, int[] widths) {
        int cell;
        Arrays.fill(higherPriorityCells, false);
        int foundCells = 0;
        if (checkPrefs) {
            int[] prefs = this.getMinOrPrefSizes(info, false);
            cell = 0;
            while (cell < allowedCells.length) {
                if (allowedCells[cell] && !GridLayoutManager.isCellEmpty(info, cell) && prefs[cell] > widths[cell]) {
                    higherPriorityCells[cell] = true;
                    ++foundCells;
                }
                ++cell;
            }
            if (foundCells > 0) {
                return;
            }
        }
        int cell2 = 0;
        while (cell2 < allowedCells.length) {
            if (allowedCells[cell2] && (info.getCellSizePolicy(cell2) & 4) != 0) {
                higherPriorityCells[cell2] = true;
                ++foundCells;
            }
            ++cell2;
        }
        if (foundCells > 0) {
            return;
        }
        cell = 0;
        while (cell < allowedCells.length) {
            if (allowedCells[cell] && (info.getCellSizePolicy(cell) & 2) != 0) {
                higherPriorityCells[cell] = true;
                ++foundCells;
            }
            ++cell;
        }
        if (foundCells > 0) {
            return;
        }
        int cell3 = 0;
        while (cell3 < allowedCells.length) {
            if (allowedCells[cell3] && !GridLayoutManager.isCellEmpty(info, cell3)) {
                higherPriorityCells[cell3] = true;
                ++foundCells;
            }
            ++cell3;
        }
        if (foundCells > 0) {
            return;
        }
        int cell4 = 0;
        while (cell4 < allowedCells.length) {
            if (allowedCells[cell4]) {
                higherPriorityCells[cell4] = true;
            }
            ++cell4;
        }
    }

    public boolean isSameSizeHorizontally() {
        return this.mySameSizeHorizontally;
    }

    public boolean isSameSizeVertically() {
        return this.mySameSizeVertically;
    }

    public void setSameSizeHorizontally(boolean sameSizeHorizontally) {
        this.mySameSizeHorizontally = sameSizeHorizontally;
    }

    public void setSameSizeVertically(boolean sameSizeVertically) {
        this.mySameSizeVertically = sameSizeVertically;
    }

    public int[] getHorizontalGridLines() {
        int[] result = new int[this.myYs.length + 1];
        result[0] = this.myYs[0];
        int i = 0;
        while (i < this.myYs.length - 1) {
            result[i + 1] = (this.myYs[i] + this.myHeights[i] + this.myYs[i + 1]) / 2;
            ++i;
        }
        result[this.myYs.length] = this.myYs[this.myYs.length - 1] + this.myHeights[this.myYs.length - 1];
        return result;
    }

    public int[] getVerticalGridLines() {
        int[] result = new int[this.myXs.length + 1];
        result[0] = this.myXs[0];
        int i = 0;
        while (i < this.myXs.length - 1) {
            result[i + 1] = (this.myXs[i] + this.myWidths[i] + this.myXs[i + 1]) / 2;
            ++i;
        }
        result[this.myXs.length] = this.myXs[this.myXs.length - 1] + this.myWidths[this.myXs.length - 1];
        return result;
    }

    public int getCellCount(boolean isRow) {
        return isRow ? this.getRowCount() : this.getColumnCount();
    }

    public int getCellSizePolicy(boolean isRow, int cellIndex) {
        DimensionInfo info;
        DimensionInfo dimensionInfo = info = isRow ? this.myVerticalInfo : this.myHorizontalInfo;
        if (info == null) {
            return 0;
        }
        return info.getCellSizePolicy(cellIndex);
    }
}


