/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.cfg;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class CFGForm {
    private JPanel masterPanel;
    private JPanel taintPanel;
    private JScrollPane taintScroll;
    private JTextArea resArea;

    public static void start() {
        JFrame frame = new JFrame(Const.CFGForm);
        CFGForm instance = new CFGForm();

        CFGEngine engine = new CFGEngine();
        String res = engine.doAnalyze(
                MainForm.getCurMethod().getClassPath().toAbsolutePath().toString(),
                MainForm.getCurMethod().getMethodName(),
                MainForm.getCurMethod().getMethodDesc());

        instance.resArea.setText(res);
        instance.resArea.setCaretPosition(0);

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
        taintPanel = new JPanel();
        SwingLayout.configureGrid(taintPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(masterPanel, taintPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        taintScroll = new JScrollPane();
        SwingLayout.add(taintPanel, taintScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, new Dimension(600, 400), null, 0);
        resArea = new JTextArea();
        resArea.setEditable(false);
        Font resAreaFont = this.resolveFont("Consolas", -1, -1, resArea.getFont());
        if (resAreaFont != null) resArea.setFont(resAreaFont);
        taintScroll.setViewportView(resArea);
    }

    /**
     * @noinspection ALL
     */
    private Font resolveFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

}
