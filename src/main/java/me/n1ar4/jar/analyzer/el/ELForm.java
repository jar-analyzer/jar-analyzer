package me.n1ar4.jar.analyzer.el;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.engine.CoreHelper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    private static ELForm elInstance;

    public static void setVal(int val) {
        elInstance.elProcess.setValue(val);
    }

    public ELForm() {
        RSyntaxTextArea textArea = new RSyntaxTextArea(30, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setEnabled(true);
        textArea.setEditable(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        elCodePanel.add(sp, new GridConstraints());
        setELCodeArea(textArea);
        elCodePanel.repaint();

        jTextArea.setText("#method\n" +
                "        .startWith(\"set\")\n" +
                "        .endWith(\"value\")\n" +
                "        .nameContains(\"lookup\")\n" +
                "        .classNameContains(\"Context\")\n" +
                "        .returnType(\"java.lang.Process\")\n" +
                "        .paramTypeMap(0,\"java.lang.String\")\n" +
                "        .paramsNum(1)\n" +
                "        .isStatic(false)\n" +
                "        .isSubClassOf(\"java.awt.Component\")\n" +
                "        .isSuperClassOf(\"com.test.SomeClass\")\n" +
                "        .hasClassAnno(\"Controller\")\n" +
                "        .hasAnno(\"RequestMapping\")\n" +
                "        .hasField(\"context\")");

        checkButton.addActionListener(e -> {
            try {
                ExpressionParser parser = new SpelExpressionParser();
                String spel = jTextArea.getText();
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
                int cpuCoreCount = Runtime.getRuntime().availableProcessors();
                int maximumPoolSize = cpuCoreCount * 2;
                logger.info("pool size: {}", maximumPoolSize);
                long keepAliveTime = 1;
                TimeUnit timeUnit = TimeUnit.MINUTES;
                BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(50);
                ThreadFactory threadFactory = Executors.defaultThreadFactory();
                RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
                ThreadPoolExecutor executor = new ThreadPoolExecutor(
                        maximumPoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        timeUnit,
                        workQueue,
                        threadFactory,
                        handler
                );

                MethodEL condition = (MethodEL) value;
                ConcurrentLinkedQueue<ResObj> searchList = new ConcurrentLinkedQueue<>();
                List<MethodReference> mrs = MainForm.getEngine().getAllMethodRef();

                logger.info("get all methods info finish");
                logger.info("methods length: {}", mrs.size());

                int part = mrs.size() / 80;
                ELForm.setVal(10);
                int index = 0;
                int tempVal = 11;
                for (MethodReference mr : mrs) {
                    index++;
                    if (part == 0) {
                        part = 1;
                    }
                    if (index % part == 0) {
                        if (tempVal < 90) {
                            ELForm.setVal(tempVal++);
                        }
                    }
                    ClassReference.Handle ch = mr.getClassReference();
                    MethodELProcessor processor = new MethodELProcessor(ch, mr, searchList, condition);
                    executor.submit(processor::process);
                }
                executor.shutdown();
                try {
                    // 超时 30 秒
                    if (executor.awaitTermination(30, TimeUnit.SECONDS)) {
                        if (searchList.isEmpty()) {
                            setVal(100);
                            searchButton.setEnabled(true);
                            JOptionPane.showMessageDialog(this.jTextArea, "没有找到结果");
                            return;
                        } else {
                            JOptionPane.showMessageDialog(this.jTextArea, "搜索成功：找到符合表达式的方法");
                        }
                        setVal(95);
                        ArrayList<ResObj> resObjList = new ArrayList<>();
                        Object[] array = searchList.toArray();
                        for (Object o : array) {
                            resObjList.add((ResObj) o);
                        }
                        new Thread(() -> CoreHelper.refreshMethods(resObjList)).start();
                        setVal(100);
                        return;
                    }
                } catch (InterruptedException ignored) {
                }
                JOptionPane.showMessageDialog(this.jTextArea, "没有找到结果");
            } else {
                JOptionPane.showMessageDialog(this.jTextArea, "错误的表达式");
            }
            ELForm.setVal(100);
            searchButton.setEnabled(true);
            logger.info("el process finish");
        }).start());

        elInstance = this;
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
        opPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        elPanel.add(opPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        checkButton = new JButton();
        checkButton.setText("验证表达式");
        opPanel.add(checkButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchButton = new JButton();
        searchButton.setText("使用该表达式搜索");
        opPanel.add(searchButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editScroll = new JScrollPane();
        elPanel.add(editScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(500, 300), null, null, 0, false));
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
