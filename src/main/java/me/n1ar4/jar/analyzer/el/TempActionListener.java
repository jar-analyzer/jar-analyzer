/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TempActionListener implements ActionListener {
    private final JTextArea textArea;
    private final JComboBox<String> tempCombo;

    public TempActionListener(JComboBox<String> tempCombo, JTextArea testArea) {
        this.textArea = testArea;
        this.tempCombo = tempCombo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String obj = (String) tempCombo.getSelectedItem();
        textArea.setText(Templates.data.get(obj));
    }
}
