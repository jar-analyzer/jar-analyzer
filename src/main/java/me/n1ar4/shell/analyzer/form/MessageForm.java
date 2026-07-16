/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.shell.analyzer.form;

import com.n1ar4.agent.dto.SourceResult;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
import me.n1ar4.shell.analyzer.model.InfoObj;

import javax.swing.*;
import java.awt.*;

public class MessageForm {
    private JPanel rootPanel;
    private JPanel msgPanel;
    private JTextField urlText;
    private JLabel urlLabel;
    private JLabel urlDescLabel;
    private JTextArea descArea;
    private JTextField hashText;
    private JScrollPane descScroll;
    private JLabel hashLabel;
    private JTextArea globalArea;
    private JLabel globalLabel;
    private JScrollPane globalScroll;

    private static final String urlInfoDescSplitTag = "\\^&\\*\\$#@";

    public MessageForm(InfoObj obj) {
        urlText.setText(obj.getUrl());
        hashText.setText(String.format("INT: %s HEX: %s", obj.getHash(),
                Integer.toHexString(Integer.parseInt(obj.getHash()))));
        String[] data = obj.getUrlDesc().split(urlInfoDescSplitTag);
        StringBuilder sb = new StringBuilder();
        for (String d : data) {
            if (d.startsWith(SourceResult.SourceResultTag)) {
                continue;
            }
            sb.append(d);
            sb.append("\n");
        }
        descArea.setText(sb.toString().trim());
        StringBuilder gb = new StringBuilder();
        for (String d : obj.getGlobalDesc()) {
            gb.append(d.trim());
            gb.append("\n");
        }
        globalArea.setText(gb.toString().trim());
    }

    public static void start0(InfoObj obj) {
        JFrame frame = new JFrame("message");
        frame.setContentPane(new MessageForm(obj).rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    {
        initializeComponents();
    }

    private void initializeComponents() {
        rootPanel = new JPanel();
        SwingLayout.configureGrid(rootPanel, 1, 1, new Insets(0, 0, 0, 0), -1, -1);
        msgPanel = new JPanel();
        SwingLayout.configureGrid(msgPanel, 5, 2, new Insets(0, 0, 0, 0), -1, -1);
        SwingLayout.add(rootPanel, msgPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        urlLabel = new JLabel();
        urlLabel.setText("URL");
        SwingLayout.add(msgPanel, urlLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(msgPanel, spacer1, 4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        urlText = new JTextField();
        urlText.setEditable(false);
        SwingLayout.add(msgPanel, urlText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, new Dimension(400, -1), null, null, 0);
        urlDescLabel = new JLabel();
        urlDescLabel.setText("DESC");
        SwingLayout.add(msgPanel, urlDescLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        descScroll = new JScrollPane();
        SwingLayout.add(msgPanel, descScroll, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        descArea = new JTextArea();
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descScroll.setViewportView(descArea);
        hashLabel = new JLabel();
        hashLabel.setText("HASH");
        SwingLayout.add(msgPanel, hashLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        hashText = new JTextField();
        hashText.setEditable(false);
        SwingLayout.add(msgPanel, hashText, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        globalLabel = new JLabel();
        globalLabel.setText("GLOBAL DESC");
        SwingLayout.add(msgPanel, globalLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        globalScroll = new JScrollPane();
        SwingLayout.add(msgPanel, globalScroll, 3, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        globalArea = new JTextArea();
        globalArea.setEditable(false);
        globalScroll.setViewportView(globalArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return rootPanel;
    }

}
