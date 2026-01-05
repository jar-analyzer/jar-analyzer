/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ELForm {
    private static final Logger logger = LogManager.getLogger();
    public JPanel elPanel;
    private JTextArea jTextArea;
    private JButton checkButton;
    private JButton searchButton;
    private JPanel opPanel;
    private JScrollPane editScroll;
    private JPanel elCodePanel;
    private JProgressBar elProcess;
    private JComboBox<String> tempCombo;
    private JPanel templatesPanel;
    private JLabel builtinLabel;
    private JLabel msgLabel;
    private JButton stopBtn;
    private static ELForm elInstance;

    public static void setVal(int val) {
        SwingUtilities.invokeLater(() -> elInstance.elProcess.setValue(val));
    }

    public static String removeComments(String code) {
        return code.replaceAll("(?m)^\\s*//.*$", "");
    }

    public ELForm() {
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);

        textArea.setFont(textArea.getFont().deriveFont(MainForm.FONT_SIZE));

        textArea.setCodeFoldingEnabled(true);
        textArea.setEnabled(true);
        textArea.setEditable(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        elCodePanel.add(sp, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        setELCodeArea(textArea);
        elCodePanel.repaint();

        jTextArea.setText("#method\n" +
                "        .startWith(\"set\")\n" +
                "        .endWith(\"value\")\n" +
                "        .nameContains(\"lookup\")\n" +
                "        .nameNotContains(\"internal\")\n" +
                "        .classNameContains(\"Context\")\n" +
                "        .classNameNotContains(\"Abstract\")\n" +
                "        .returnType(\"java.lang.Process\")\n" +
                "        .paramTypeMap(0,\"java.lang.String\")\n" +
                "        .paramsNum(1)\n" +
                "        .isStatic(false)\n" +
                "        .isPublic(true)\n" +
                "        .isSubClassOf(\"java.awt.Component\")\n" +
                "        .isSuperClassOf(\"com.test.SomeClass\")\n" +
                "        .hasClassAnno(\"Controller\")\n" +
                "        .hasAnno(\"RequestMapping\")\n" +
                "        .excludeAnno(\"Auth\")\n" +
                "        .hasField(\"context\")");

        checkButton.addActionListener(e -> {
            try {
                ExpressionParser parser = new SpelExpressionParser();
                String spel = jTextArea.getText();
                spel = removeComments(spel);
                parser.parseExpression(spel);
                JOptionPane.showMessageDialog(this.jTextArea, "解析通过，正确的表达式");
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(this.jTextArea, "解析异常，错误的表达式");
            }
        });

        searchButton.addActionListener(e -> new Thread(() -> {
            logger.info("start el process");

            // 2024/07/02 FIX BUG
            if (MainForm.getEngine() == null) {
                logger.warn("engine is null");
                ELForm.setVal(0);
                return;
            }
            if (!MainForm.getEngine().isEnabled()) {
                logger.warn("engine is not enabled");
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "engine is not enabled");
                ELForm.setVal(0);
                return;
            }

            searchButton.setEnabled(false);
            ELForm.setVal(0);

            ExpressionParser parser = new SpelExpressionParser();
            String spel = jTextArea.getText();
            spel = removeComments(spel);

            Object value;
            try {
                MethodEL m = new MethodEL();
                Expression exp = parser.parseExpression(spel);
                StandardEvaluationContext ctx = new StandardEvaluationContext();
                ctx.setVariable("method", m);
                value = exp.getValue(ctx);
            } catch (Exception ex) {
                ELForm.setVal(100);
                searchButton.setEnabled(true);
                JOptionPane.showMessageDialog(this.jTextArea, "语法错误");
                return;
            }

            ELForm.setVal(3);
            logger.info("parse el success");

            if (value instanceof MethodEL) {
                // 启动表达式搜索
                ELSearchEngine engine = new ELSearchEngine(value, msgLabel, searchButton, stopBtn, jTextArea);
                engine.run();
                return;
            } else {
                JOptionPane.showMessageDialog(this.jTextArea, "错误的表达式");
            }
            ELForm.setVal(100);
            searchButton.setEnabled(true);
            logger.info("el process finish");
        }).start());

        elInstance = this;

        Set<String> keys = Templates.data.keySet();
        for (String key : keys) {
            tempCombo.addItem(key);
        }
        tempCombo.addActionListener(new TempActionListener(tempCombo, jTextArea));
    }

    private void setELCodeArea(RSyntaxTextArea textArea) {
        jTextArea = textArea;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        elPanel = new JPanel();
        elPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        opPanel = new JPanel();
        opPanel.setLayout(new GridLayoutManager(2, 3, new Insets(3, 3, 3, 3), -1, -1));
        elPanel.add(opPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        checkButton = new JButton();
        checkButton.setText("验证表达式");
        opPanel.add(checkButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchButton = new JButton();
        searchButton.setText("使用该表达式搜索");
        opPanel.add(searchButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        templatesPanel = new JPanel();
        templatesPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        opPanel.add(templatesPanel, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tempCombo = new JComboBox();
        templatesPanel.add(tempCombo, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        builtinLabel = new JLabel();
        builtinLabel.setText("内置语法");
        templatesPanel.add(builtinLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        msgLabel = new JLabel();
        msgLabel.setText("no message");
        templatesPanel.add(msgLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        stopBtn = new JButton();
        stopBtn.setText("强行停止");
        opPanel.add(stopBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editScroll = new JScrollPane();
        elPanel.add(editScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(500, 350), null, null, 0, false));
        elCodePanel = new JPanel();
        elCodePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        editScroll.setViewportView(elCodePanel);
        elProcess = new JProgressBar();
        elProcess.setStringPainted(true);
        elPanel.add(elProcess, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return elPanel;
    }

}
