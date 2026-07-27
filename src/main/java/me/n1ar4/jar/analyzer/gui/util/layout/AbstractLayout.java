/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import java.awt.*;

public abstract class AbstractLayout
        implements LayoutManager2 {
    public static final int DEFAULT_HGAP = 10;
    public static final int DEFAULT_VGAP = 5;
    protected Component[] myComponents = COMPONENT_EMPTY_ARRAY;
    protected GridConstraints[] myConstraints = GridConstraints.EMPTY_ARRAY;
    protected Insets myMargin = new Insets(0, 0, 0, 0);
    private int myHGap = -1;
    private int myVGap = -1;
    private static final Component[] COMPONENT_EMPTY_ARRAY = new Component[0];

    public final Insets getMargin() {
        return (Insets) this.myMargin.clone();
    }

    public final int getHGap() {
        return this.myHGap;
    }

    protected static int getHGapImpl(Container container) {
        if (container == null) {
            throw new IllegalArgumentException("container cannot be null");
        }
        while (container != null) {
            if (container.getLayout() instanceof AbstractLayout) {
                AbstractLayout layout = (AbstractLayout) container.getLayout();
                if (layout.getHGap() != -1) {
                    return layout.getHGap();
                }
            }
            container = container.getParent();
        }
        return DEFAULT_HGAP;
    }

    public final void setHGap(int hGap) {
        if (hGap < -1) {
            throw new IllegalArgumentException("wrong hGap: " + hGap);
        }
        this.myHGap = hGap;
    }

    public final int getVGap() {
        return this.myVGap;
    }

    protected static int getVGapImpl(Container container) {
        if (container == null) {
            throw new IllegalArgumentException("container cannot be null");
        }
        while (container != null) {
            if (container.getLayout() instanceof AbstractLayout) {
                AbstractLayout layout = (AbstractLayout) container.getLayout();
                if (layout.getVGap() != -1) {
                    return layout.getVGap();
                }
            }
            container = container.getParent();
        }
        return DEFAULT_VGAP;
    }

    public final void setVGap(int vGap) {
        if (vGap < -1) {
            throw new IllegalArgumentException("wrong vGap: " + vGap);
        }
        this.myVGap = vGap;
    }

    public final void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin cannot be null");
        }
        this.myMargin = (Insets) margin.clone();
    }

    final int getComponentCount() {
        return this.myComponents.length;
    }

    final Component getComponent(int index) {
        return this.myComponents[index];
    }

    final GridConstraints getConstraints(int index) {
        return this.myConstraints[index];
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        if (!(constraints instanceof GridConstraints)) {
            throw new IllegalArgumentException("constraints: " + constraints);
        }
        Component[] newComponents = new Component[this.myComponents.length + 1];
        System.arraycopy(this.myComponents, 0, newComponents, 0, this.myComponents.length);
        newComponents[this.myComponents.length] = comp;
        this.myComponents = newComponents;
        GridConstraints[] newConstraints = new GridConstraints[this.myConstraints.length + 1];
        System.arraycopy(this.myConstraints, 0, newConstraints, 0, this.myConstraints.length);
        newConstraints[this.myConstraints.length] = (GridConstraints) constraints;
        this.myConstraints = newConstraints;
    }

    public final void addLayoutComponent(String name, Component comp) {
        throw new UnsupportedOperationException();
    }

    public final void removeLayoutComponent(Component comp) {
        int i = this.getComponentIndex(comp);
        if (i == -1) {
            throw new IllegalArgumentException("component was not added: " + comp);
        }
        if (this.myComponents.length == 1) {
            this.myComponents = COMPONENT_EMPTY_ARRAY;
        } else {
            Component[] newComponents = new Component[this.myComponents.length - 1];
            System.arraycopy(this.myComponents, 0, newComponents, 0, i);
            System.arraycopy(this.myComponents, i + 1, newComponents, i, this.myComponents.length - i - 1);
            this.myComponents = newComponents;
        }
        if (this.myConstraints.length == 1) {
            this.myConstraints = GridConstraints.EMPTY_ARRAY;
        } else {
            GridConstraints[] newConstraints = new GridConstraints[this.myConstraints.length - 1];
            System.arraycopy(this.myConstraints, 0, newConstraints, 0, i);
            System.arraycopy(this.myConstraints, i + 1, newConstraints, i, this.myConstraints.length - i - 1);
            this.myConstraints = newConstraints;
        }
    }

    public GridConstraints getConstraintsForComponent(Component comp) {
        int i = this.getComponentIndex(comp);
        if (i == -1) {
            throw new IllegalArgumentException("component was not added: " + comp);
        }
        return this.myConstraints[i];
    }

    private int getComponentIndex(Component comp) {
        int i = 0;
        while (i < this.myComponents.length) {
            Component component = this.myComponents[i];
            if (component == comp) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public final float getLayoutAlignmentX(Container container) {
        return 0.5f;
    }

    public final float getLayoutAlignmentY(Container container) {
        return 0.5f;
    }

    public abstract Dimension maximumLayoutSize(Container var1);

    public abstract void invalidateLayout(Container var1);

    public abstract Dimension preferredLayoutSize(Container var1);

    public abstract Dimension minimumLayoutSize(Container var1);

    public abstract void layoutContainer(Container var1);
}


