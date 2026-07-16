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

import javax.swing.*;
import java.awt.*;

/**
 * Small helpers for building row/column based forms with Swing's native
 * {@link GridBagLayout}. The grid metadata is used only to calculate cell
 * gaps and outer margins; components are always added with standard
 * {@link GridBagConstraints}.
 */
public final class SwingLayout {
    private static final String GRID_SPEC_KEY = SwingLayout.class.getName() + ".gridSpec";

    private SwingLayout() {
    }

    public static void configureGrid(JPanel panel, int rows, int columns,
                                     Insets margin, int horizontalGap, int verticalGap) {
        panel.setLayout(new GridBagLayout());
        panel.putClientProperty(GRID_SPEC_KEY, new GridSpec(
                rows,
                columns,
                margin == null ? new Insets(0, 0, 0, 0) : margin,
                Math.max(horizontalGap, 0),
                Math.max(verticalGap, 0)));
    }

    public static void add(Container parent, Component child,
                           int row, int column, int rowSpan, int columnSpan,
                           int anchor, int fill, boolean growX, boolean growY,
                           Dimension minimumSize, Dimension preferredSize,
                           Dimension maximumSize, int indent) {
        applySize(child, minimumSize, SizeKind.MINIMUM);
        applySize(child, preferredSize, SizeKind.PREFERRED);
        applySize(child, maximumSize, SizeKind.MAXIMUM);

        GridBagConstraints constraints = constraints(
                row, column, rowSpan, columnSpan, anchor, fill, growX, growY);
        constraints.insets = cellInsets(parent, row, column, rowSpan, columnSpan, indent);
        parent.add(child, constraints);
    }

    public static GridBagConstraints constraints(int row, int column,
                                                 int rowSpan, int columnSpan,
                                                 int anchor, int fill,
                                                 boolean growX, boolean growY) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.gridwidth = columnSpan;
        constraints.gridheight = rowSpan;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.weightx = growX ? 1.0 : 0.0;
        constraints.weighty = growY ? 1.0 : 0.0;
        return constraints;
    }

    private static Insets cellInsets(Container parent, int row, int column,
                                     int rowSpan, int columnSpan, int indent) {
        GridSpec spec = null;
        if (parent instanceof JComponent) {
            Object value = ((JComponent) parent).getClientProperty(GRID_SPEC_KEY);
            if (value instanceof GridSpec) {
                spec = (GridSpec) value;
            }
        }
        if (spec == null) {
            return new Insets(0, indent * 10, 0, 0);
        }

        int top = row == 0 ? spec.margin.top : spec.verticalGap / 2;
        int bottom = row + rowSpan >= spec.rows
                ? spec.margin.bottom : spec.verticalGap - spec.verticalGap / 2;
        int left = column == 0 ? spec.margin.left : spec.horizontalGap / 2;
        int right = column + columnSpan >= spec.columns
                ? spec.margin.right : spec.horizontalGap - spec.horizontalGap / 2;
        return new Insets(top, left + indent * 10, bottom, right);
    }

    private static void applySize(Component component, Dimension requested, SizeKind kind) {
        if (requested == null) {
            return;
        }
        Dimension current;
        switch (kind) {
            case MINIMUM:
                current = component.getMinimumSize();
                break;
            case MAXIMUM:
                current = component.getMaximumSize();
                break;
            default:
                current = component.getPreferredSize();
                break;
        }
        Dimension normalized = new Dimension(
                requested.width < 0 ? current.width : requested.width,
                requested.height < 0 ? current.height : requested.height);
        switch (kind) {
            case MINIMUM:
                component.setMinimumSize(normalized);
                break;
            case MAXIMUM:
                component.setMaximumSize(normalized);
                break;
            default:
                component.setPreferredSize(normalized);
                break;
        }
    }

    private enum SizeKind {
        MINIMUM,
        PREFERRED,
        MAXIMUM
    }

    private static final class GridSpec {
        private final int rows;
        private final int columns;
        private final Insets margin;
        private final int horizontalGap;
        private final int verticalGap;

        private GridSpec(int rows, int columns, Insets margin,
                         int horizontalGap, int verticalGap) {
            this.rows = rows;
            this.columns = columns;
            this.margin = margin;
            this.horizontalGap = horizontalGap;
            this.verticalGap = verticalGap;
        }
    }
}
