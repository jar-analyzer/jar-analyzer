package me.n1ar4.shell.analyzer.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Version;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.jar.analyzer.utils.SocketUtil;
import me.n1ar4.shell.analyzer.model.ClassObj;
import me.n1ar4.shell.analyzer.model.ProcessObj;
import me.n1ar4.shell.analyzer.start.SocketHelper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ShellForm {
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
    private JButton runningButton;
    private JTextField pidText;
    private JButton attachButton;
    private JList<ClassObj> filterList;
    private JList<ClassObj> listenerList;
    private JTabbedPane tabbedPane;
    private JPanel normalPanel;
    private JCheckBox ignoreApacheBox;
    private JTextArea blackArea;
    private JCheckBox ignoreJavaBox;
    private JButton analyzeButton;
    private JScrollPane processScroll;
    private JLabel pidLabel;
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
    private JTable processTable;
    private JCheckBox ignoreSpringBox;
    private JLabel blackTip;
    private JButton killButton;
    private JTextField killText;
    private JButton refreshButton;
    private JTextField passText;
    private JLabel passLabel;
    private JButton genButton;
    private static final List<String> black = new ArrayList<>();
    private static final String[] columns = new String[]{"PID", "Name"};
    private static String[][] rows = new String[0][0];
    private static final List<ClassObj> coList = new ArrayList<>();

    private static DefaultTableModel model = new DefaultTableModel(rows, columns) {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private void analyze() {
        String host = "127.0.0.1";
        String pass = passText.getText();
        if (pass.length() != 8) {
            JOptionPane.showMessageDialog(shellPanel, "请输入密码");
        }

        SocketHelper.setHost(host);
        SocketHelper.setPass(pass);

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
                coList.add(co);
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
                coList.add(co);
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
                coList.add(co);
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
                coList.add(co);
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
        processTable.setModel(model);
        processTable.getColumnModel().getColumn(0).setMaxWidth(100);

        genButton.addActionListener(e -> {
            int length = 8;
            String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            random.setSeed(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                int number = random.nextInt(62);
                sb.append(str.charAt(number));
            }
            passText.setText(sb.toString());
        });

        this.processTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int row = processTable.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    String pid = rows[row][0];
                    pidText.setText(pid);
                }
            }
        });

        runningButton.addActionListener(e -> {
            List<List<String>> dataList = new ArrayList<>();
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            for (VirtualMachineDescriptor v : list) {
                ProcessObj p = new ProcessObj();
                p.setId(v.id());

                String t = v.displayName();
                if (t == null || t.equals("")) {
                    continue;
                }

                if (!t.toLowerCase().endsWith(".jar")) {
                    String[] s = t.split("\\.");
                    t = s[s.length - 1];

                    if (t.contains("/")) {
                        s = t.split("/");
                        t = s[s.length - 1];
                    }
                }

                p.setName(t);
                List<String> temp = new ArrayList<>();
                temp.add(p.getId());
                temp.add(p.getName());
                dataList.add(temp);
            }
            String[][] z = new String[dataList.size()][];
            for (int i = 0; i < dataList.size(); i++) {
                String[] a = dataList.get(i).toArray(new String[0]);
                z[i] = a;
            }

            log("当前运行的Java进程数量: " + z.length);

            rows = z;
            model = new DefaultTableModel(rows, columns) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            processTable.setModel(model);
            processTable.getColumnModel().getColumn(0).setMaxWidth(100);
        });
        attachButton.addActionListener(e -> {
            String pid = pidText.getText();
            String pass = passText.getText();
            if (pass.length() != 8) {
                JOptionPane.showMessageDialog(shellPanel, "请输入长度为8的密码");
                return;
            }
            SocketHelper.setPass(pass);
            log("开始Attach到目标: " + pid);
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                log("正在加载Agent程序...");

                Path agentPath = Paths.get("lib").resolve(
                        Paths.get("agent.jar"));
                Path agentDepPath = Paths.get("lib").resolve(
                        Paths.get("agent-jar-with-dependencies.jar"));

                String path;
                if (Files.exists(agentPath)) {
                    path = agentPath.toAbsolutePath().toString();
                } else if (Files.exists(agentDepPath)) {
                    path = agentDepPath.toAbsolutePath().toString();
                } else {
                    log("请检查当前目录的agent文件");
                    return;
                }
                log("加载Agent: " + path);
                vm.loadAgent(path, pass);
                vm.detach();
                log("加载Agent程序完成");

                if (SocketHelper.check()) {
                    log("成功目标建立TCP连接");
                } else {
                    log("无法与目标建立TCP连接");
                }

            } catch (Exception ignored) {
                new Thread(this::analyze).start();
            }
        });
        filterList.addMouseListener(new CommonMouse());
        valveList.addMouseListener(new CommonMouse());
        listenerList.addMouseListener(new CommonMouse());
        servletList.addMouseListener(new CommonMouse());
        analyzeButton.addActionListener(e -> new Thread(this::analyze).start());
        killButton.addActionListener(e -> {
            String kill = killText.getText();

            ClassObj co = null;
            for (ClassObj o : coList) {
                if (o.getClassName().equals(kill)) {
                    co = o;
                }
            }

            if (co == null) {
                JOptionPane.showMessageDialog(shellPanel, "不存在该类");
                return;
            }

            ClassObj finalCo = co;

            if (kill.startsWith("org.springframework")) {
                int i = JOptionPane.showConfirmDialog(shellPanel, "确定要修改Spring的类？");
                if (i != 0) {
                    return;
                }
            }
            if (kill.startsWith("org.apache")) {
                int i = JOptionPane.showConfirmDialog(shellPanel, "确定要修改Apache的类？");
                if (i != 0) {
                    return;
                }
            }
            if (kill.startsWith("java.") || kill.startsWith("javax.") ||
                    kill.startsWith("sun.") || kill.startsWith("com.sun.")) {
                int i = JOptionPane.showConfirmDialog(shellPanel, "确定要修改JDK的类？");
                if (i != 0) {
                    return;
                }
            }

            new Thread(() -> {
                try {
                    if (finalCo.getType().equals("FILTER")) {
                        SocketHelper.killFilter(kill);
                    }
                    if (finalCo.getType().equals("SERVLET")) {
                        SocketHelper.killServlet(kill);
                    }
                    if (finalCo.getType().equals("LISTENER")) {
                        SocketHelper.killListener(kill);
                    }
                    if (finalCo.getType().equals("VALVE")) {
                        SocketHelper.killValve(kill);
                    }
                    log("已删除内存马: " + kill);
                } catch (Exception ex) {
                    log("无法删除内存马");
                }
            }).start();
        });
        refreshButton.addActionListener(e -> {
            String pid = pidText.getText();
            String pass = passText.getText();
            if (pass.length() != 8) {
                JOptionPane.showMessageDialog(shellPanel, "请输入长度为8的密码");
                return;
            }
            SocketHelper.setPass(pass);
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                Path agentPath = Paths.get("lib").resolve(
                        Paths.get("agent.jar"));
                Path agentDepPath = Paths.get("lib").resolve(
                        Paths.get("agent-jar-with-dependencies.jar"));

                String path;
                if (Files.exists(agentPath)) {
                    path = agentPath.toAbsolutePath().toString();
                } else if (Files.exists(agentDepPath)) {
                    path = agentDepPath.toAbsolutePath().toString();
                } else {
                    log("请检查当前目录的agent文件");
                    return;
                }
                vm.loadAgent(path, pass);
                vm.detach();
                new Thread(this::analyze).start();
                log("已刷新");
            } catch (Exception ignored) {
                new Thread(this::analyze).start();
            }
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
                        total = "// FernFlower \n" + total;
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
        // check windows
        // 目前该功能仅给 Windows 使用
        if (!OSUtil.isWindows() || !Version.isJava8()) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "only support jdk8/windows<br>" +
                            "目前只支持 jdk8/windows 系统<br>" +
                            "更多信息参考原始项目地址：<br>" +
                            "https://github.com/4ra1n/shell-analyzer" +
                            "</html>");
            return;
        }

        // 检查端口 10033 端口是否被占用
        if (SocketUtil.isPortInUse("localhost", 10033)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "10033 port in use<br>" +
                            "10033 端口被占用<br>" +
                            "该功能需要使用该端口" +
                            "</html>");
            return;
        }

        JFrame frame = new JFrame("tomcat-analyzer by 4ra1n");
        instance = new ShellForm();
        frame.setContentPane(instance.shellPanel);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.pack();

        frame.setResizable(false);
        frame.setSize(1300, 800);

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
        topPanel.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.setEnabled(false);
        rootPanel.add(topPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        topPanel.setBorder(BorderFactory.createTitledBorder(null, "启动", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        runningButton = new JButton();
        runningButton.setText("检测当前运行的Java进程");
        topPanel.add(runningButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processScroll = new JScrollPane();
        processScroll.setBackground(new Color(-12895429));
        processScroll.setHorizontalScrollBarPolicy(30);
        processScroll.setVerticalScrollBarPolicy(20);
        topPanel.add(processScroll, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        processTable = new JTable();
        processTable.setAutoCreateRowSorter(false);
        processTable.setFillsViewportHeight(false);
        processScroll.setViewportView(processTable);
        pidText = new JTextField();
        topPanel.add(pidText, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        attachButton = new JButton();
        attachButton.setText("开始Attach");
        topPanel.add(attachButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pidLabel = new JLabel();
        pidLabel.setText("PID");
        topPanel.add(pidLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        topPanel.add(panel1, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        passLabel = new JLabel();
        passLabel.setText(" 长度为8的Token");
        panel1.add(passLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passText = new JTextField();
        panel1.add(passText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        genButton = new JButton();
        genButton.setText("自动生成");
        panel1.add(genButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        normalPanel = new JPanel();
        normalPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(normalPanel, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(450, -1), null, new Dimension(450, -1), 0, false));
        normalPanel.setBorder(BorderFactory.createTitledBorder(null, "组件", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
        codePanel.setBorder(BorderFactory.createTitledBorder(null, "反编译代码", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        logPanel = new JPanel();
        logPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(logPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        logPanel.setBorder(BorderFactory.createTitledBorder(null, "日志", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
        confPane.setBorder(BorderFactory.createTitledBorder(null, "配置", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
        analyzePanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        confPanel.add(analyzePanel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        killButton = new JButton();
        killButton.setText("删除内存马");
        analyzePanel.add(killButton, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        killText = new JTextField();
        analyzePanel.add(killText, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        refreshButton = new JButton();
        refreshButton.setText("刷新");
        analyzePanel.add(refreshButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
