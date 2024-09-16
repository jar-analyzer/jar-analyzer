/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.shell.analyzer.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.shell.analyzer.model.ClassObj;
import me.n1ar4.shell.analyzer.start.SocketHelper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShellForm {
    private static final String DEFAULT_PASSWD = "P4sSW0rD";

    class CommonMouse extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {
            JList<?> list = (JList<?>) evt.getSource();
            if (evt.getClickCount() == 2) {
                core(evt, list);
            }
        }
    }

    public static ShellForm instance;
    private JPanel shellPanel;
    private JPanel rootPanel;
    private JPanel topPanel;
    private JButton attachButton;
    private JList<ClassObj> filterList;
    private JList<ClassObj> listenerList;
    private JTabbedPane tabbedPane;
    private JPanel normalPanel;
    private JCheckBox ignoreApacheBox;
    private JTextArea blackArea;
    private JCheckBox ignoreJavaBox;
    private JButton analyzeButton;
    private JPanel filtersPane;
    private JPanel servletsPane;
    private JPanel listenerPane;
    private JPanel valvePane;
    private JPanel confPane;
    private JPanel confPanel;
    private JPanel blackPanel;
    private JScrollPane blackScroll;
    private JPanel analyzePanel;
    private JPanel checkPanel;
    private JPanel codePanel;
    private JPanel logPanel;
    private JScrollPane logScroll;
    private JTextArea logArea;
    private JScrollPane filterScroll;
    private JScrollPane servletScroll;
    private JList<ClassObj> servletList;
    private JScrollPane listenerScroll;
    private JScrollPane valveScroll;
    private JList<ClassObj> valveList;
    private JCheckBox ignoreSpringBox;
    private JLabel blackTip;
    private JButton refreshButton;
    private JTextField passText;
    private JLabel passLabel;
    private JButton genButton;
    private JTextField targetIPText;
    private JTextField targetPortText;
    private JLabel targetIPLabel;
    private JLabel targetPortLabel;
    private JTextArea cmdArea;
    private JPanel initPanel;
    private static final List<String> black = new ArrayList<>();

    private void analyze() {
        DefaultListModel<ClassObj> filtersModel = new DefaultListModel<>();
        DefaultListModel<ClassObj> listenersModel = new DefaultListModel<>();
        DefaultListModel<ClassObj> servletsModel = new DefaultListModel<>();
        DefaultListModel<ClassObj> valvesModel = new DefaultListModel<>();

        boolean igApache = ignoreApacheBox.isSelected();
        boolean igJava = ignoreJavaBox.isSelected();
        boolean igSpring = ignoreSpringBox.isSelected();

        String b = blackArea.getText();

        String[] t = b.split("\n");
        for (String s : t) {
            s = s.trim();
            if (s.endsWith("\r")) {
                s = s.substring(0, s.length() - 1);
            }
            black.add(s);
        }

        try {
            List<String> filters = SocketHelper.getAllFilters();
            for (String filter : filters) {
                if (igApache && filter.startsWith("org.apache")) {
                    continue;
                }
                if (igSpring && filter.startsWith("org.springframework")) {
                    continue;
                }
                if (igJava) {
                    if (filter.startsWith("java.")) {
                        continue;
                    }
                    if (filter.startsWith("javax.")) {
                        continue;
                    }
                    if (filter.startsWith("sun.")) {
                        continue;
                    }
                }
                boolean blackF = false;
                for (String s : black) {
                    if (filter.contains(s)) {
                        blackF = true;
                        break;
                    }
                }
                if (!blackF) {
                    continue;
                }
                ClassObj co = new ClassObj(filter, "FILTER");
                filtersModel.addElement(co);
            }
            filterList.setModel(filtersModel);
        } catch (Exception ex) {
            log("无法获得信息");
        }

        try {
            List<String> servlets = SocketHelper.getAllServlets();
            for (String servlet : servlets) {
                if (igApache && servlet.startsWith("org.apache")) {
                    continue;
                }
                if (igSpring && servlet.startsWith("org.springframework")) {
                    continue;
                }
                if (igJava) {
                    if (servlet.startsWith("java.")) {
                        continue;
                    }
                    if (servlet.startsWith("javax.")) {
                        continue;
                    }
                    if (servlet.startsWith("sun.")) {
                        continue;
                    }
                }
                boolean blackF = false;
                for (String s : black) {
                    if (servlet.contains(s)) {
                        blackF = true;
                        break;
                    }
                }
                if (!blackF) {
                    continue;
                }
                ClassObj co = new ClassObj(servlet, "SERVLET");
                servletsModel.addElement(co);
            }
            servletList.setModel(servletsModel);
        } catch (Exception ex) {
            log("无法获得信息");
        }

        try {
            List<String> listeners = SocketHelper.getAllListeners();
            for (String li : listeners) {
                if (igApache && li.startsWith("org.apache")) {
                    continue;
                }
                if (igSpring && li.startsWith("org.springframework")) {
                    continue;
                }
                if (igJava) {
                    if (li.startsWith("java.")) {
                        continue;
                    }
                    if (li.startsWith("javax.")) {
                        continue;
                    }
                    if (li.startsWith("sun.")) {
                        continue;
                    }
                }
                boolean blackF = false;
                for (String s : black) {
                    if (li.contains(s)) {
                        blackF = true;
                        break;
                    }
                }
                if (!blackF) {
                    continue;
                }
                ClassObj co = new ClassObj(li, "LISTENER");
                listenersModel.addElement(co);
            }
            listenerList.setModel(listenersModel);
        } catch (Exception ex) {
            log("无法获得信息");
        }

        try {
            List<String> valves = SocketHelper.getAllValves();
            for (String v : valves) {
                if (igApache && v.startsWith("org.apache")) {
                    continue;
                }
                if (igSpring && v.startsWith("org.springframework")) {
                    continue;
                }
                if (igJava) {
                    if (v.startsWith("java.")) {
                        continue;
                    }
                    if (v.startsWith("javax.")) {
                        continue;
                    }
                    if (v.startsWith("sun.")) {
                        continue;
                    }
                }
                boolean blackF = false;
                for (String s : black) {
                    if (v.contains(s)) {
                        blackF = true;
                        break;
                    }
                }
                if (!blackF) {
                    continue;
                }
                ClassObj co = new ClassObj(v, "VALVE");
                valvesModel.addElement(co);
            }
            valveList.setModel(valvesModel);
        } catch (Exception ex) {
            log("无法获得信息");
        }
    }

    private static RSyntaxTextArea codeArea;

    @SuppressWarnings("all")
    public ShellForm() {
        codeArea = new RSyntaxTextArea(100, 150);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(codeArea);
        codePanel.add(sp, new GridConstraints());

        genButton.addActionListener(e -> {
            String command = "-javaagent:agent.jar=port=%s;password=%s";
            command = String.format(command, targetPortText.getText(), passText.getText());

            String output = "1. 远程启动你的 JAVA 程序\n" +
                    "请在你的启动参数中添加 " + command + "\n" +
                    "2. 配置 IP 和 PASSWORD 信息后点击 CONNECT\n" +
                    "3. 如果没有自动显示信息可以尝试点击右侧的 刷新 按钮\n" +
                    "注意：在 Tomcat 中修改的是 catalina.bat/sh 参考弹出图片";

            cmdArea.setText(output);

            InputStream tomcatIs = ShellForm.class.getClassLoader().getResourceAsStream("img/tomcat.png");
            try {
                BufferedImage image = ImageIO.read(tomcatIs);
                JFrame frame = new JFrame("config");
                frame.setSize(882, 539);
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                frame.add(imageLabel);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            } catch (Exception ignored) {
            }
        });

        attachButton.addActionListener(e -> {
            String host = targetIPText.getText();
            String port = targetPortText.getText();
            String pass = passText.getText();
            if (pass.length() != 8) {
                JOptionPane.showMessageDialog(shellPanel, "请输入长度为 8 的密码");
                return;
            }

            SocketHelper.setHost(host);
            SocketHelper.setPort(port);
            SocketHelper.setPass(pass);

            ProcessDialog.createProgressDialog(rootPanel);
            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread() {
                @Override
                public void run() {
                    if (SocketHelper.check()) {
                        log("成功目标建立TCP连接");
                        dialog.dispose();
                        analyze();
                    } else {
                        log("无法与目标建立TCP连接");
                        dialog.dispose();
                        JOptionPane.showMessageDialog(rootPanel, "无法建立连接");
                    }
                }
            }.start();
        });


        filterList.addMouseListener(new CommonMouse());
        valveList.addMouseListener(new CommonMouse());
        listenerList.addMouseListener(new CommonMouse());
        servletList.addMouseListener(new CommonMouse());
        analyzeButton.addActionListener(e -> new Thread(this::analyze).start());
        refreshButton.addActionListener(e -> {
            String pass = passText.getText();
            if (pass.length() != 8) {
                JOptionPane.showMessageDialog(shellPanel, "请输入长度为8的密码");
                return;
            }
            SocketHelper.setPass(pass);
        });
    }

    public void core(MouseEvent evt, JList<?> list) {
        String pass = passText.getText();
        if (pass.length() != 8) {
            JOptionPane.showMessageDialog(shellPanel, "请输入密码");
        }
        SocketHelper.setPass(pass);

        int index = list.locationToIndex(evt.getPoint());
        ClassObj res = (ClassObj) list.getModel().getElementAt(index);

        log("尝试获取字节码进行反编译");

        new Thread(() -> {
            try {
                SocketHelper.getBytecode(res.getClassName());
            } catch (Exception ex) {
                log("无法连接目标");
            }
            String classPath = "test.class";
            String javaDir = ".";
            Path javaPathPath = Paths.get("test.java");
            if (!Files.exists(Paths.get(classPath))) {
                log("未知的错误");
                return;
            }
            try {
                Files.delete(javaPathPath);
            } catch (Exception ignored) {
            }
            String tips = "error";
            new Thread(() -> {
                String total;
                String[] args = new String[]{
                        classPath,
                        javaDir
                };
                try {
                    Files.delete(javaPathPath);
                } catch (IOException ignored) {
                }
                ConsoleDecompiler.main(args);
                try {
                    total = new String(Files.readAllBytes(javaPathPath));
                    if (total.trim().isEmpty()) {
                        total = tips;
                    } else {
                        total = "// FernFlower by Jar Analyzer V2\n" + total;
                    }
                } catch (Exception ignored) {
                    total = tips;
                }
                try {
                    Files.delete(javaPathPath);
                } catch (IOException ignored) {
                }

                total = total.replace("\r\n", "\n");
                codeArea.setText(total);
            }).start();
        }).start();
    }

    public static void log(String l) {
        String text = String.format("[*] %s\n", l);
        instance.logArea.append(text);
    }

    public static void start0() {
        JFrame frame = new JFrame("tomcat-analyzer by 4ra1n");
        instance = new ShellForm();

        instance.passText.setText(DEFAULT_PASSWD);
        instance.targetIPText.setText("127.0.0.1");
        instance.targetPortText.setText("10033");

        frame.setContentPane(instance.shellPanel);

        frame.pack();

        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());

        frame.setResizable(false);
        frame.setSize(1400, 800);

        frame.setVisible(true);
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
        shellPanel = new JPanel();
        shellPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 7, new Insets(0, 0, 0, 0), -1, -1));
        shellPanel.add(rootPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        topPanel = new JPanel();
        topPanel.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.setEnabled(false);
        rootPanel.add(topPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        topPanel.setBorder(BorderFactory.createTitledBorder(null, "START", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        attachButton = new JButton();
        attachButton.setText("CONNECT");
        topPanel.add(attachButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        initPanel = new JPanel();
        initPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(initPanel, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        passLabel = new JLabel();
        passLabel.setText("TOKEN");
        initPanel.add(passLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        genButton = new JButton();
        genButton.setText("GENERATE CMD");
        initPanel.add(genButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passText = new JTextField();
        initPanel.add(passText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        targetIPLabel = new JLabel();
        targetIPLabel.setText("TARGET IP");
        topPanel.add(targetIPLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetIPText = new JTextField();
        topPanel.add(targetIPText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        targetPortLabel = new JLabel();
        targetPortLabel.setText("PORT");
        topPanel.add(targetPortLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        targetPortText = new JTextField();
        topPanel.add(targetPortText, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        cmdArea = new JTextArea();
        cmdArea.setLineWrap(true);
        topPanel.add(cmdArea, new GridConstraints(2, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        normalPanel = new JPanel();
        normalPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(normalPanel, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(450, -1), null, new Dimension(450, -1), 0, false));
        normalPanel.setBorder(BorderFactory.createTitledBorder(null, "COMPONENTS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tabbedPane = new JTabbedPane();
        normalPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        filtersPane = new JPanel();
        filtersPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Filter", filtersPane);
        filtersPane.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        filterScroll = new JScrollPane();
        filtersPane.add(filterScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        filterList = new JList();
        filterScroll.setViewportView(filterList);
        servletsPane = new JPanel();
        servletsPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Servlet", servletsPane);
        servletsPane.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        servletScroll = new JScrollPane();
        servletsPane.add(servletScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        servletList = new JList();
        servletScroll.setViewportView(servletList);
        listenerPane = new JPanel();
        listenerPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Listener", listenerPane);
        listenerPane.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        listenerScroll = new JScrollPane();
        listenerPane.add(listenerScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listenerList = new JList();
        listenerScroll.setViewportView(listenerList);
        valvePane = new JPanel();
        valvePane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Valve", valvePane);
        valvePane.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        valveScroll = new JScrollPane();
        valvePane.add(valveScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        valveList = new JList();
        valveScroll.setViewportView(valveList);
        codePanel = new JPanel();
        codePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(codePanel, new GridConstraints(1, 1, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(500, 400), null, null, 0, false));
        codePanel.setBorder(BorderFactory.createTitledBorder(null, "DECOMPILE CODE", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logPanel = new JPanel();
        logPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(logPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        logPanel.setBorder(BorderFactory.createTitledBorder(null, "LOG", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logScroll = new JScrollPane();
        logPanel.add(logScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logArea = new JTextArea();
        logArea.setBackground(new Color(-14211289));
        logArea.setEditable(false);
        Font logAreaFont = this.$$$getFont$$$("Consolas", -1, 12, logArea.getFont());
        if (logAreaFont != null) logArea.setFont(logAreaFont);
        logArea.setForeground(new Color(-16711895));
        logScroll.setViewportView(logArea);
        confPane = new JPanel();
        confPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(confPane, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(350, -1), 0, false));
        confPane.setBorder(BorderFactory.createTitledBorder(null, "CONFIG", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        confPanel = new JPanel();
        confPanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        confPane.add(confPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        blackPanel = new JPanel();
        blackPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        confPanel.add(blackPanel, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 100), null, null, 0, false));
        blackPanel.setBorder(BorderFactory.createTitledBorder(null, "自定义黑名单", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        blackScroll = new JScrollPane();
        blackPanel.add(blackScroll, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        blackArea = new JTextArea();
        blackArea.setText("");
        blackScroll.setViewportView(blackArea);
        blackTip = new JLabel();
        blackTip.setText("每行一个（只显示包含黑名单字符串的类）");
        blackPanel.add(blackTip, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        analyzePanel = new JPanel();
        analyzePanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        confPanel.add(analyzePanel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        refreshButton = new JButton();
        refreshButton.setText("刷新");
        analyzePanel.add(refreshButton, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        analyzeButton = new JButton();
        analyzeButton.setText("开始分析");
        analyzePanel.add(analyzeButton, new GridConstraints(0, 0, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkPanel = new JPanel();
        checkPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        confPanel.add(checkPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ignoreApacheBox = new JCheckBox();
        ignoreApacheBox.setText("忽略org.apache开头的类");
        checkPanel.add(ignoreApacheBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ignoreJavaBox = new JCheckBox();
        ignoreJavaBox.setText("忽略java/javax/sun开头的类");
        checkPanel.add(ignoreJavaBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ignoreSpringBox = new JCheckBox();
        ignoreSpringBox.setText("忽略org.springframework开头的类");
        checkPanel.add(ignoreSpringBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
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
    public JComponent $$$getRootComponent$$$() {
        return shellPanel;
    }

}
