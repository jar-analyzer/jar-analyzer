/*
 * Adapted from JetBrains IntelliJ IDEA UI Designer forms runtime 7.0.3.
 * Licensed under the Apache License, Version 2.0.
 */
package me.n1ar4.jar.analyzer.gui.util.layout;

import javax.swing.*;
import java.awt.*;

public class Spacer
        extends JComponent {
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    public final Dimension getPreferredSize() {
        return this.getMinimumSize();
    }
}


