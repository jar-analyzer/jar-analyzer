/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.gui.util.layout.GridConstraints;
import me.n1ar4.jar.analyzer.gui.util.layout.GridLayoutManager;
import me.n1ar4.jar.analyzer.gui.util.layout.Spacer;

import javax.swing.*;
import java.awt.*;

/**
 * Pure-Java Swing facade for the project's Designer-compatible grid layout.
 *
 * <p>The public API uses AWT anchor/fill constants so form code does not
 * depend on the compatibility implementation. Size policies retain their
 * original three-state semantics: shrink, grow, and prefer-to-grow.</p>
 */
public final class SwingLayout {
    public static final int SIZEPOLICY_FIXED = GridConstraints.SIZEPOLICY_FIXED;
    public static final int SIZEPOLICY_CAN_SHRINK = GridConstraints.SIZEPOLICY_CAN_SHRINK;
    public static final int SIZEPOLICY_CAN_GROW = GridConstraints.SIZEPOLICY_CAN_GROW;
    public static final int SIZEPOLICY_WANT_GROW = GridConstraints.SIZEPOLICY_WANT_GROW;

    private SwingLayout() {
    }

    public static void configureGrid(JPanel panel, int rows, int columns,
                                     Insets margin, int horizontalGap, int verticalGap) {
        panel.setLayout(new GridLayoutManager(
                rows,
                columns,
                margin == null ? new Insets(0, 0, 0, 0) : margin,
                horizontalGap,
                verticalGap));
    }

    public static void add(Container parent, Component child,
                           int row, int column, int rowSpan, int columnSpan,
                           int anchor, int fill, int horizontalSizePolicy,
                           int verticalSizePolicy,
                           Dimension minimumSize, Dimension preferredSize,
                           Dimension maximumSize, int indent) {
        parent.add(child, constraints(
                row, column, rowSpan, columnSpan,
                anchor, fill, horizontalSizePolicy, verticalSizePolicy,
                minimumSize, preferredSize, maximumSize, indent));
    }

    public static GridConstraints constraints(
            int row, int column, int rowSpan, int columnSpan,
            int anchor, int fill, int horizontalSizePolicy,
            int verticalSizePolicy) {
        return constraints(
                row, column, rowSpan, columnSpan,
                anchor, fill, horizontalSizePolicy, verticalSizePolicy,
                null, null, null, 0);
    }

    public static Component spacer() {
        return new Spacer();
    }

    private static GridConstraints constraints(
            int row, int column, int rowSpan, int columnSpan,
            int anchor, int fill, int horizontalSizePolicy,
            int verticalSizePolicy, Dimension minimumSize,
            Dimension preferredSize, Dimension maximumSize, int indent) {
        return new GridConstraints(
                row,
                column,
                rowSpan,
                columnSpan,
                toGridAnchor(anchor),
                toGridFill(fill),
                horizontalSizePolicy,
                verticalSizePolicy,
                minimumSize,
                preferredSize,
                maximumSize,
                indent,
                false);
    }

    private static int toGridAnchor(int anchor) {
        switch (anchor) {
            case GridBagConstraints.NORTH:
                return GridConstraints.ANCHOR_NORTH;
            case GridBagConstraints.SOUTH:
                return GridConstraints.ANCHOR_SOUTH;
            case GridBagConstraints.EAST:
                return GridConstraints.ANCHOR_EAST;
            case GridBagConstraints.WEST:
                return GridConstraints.ANCHOR_WEST;
            case GridBagConstraints.NORTHEAST:
                return GridConstraints.ANCHOR_NORTHEAST;
            case GridBagConstraints.SOUTHEAST:
                return GridConstraints.ANCHOR_SOUTHEAST;
            case GridBagConstraints.SOUTHWEST:
                return GridConstraints.ANCHOR_SOUTHWEST;
            case GridBagConstraints.NORTHWEST:
                return GridConstraints.ANCHOR_NORTHWEST;
            case GridBagConstraints.CENTER:
            default:
                return GridConstraints.ANCHOR_CENTER;
        }
    }

    private static int toGridFill(int fill) {
        switch (fill) {
            case GridBagConstraints.HORIZONTAL:
                return GridConstraints.FILL_HORIZONTAL;
            case GridBagConstraints.VERTICAL:
                return GridConstraints.FILL_VERTICAL;
            case GridBagConstraints.BOTH:
                return GridConstraints.FILL_BOTH;
            case GridBagConstraints.NONE:
            default:
                return GridConstraints.FILL_NONE;
        }
    }
}
