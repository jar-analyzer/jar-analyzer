/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.obfuscate;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

public class ObfuscateForm {
    private JPanel mainPanel;
    private JButton clearBtn;
    private JButton executeBtn;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JScrollPane inputScroll;
    private JScrollPane outputScroll;

    public ObfuscateForm() {
        inputArea.setText("rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHD" +
                "FmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAA" +
                "AAAAB3CAAAABAAAAAAeA==");
        this.clearBtn.addActionListener(e -> {
            inputArea.setText(null);
            outputArea.setText(null);
        });
        this.executeBtn.addActionListener(e -> {
            String input = inputArea.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "input is null");
                return;
            }
            byte[] res;
            try {
                res = Base64.getDecoder().decode(input);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(mainPanel, "must use base64");
                return;
            }
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(res));
                Object obj = ois.readObject();
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                CustomObjectOutputStream oos = new CustomObjectOutputStream(bao);
                oos.writeObject(obj);
                oos.flush();
                oos.close();
                byte[] serializedData = bao.toByteArray();
                outputArea.setText(Base64.getEncoder().encodeToString(serializedData));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "obfuscate error: " + ex);
            }
        });
    }

    public static void start() {
        JFrame frame = new JFrame("ObfuscateForm");
        frame.setContentPane(new ObfuscateForm().mainPanel);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        mainPanel = new JPanel();
        SwingLayout.configureGrid(mainPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        final JPanel panel1 = new JPanel();
        SwingLayout.configureGrid(panel1, 3, 3, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(mainPanel, panel1, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        inputScroll = new JScrollPane();
        SwingLayout.add(panel1, inputScroll, 0, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 200), null, null, 0);
        inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputScroll.setViewportView(inputArea);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(panel1, spacer1, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        clearBtn = new JButton();
        clearBtn.setText("clean");
        SwingLayout.add(panel1, clearBtn, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        executeBtn = new JButton();
        executeBtn.setText("execute");
        SwingLayout.add(panel1, executeBtn, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        outputScroll = new JScrollPane();
        SwingLayout.add(panel1, outputScroll, 2, 0, 1, 3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, new Dimension(500, 200), null, null, 0);
        outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        outputScroll.setViewportView(outputArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return mainPanel;
    }

}
