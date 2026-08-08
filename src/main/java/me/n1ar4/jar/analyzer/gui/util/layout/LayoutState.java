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

public final class LayoutState {
    private final Component[] myComponents;
    private final GridConstraints[] myConstraints;
    private final int myColumnCount;
    private final int myRowCount;
    final Dimension[] myPreferredSizes;
    final Dimension[] myMinimumSizes;

    public LayoutState(GridLayoutManager layout, boolean ignoreInvisibleComponents) {
        ArrayList<Component> componentsList = new ArrayList<Component>(layout.getComponentCount());
        ArrayList<GridConstraints> constraintsList = new ArrayList<GridConstraints>(layout.getComponentCount());
        int i = 0;
        while (i < layout.getComponentCount()) {
            Component component = layout.getComponent(i);
            if (!ignoreInvisibleComponents || component.isVisible()) {
                componentsList.add(component);
                GridConstraints constraints = layout.getConstraints(i);
                constraintsList.add(constraints);
            }
            ++i;
        }
        this.myComponents = componentsList.toArray(new Component[componentsList.size()]);
        this.myConstraints = constraintsList.toArray(new GridConstraints[constraintsList.size()]);
        this.myMinimumSizes = new Dimension[this.myComponents.length];
        this.myPreferredSizes = new Dimension[this.myComponents.length];
        this.myColumnCount = layout.getColumnCount();
        this.myRowCount = layout.getRowCount();
    }

    public int getComponentCount() {
        return this.myComponents.length;
    }

    public Component getComponent(int index) {
        return this.myComponents[index];
    }

    public GridConstraints getConstraints(int index) {
        return this.myConstraints[index];
    }

    public int getColumnCount() {
        return this.myColumnCount;
    }

    public int getRowCount() {
        return this.myRowCount;
    }
}


