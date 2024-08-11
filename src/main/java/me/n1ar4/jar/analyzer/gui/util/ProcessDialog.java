package me.n1ar4.jar.analyzer.gui.util;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("all")
public class ProcessDialog {
    public static JDialog createProgressDialog(JPanel master) {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JLabel label = new JLabel("running please wait ...");

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 10, 0);
        contentPanel.add(progressBar, gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        contentPanel.add(label, gbc);

        JOptionPane optionPane = new JOptionPane(contentPanel, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, IconManager.showIcon, new Object[]{}, null);

        JDialog dialog = new JDialog();
        dialog.setTitle("Jar Analyzer");
        dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(master);
        return dialog;
    }
}
