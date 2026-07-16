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
import me.n1ar4.jar.analyzer.gui.util.SyntaxAreaHelper;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.awt.*;

public class OpcodeForm {
    private JPanel masterPanel;
    private static JTextArea codeArea;

    public static void setCodeArea(JTextArea codeArea) {
        OpcodeForm.codeArea = codeArea;
    }

    public static void start(String code) {
        JFrame frame = new JFrame(Const.OpcodeForm);
        OpcodeForm instance = new OpcodeForm();

        SyntaxAreaHelper.buildJavaOpcode(instance.masterPanel);
        codeArea.setText(code);
        codeArea.setCaretPosition(0);

        codeArea.setPreferredSize(new Dimension(900, 600));
        codeArea.setMaximumSize(new Dimension(900, 600));
        codeArea.setMinimumSize(new Dimension(900, 600));

        instance.masterPanel.setPreferredSize(new Dimension(900, 600));
        instance.masterPanel.setMaximumSize(new Dimension(900, 600));
        instance.masterPanel.setMinimumSize(new Dimension(900, 600));

        frame.setContentPane(instance.masterPanel);
        frame.setResizable(true);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        masterPanel = new JPanel();
        SwingLayout.configureGrid(masterPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

}
