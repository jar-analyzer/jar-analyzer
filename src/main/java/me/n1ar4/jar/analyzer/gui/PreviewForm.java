/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class PreviewForm {
    private JPanel rootPanel;
    private JPanel codePanel;

    public PreviewForm(String code, int pos, boolean java) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(30, 60);
        if (java) {
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        } else {
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }

        if (OSUtil.isLinux()) {
            textArea.setFont(textArea.getFont().deriveFont(18.0f));
        }

        textArea.setCodeFoldingEnabled(true);
        textArea.setEnabled(true);
        textArea.setEditable(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        SwingLayout.add(codePanel, sp, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        SwingLayout.add(codePanel, sp, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        codePanel.repaint();

        textArea.setText(code);
        textArea.setCaretPosition(pos);
    }

    public static JFrame start(String code, int pos, boolean java) {
        JFrame frame = new JFrame("preview");
        frame.setUndecorated(true);
        frame.setContentPane(new PreviewForm(code, pos, java).rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        Rectangle screenBounds = null;

        for (GraphicsDevice screen : screens) {
            Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (bounds.contains(mouseLocation)) {
                screenBounds = bounds;
                break;
            }
        }

        if (screenBounds == null) {
            screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        }

        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();

        int posX = mouseLocation.x + 10;
        int posY = mouseLocation.y + 10;

        if (posX + windowWidth > screenBounds.x + screenBounds.width) {
            posX = mouseLocation.x - windowWidth - 10;
        }

        if (posY + windowHeight > screenBounds.y + screenBounds.height) {
            posY = mouseLocation.y - windowHeight - 10;
        }

        posX = Math.max(screenBounds.x, posX);
        posY = Math.max(screenBounds.y, posY);

        frame.setLocation(posX, posY);
        frame.setVisible(true);
        return frame;
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        codePanel = new JPanel();
        SwingLayout.configureGrid(codePanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(rootPanel, codePanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(600, 400), null, null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }
}
