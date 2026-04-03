/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el.ui;

import me.n1ar4.jar.analyzer.el.TempActionListener;
import me.n1ar4.jar.analyzer.el.Templates;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Set;

public class ELPanel extends JPanel {

    private final RSyntaxTextArea codeArea;
    private final JButton checkButton;
    private final JButton searchButton;
    private final JButton stopBtn;
    private final JButton exportBtn;
    private final JProgressBar progressBar;
    private final JLabel msgLabel;
    private final JComboBox<String> templateCombo;

    public ELPanel() {
        setLayout(new BorderLayout(0, 4));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        // ======== 代码编辑区 ========
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SPEL Expression",
                TitledBorder.LEFT, TitledBorder.TOP));

        codeArea = new RSyntaxTextArea(18, 80);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        codeArea.setAntiAliasingEnabled(true);
        codeArea.setAutoIndentEnabled(true);
        codeArea.setTabSize(4);
        codeArea.setFont(codeArea.getFont().deriveFont(MainForm.FONT_SIZE));

        codeArea.setText("// SpEL 表达式搜索\n" +
                "// 支持 // 单行注释\n" +
                "// 所有条件之间是 AND 关系\n" +
                "#method\n" +
                "        .nameContains(\"lookup\")\n" +
                "        .classNameContains(\"Context\")\n" +
                "        .isPublic(true)");

        RTextScrollPane editorScroll = new RTextScrollPane(codeArea);
        editorScroll.setPreferredSize(new Dimension(-1, 350));
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        add(editorPanel, BorderLayout.CENTER);

        // 安装自动补全
        new ELAutoCompleteProvider(codeArea);

        // ======== 进度条 ========
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);

        // ======== 底部操作面板 ========
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));

        // 按钮行
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 6, 0));
        checkButton = new JButton("验证表达式");
        searchButton = new JButton("使用该表达式搜索");
        stopBtn = new JButton("强行停止");
        exportBtn = new JButton("导出 CSV");
        btnPanel.add(checkButton);
        btnPanel.add(searchButton);
        btnPanel.add(stopBtn);
        btnPanel.add(exportBtn);
        bottomPanel.add(btnPanel, BorderLayout.NORTH);

        // 模板 + 消息行
        JPanel infoPanel = new JPanel(new BorderLayout(6, 4));

        // 消息标签
        msgLabel = new JLabel("no message");
        msgLabel.setFont(msgLabel.getFont().deriveFont(Font.PLAIN, 12f));
        infoPanel.add(msgLabel, BorderLayout.NORTH);

        // 模板选择
        JPanel templatePanel = new JPanel(new BorderLayout(6, 0));
        templatePanel.add(new JLabel("内置语法:"), BorderLayout.WEST);
        templateCombo = new JComboBox<>();
        Set<String> keys = Templates.data.keySet();
        for (String key : keys) {
            templateCombo.addItem(key);
        }
        templateCombo.addActionListener(new TempActionListener(templateCombo, codeArea));
        templatePanel.add(templateCombo, BorderLayout.CENTER);
        infoPanel.add(templatePanel, BorderLayout.CENTER);

        bottomPanel.add(infoPanel, BorderLayout.CENTER);

        // 将底部面板放在进度条之上
        JPanel southPanel = new JPanel(new BorderLayout(0, 4));
        southPanel.add(bottomPanel, BorderLayout.CENTER);
        southPanel.add(progressBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    // ======== Getter methods ========

    public RSyntaxTextArea getCodeArea() {
        return codeArea;
    }

    public JButton getCheckButton() {
        return checkButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getStopBtn() {
        return stopBtn;
    }

    public JButton getExportBtn() {
        return exportBtn;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getMsgLabel() {
        return msgLabel;
    }
}
