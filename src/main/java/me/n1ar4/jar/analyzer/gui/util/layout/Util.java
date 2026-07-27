/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import java.awt.*;
import java.util.ArrayList;

public final class Util {
    private static final Dimension MAX_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final int DEFAULT_INDENT = 10;

    public static Dimension getMinimumSize(Component component, GridConstraints constraints, boolean addIndent) {
        Dimension size = Util.getSize(constraints.myMinimumSize, component.getMinimumSize());
        if (addIndent) {
            size.width += 10 * constraints.getIndent();
        }
        return size;
    }

    public static Dimension getMaximumSize(Component component, GridConstraints constraints, boolean addIndent) {
        Dimension size = Util.getSize(constraints.myMaximumSize, MAX_SIZE);
        if (addIndent && size.width < Util.MAX_SIZE.width) {
            size.width += 10 * constraints.getIndent();
        }
        return size;
    }

    public static Dimension getPreferredSize(Component component, GridConstraints constraints, boolean addIndent) {
        Dimension size = Util.getSize(constraints.myPreferredSize, component.getPreferredSize());
        if (addIndent) {
            size.width += 10 * constraints.getIndent();
        }
        return size;
    }

    private static Dimension getSize(Dimension overridenSize, Dimension ownSize) {
        int overridenWidth = overridenSize.width >= 0 ? overridenSize.width : ownSize.width;
        int overridenHeight = overridenSize.height >= 0 ? overridenSize.height : ownSize.height;
        return new Dimension(overridenWidth, overridenHeight);
    }

    public static void adjustSize(Component component, GridConstraints constraints, Dimension size) {
        Dimension minimumSize = Util.getMinimumSize(component, constraints, false);
        Dimension maximumSize = Util.getMaximumSize(component, constraints, false);
        size.width = Math.max(size.width, minimumSize.width);
        size.height = Math.max(size.height, minimumSize.height);
        size.width = Math.min(size.width, maximumSize.width);
        size.height = Math.min(size.height, maximumSize.height);
    }

    public static int eliminate(
            int[] cellIndices, int[] spans, ArrayList<Integer> eliminated) {
        int size = cellIndices.length;
        if (size != spans.length) {
            throw new IllegalArgumentException("size mismatch: " + size + ", " + spans.length);
        }
        if (eliminated != null && eliminated.size() != 0) {
            throw new IllegalArgumentException("eliminated must be empty");
        }
        int cellCount = 0;
        int i = 0;
        while (i < size) {
            cellCount = Math.max(cellCount, cellIndices[i] + spans[i]);
            ++i;
        }
        int cell = cellCount - 1;
        while (cell >= 0) {
            boolean starts = false;
            boolean ends = false;
            int i2 = 0;
            while (i2 < size) {
                if (cellIndices[i2] == cell) {
                    starts = true;
                }
                if (cellIndices[i2] + spans[i2] - 1 == cell) {
                    ends = true;
                }
                ++i2;
            }
            if (!starts || !ends) {
                if (eliminated != null) {
                    eliminated.add(Integer.valueOf(cell));
                }
                int i3 = 0;
                while (i3 < size) {
                    boolean decreaseIndex;
                    boolean decreaseSpan = cellIndices[i3] <= cell && cell < cellIndices[i3] + spans[i3];
                    boolean bl = decreaseIndex = cellIndices[i3] > cell;
                    if (decreaseSpan) {
                        int n = i3;
                        spans[n] = spans[n] - 1;
                    }
                    if (decreaseIndex) {
                        int n = i3;
                        cellIndices[n] = cellIndices[n] - 1;
                    }
                    ++i3;
                }
                --cellCount;
            }
            --cell;
        }
        return cellCount;
    }
}


