/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.frame;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class FrameForm {

    private JPanel masterPanel;
    private JScrollPane rootScroll;
    private JTextArea frameArea;

    public static void start(boolean useFull) {
        JFrame frame = new JFrame(Const.FrameForm);
        FrameForm instance = new FrameForm();

        if (useFull) {
            StackFrameEngine engine = new StackFrameEngine();
            instance.frameArea.setText("please wait...");

            new Thread(() -> {
                String res = engine.doAnalyze(
                        MainForm.getCurMethod().getClassPath().toAbsolutePath().toString(),
                        MainForm.getCurMethod().getMethodName(),
                        MainForm.getCurMethod().getMethodDesc());
                instance.frameArea.setText(res);
                instance.frameArea.setCaretPosition(0);
            }).start();
        } else {
            SimpleStackFrameEngine engine = new SimpleStackFrameEngine();
            instance.frameArea.setText("please wait...");

            new Thread(() -> {
                String res = engine.doAnalyze(
                        MainForm.getCurMethod().getClassPath().toAbsolutePath().toString(),
                        MainForm.getCurMethod().getMethodName(),
                        MainForm.getCurMethod().getMethodDesc());
                instance.frameArea.setText(res);
                instance.frameArea.setCaretPosition(0);
            }).start();
        }

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
        rootScroll = new JScrollPane();
        SwingLayout.add(masterPanel, rootScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, new Dimension(800, 800), null, 0);
        frameArea = new JTextArea();
        frameArea.setEditable(false);
        Font frameAreaFont = this.resolveFont("Consolas", -1, -1, frameArea.getFont());
        if (frameAreaFont != null) frameArea.setFont(frameAreaFont);
        rootScroll.setViewportView(frameArea);
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
