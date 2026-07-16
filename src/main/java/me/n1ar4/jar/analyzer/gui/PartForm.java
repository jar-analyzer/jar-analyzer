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

import me.n1ar4.jar.analyzer.core.DatabaseManager;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;

public class PartForm {
    private static final Logger logger = LogManager.getLogger();
    private JPanel rootPanel;
    private JTextField partVal;
    private JButton setButton;
    private static PartForm instance;

    public static void start() {
        JFrame frame = new JFrame(Const.PartForm);
        instance = new PartForm();
        instance.init();
        frame.setContentPane(instance.rootPanel);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
    }

    private void init() {
        logger.info("partition size: {}", DatabaseManager.PART_SIZE);
        instance.partVal.setText(String.valueOf(DatabaseManager.PART_SIZE));
        instance.setButton.addActionListener(e -> {
            DatabaseManager.PART_SIZE = Integer.parseInt(partVal.getText());
            JOptionPane.showMessageDialog(instance.rootPanel,
                    "set new partition value " + DatabaseManager.PART_SIZE);
        });
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 2, 2, new Insets(0, 0, 0, 0), -1, -1);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(rootPanel, spacer1, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        setButton = new JButton();
        setButton.setText("SET");
        SwingLayout.add(rootPanel, setButton, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        partVal = new JTextField();
        SwingLayout.add(rootPanel, partVal, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(300, -1), new Dimension(150, -1), null, 0);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
