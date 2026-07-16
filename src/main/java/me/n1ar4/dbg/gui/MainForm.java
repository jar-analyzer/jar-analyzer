/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.dbg.gui;

import me.n1ar4.dbg.core.DBGRunner;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MainForm {
    private JPanel masterPanel;
    private JTabbedPane tabbedPanel;
    private JPanel mainPanel;
    private JPanel javaBytecodePanel;
    private JPanel threadStackPanel;
    private JPanel localVariablesPanel;
    private JScrollPane javaBytecodeScroll;
    private JScrollPane threadStackScroll;
    private JScrollPane localVariablesScroll;
    private JTable bytecodeTable;
    private JTable threadStackTable;
    private JTable localVariablesTable;
    private JPanel logPanel;
    private JTextArea logArea;
    private JScrollPane logScroll;
    private JPanel curPanel;
    private JTextField breakClassText;
    private JButton setBreakpointButton;
    private JLabel curClassLabel;
    private JLabel curlMethodLabel;
    private JLabel breakClassLabel;
    private JLabel curClassText;
    private JLabel curMethodText;
    private JTextField breakMethodText;
    private JLabel breakMethodLabel;
    private JButton deleteBreakpointButton;
    private JPanel deleteBreakPanel;
    private JButton outButton;
    private JButton overButton;
    private JButton intoButton;
    private JButton runButton;
    private JPanel actionPanel;
    private static MainForm instance;
    private static DBGRunner runner;

    public static MainForm getInstance() {
        return instance;
    }

    public JPanel getMasterPanel() {
        return masterPanel;
    }

    public JTable getBytecodeTable() {
        return bytecodeTable;
    }

    public JTable getThreadStackTable() {
        return threadStackTable;
    }

    public JTable getLocalVariablesTable() {
        return localVariablesTable;
    }

    public static DBGRunner getRunner() {
        return runner;
    }

    public static void setRunner(DBGRunner runner) {
        MainForm.runner = runner;
    }

    public JTextField getBreakClassText() {
        return breakClassText;
    }

    public JLabel getCurClassText() {
        return curClassText;
    }

    public JLabel getCurMethodText() {
        return curMethodText;
    }

    public JTextField getBreakMethodText() {
        return breakMethodText;
    }

    public static void doStart() {
        runner.doClassPrepare();
        runner.start();
    }

    public static void start() {
        JFrame frame = new JFrame("java-dbg - 4ra1n");

        instance = new MainForm();
        instance.init();

        frame.setJMenuBar(MenuUtil.createMenuBar());
        frame.setContentPane(instance.masterPanel);

        frame.pack();

        frame.setLocationRelativeTo(me.n1ar4.jar.analyzer.gui.MainForm.getInstance().getMasterPanel());

        frame.setResizable(true);
        frame.setVisible(true);
    }

    private void init() {
        logArea.setText(null);
        logArea.setCaretPosition(0);
        instance.getCurClassText().setText("NO CLASS NOW");
        instance.getCurMethodText().setText("NO METHOD NOW");
        instance.setBreakpointButton.addActionListener(e -> {
            MainForm.getRunner().doBreakpoint(
                    instance.getBreakClassText().getText(),
                    instance.getBreakMethodText().getText()
            );
        });
        instance.runButton.addActionListener(e -> {
            MainForm.getRunner().run();
        });
        instance.overButton.addActionListener(e -> {
            MainForm.getRunner().doStepOver();
        });
        instance.intoButton.addActionListener(e -> {
            MainForm.getRunner().doStepInto();
        });
        instance.outButton.addActionListener(e -> {
            MainForm.getRunner().doStepOut();
        });
        TableManager.setBytecodeTable();
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        masterPanel = new JPanel();
        SwingLayout.configureGrid(masterPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        tabbedPanel = new JTabbedPane();
        SwingLayout.add(masterPanel, tabbedPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, new Dimension(200, 200), null, 0);
        mainPanel = new JPanel();
        SwingLayout.configureGrid(mainPanel, 3, 2, new Insets(3, 3, 3, 3), -1, -1);
        tabbedPanel.addTab("main", mainPanel);
        javaBytecodePanel = new JPanel();
        SwingLayout.configureGrid(javaBytecodePanel, 2, 1, new Insets(3, 3, 3, 3), -1, -1);
        SwingLayout.add(mainPanel, javaBytecodePanel, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(600, 500), null, null, 0);
        javaBytecodePanel.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        javaBytecodeScroll = new JScrollPane();
        SwingLayout.add(javaBytecodePanel, javaBytecodeScroll, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        bytecodeTable = new JTable();
        javaBytecodeScroll.setViewportView(bytecodeTable);
        curPanel = new JPanel();
        SwingLayout.configureGrid(curPanel, 5, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(javaBytecodePanel, curPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        curClassLabel = new JLabel();
        curClassLabel.setText("Current Class");
        SwingLayout.add(curPanel, curClassLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        curlMethodLabel = new JLabel();
        curlMethodLabel.setText("Current Method");
        SwingLayout.add(curPanel, curlMethodLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        breakClassLabel = new JLabel();
        breakClassLabel.setText("Break Class");
        SwingLayout.add(curPanel, breakClassLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        breakClassText = new JTextField();
        SwingLayout.add(curPanel, breakClassText, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        curClassText = new JLabel();
        curClassText.setText("");
        SwingLayout.add(curPanel, curClassText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        curMethodText = new JLabel();
        curMethodText.setText("");
        SwingLayout.add(curPanel, curMethodText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        deleteBreakPanel = new JPanel();
        SwingLayout.configureGrid(deleteBreakPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(curPanel, deleteBreakPanel, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        breakMethodText = new JTextField();
        SwingLayout.add(deleteBreakPanel, breakMethodText, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        breakMethodLabel = new JLabel();
        breakMethodLabel.setText("Break Method");
        SwingLayout.add(curPanel, breakMethodLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        final JPanel panel1 = new JPanel();
        SwingLayout.configureGrid(panel1, 1, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(curPanel, panel1, 4, 0, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        deleteBreakpointButton = new JButton();
        deleteBreakpointButton.setText("Delete Breakpoint");
        SwingLayout.add(panel1, deleteBreakpointButton, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        setBreakpointButton = new JButton();
        setBreakpointButton.setText("Set Breakpoint");
        SwingLayout.add(panel1, setBreakpointButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        actionPanel = new JPanel();
        SwingLayout.configureGrid(actionPanel, 1, 4, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(mainPanel, actionPanel, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        actionPanel.setBorder(BorderFactory.createTitledBorder(null, "Action", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        overButton = new JButton();
        overButton.setText("Over");
        SwingLayout.add(actionPanel, overButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        runButton = new JButton();
        runButton.setText("Run");
        SwingLayout.add(actionPanel, runButton, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        intoButton = new JButton();
        intoButton.setText("Into");
        SwingLayout.add(actionPanel, intoButton, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        outButton = new JButton();
        outButton.setText("Out");
        SwingLayout.add(actionPanel, outButton, 0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        threadStackPanel = new JPanel();
        SwingLayout.configureGrid(threadStackPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        SwingLayout.add(mainPanel, threadStackPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(-1, 300), null, null, 0);
        threadStackPanel.setBorder(BorderFactory.createTitledBorder(null, "Thread Stack", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        threadStackScroll = new JScrollPane();
        SwingLayout.add(threadStackPanel, threadStackScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        threadStackTable = new JTable();
        threadStackScroll.setViewportView(threadStackTable);
        localVariablesPanel = new JPanel();
        SwingLayout.configureGrid(localVariablesPanel, 1, 1, new Insets(3, 3, 3, 3), -1, -1);
        SwingLayout.add(mainPanel, localVariablesPanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(400, 500), null, null, 0);
        localVariablesPanel.setBorder(BorderFactory.createTitledBorder(null, "Local Variables", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        localVariablesScroll = new JScrollPane();
        SwingLayout.add(localVariablesPanel, localVariablesScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        localVariablesTable = new JTable();
        localVariablesScroll.setViewportView(localVariablesTable);
        logPanel = new JPanel();
        SwingLayout.configureGrid(logPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(mainPanel, logPanel, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        logPanel.setBorder(BorderFactory.createTitledBorder(null, "Log", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logScroll = new JScrollPane();
        SwingLayout.add(logPanel, logScroll, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setText("");
        logScroll.setViewportView(logArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

}
