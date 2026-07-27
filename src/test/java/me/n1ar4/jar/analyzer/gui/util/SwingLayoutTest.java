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

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwingLayoutTest {
    @Test
    void usesDesignerCompatibleDefaultGaps() {
        JPanel panel = new JPanel();
        SwingLayout.configureGrid(panel, 2, 2,
                new Insets(0, 0, 0, 0), -1, -1);

        addFixed(panel, new SizedComponent(20, 10), 0, 0);
        addFixed(panel, new SizedComponent(30, 10), 0, 1);
        addFixed(panel, new SizedComponent(20, 15), 1, 0);
        addFixed(panel, new SizedComponent(30, 15), 1, 1);

        assertEquals(new Dimension(60, 30), panel.getPreferredSize());
    }

    @Test
    void wantGrowReceivesSpaceBeforeCanGrow() {
        JPanel panel = new JPanel();
        SwingLayout.configureGrid(panel, 1, 2,
                new Insets(0, 0, 0, 0), 0, 0);
        JComponent canGrow = new SizedComponent(50, 20);
        JComponent wantGrow = new SizedComponent(50, 20);

        SwingLayout.add(panel, canGrow, 0, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_CAN_GROW,
                SwingLayout.SIZEPOLICY_FIXED,
                null, null, null, 0);
        SwingLayout.add(panel, wantGrow, 0, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_WANT_GROW,
                SwingLayout.SIZEPOLICY_FIXED,
                null, null, null, 0);

        panel.setSize(200, 20);
        panel.doLayout();

        assertEquals(50, canGrow.getWidth());
        assertEquals(150, wantGrow.getWidth());
    }

    @Test
    void sizeHintsDoNotOverwriteComponentSizes() {
        JPanel panel = new JPanel();
        SwingLayout.configureGrid(panel, 1, 1,
                new Insets(0, 0, 0, 0), 0, 0);
        JComponent child = new SizedComponent(
                new Dimension(250, 180),
                new Dimension(300, 200),
                new Dimension(500, 400));

        SwingLayout.add(panel, child, 0, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_CAN_GROW,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_CAN_GROW,
                null, new Dimension(100, 100), null, 0);

        assertEquals(new Dimension(300, 200), child.getPreferredSize());
        assertEquals(new Dimension(250, 180), panel.getPreferredSize());
    }

    @Test
    void enforcesConstraintMaximumSize() {
        JPanel panel = new JPanel();
        SwingLayout.configureGrid(panel, 1, 1,
                new Insets(0, 0, 0, 0), 0, 0);
        JComponent child = new SizedComponent(50, 20);

        SwingLayout.add(panel, child, 0, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_WANT_GROW,
                SwingLayout.SIZEPOLICY_CAN_SHRINK | SwingLayout.SIZEPOLICY_WANT_GROW,
                null, null, new Dimension(100, 40), 0);

        panel.setSize(300, 100);
        panel.doLayout();

        assertEquals(new Dimension(100, 40), child.getSize());
        assertEquals(new Point(100, 30), child.getLocation());
    }

    private static void addFixed(
            JPanel panel, JComponent child, int row, int column) {
        SwingLayout.add(panel, child, row, column, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                SwingLayout.SIZEPOLICY_FIXED,
                SwingLayout.SIZEPOLICY_FIXED,
                null, null, null, 0);
    }

    private static final class SizedComponent extends JComponent {
        private final Dimension minimum;
        private final Dimension preferred;
        private final Dimension maximum;

        private SizedComponent(int width, int height) {
            this(new Dimension(width, height),
                    new Dimension(width, height),
                    new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }

        private SizedComponent(
                Dimension minimum, Dimension preferred, Dimension maximum) {
            this.minimum = minimum;
            this.preferred = preferred;
            this.maximum = maximum;
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(minimum);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(preferred);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(maximum);
        }
    }
}
