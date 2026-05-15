/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https:
 */

package me.n1ar4.jar.analyzer.gui.diff;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;

public class JarDiffForm {
    private static final Logger logger = LogManager.getLogger();

    private static final Color C_ADD_LINE = new Color(0xE6, 0xFF, 0xEC);
    private static final Color C_ADD_FG = new Color(0x03, 0x6A, 0x10);
    private static final Color C_DEL_LINE = new Color(0xFF, 0xEB, 0xE9);
    private static final Color C_DEL_FG = new Color(0x9E, 0x1B, 0x1B);
    private static final Color C_PAD_LINE = new Color(0xF6, 0xF6, 0xF6);
    private static final Color C_HEAD_FG = new Color(0x1F, 0x4F, 0x9E);
    private static final Color C_MOD_FG = new Color(0xC9, 0x77, 0x06);
    private static final Color C_BYTES_FG = new Color(0x55, 0x80, 0x99);
    private static final Color C_EQ_FG = new Color(0x55, 0x55, 0x55);

    private final JFrame frame = new JFrame(Const.JarDiffForm);
    private final JTextField leftPath = new JTextField();
    private final JTextField rightPath = new JTextField();
    private final JButton runBtn = new JButton("Run Diff");
    private final JCheckBox hideEqual = new JCheckBox("Hide unchanged", true);
    private final JLabel statusLabel = new JLabel("idle");
    private final JProgressBar progressBar = new JProgressBar();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Entry", "Kind", "Status", "Size +/-"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);

    private final DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("(no diff)");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    private final JTree tree = new JTree(treeModel);

    private final RSyntaxTextArea unifiedPane = new RSyntaxTextArea();
    private final RSyntaxTextArea leftPane = new RSyntaxTextArea();
    private final RSyntaxTextArea rightPane = new RSyntaxTextArea();
    private final RTextScrollPane unifiedScroll = new RTextScrollPane(unifiedPane);
    private final RTextScrollPane leftScroll = new RTextScrollPane(leftPane);
    private final RTextScrollPane rightScroll = new RTextScrollPane(rightPane);

    private List<JarDiffEntry> entries;
    private DiffJob job;

    public static void start() {
        JarDiffForm form = new JarDiffForm();
        form.show0();
    }

    private JarDiffForm() {
    }

    private void show0() {
        frame.setContentPane(buildContent());
        applyGlobalFont(frame);
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Walks the component tree and resizes every component's font to the
     * current global FONT_SIZE while keeping each component's own font
     * family. This avoids glyph-coverage issues on macOS where forcing a
     * specific family can fall back to a font that does not include CJK
     * or other extended characters.
     */
    private static void applyGlobalFont(Component root) {
        if (root == null) {
            return;
        }
        Font f = root.getFont();
        if (f != null) {
            root.setFont(f.deriveFont(MainForm.FONT_SIZE));
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                applyGlobalFont(child);
            }
            // JTree / JTable have nested rendering components handled by
            // their UI delegates; row height should follow font as well.
            if (root instanceof JTable) {
                JTable t = (JTable) root;
                t.setRowHeight(Math.max(20, (int) (MainForm.FONT_SIZE * 1.5f)));
            }
        }
    }


    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(buildTopPanel(), BorderLayout.NORTH);

        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildNavigatorPanel(), buildDiffPanel());
        center.setResizeWeight(0.32);
        center.setDividerLocation(360);
        root.add(center, BorderLayout.CENTER);

        return root;
    }

    private JComponent buildTopPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(2, 4, 2, 4);

        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 0;
        p.add(new JLabel("LEFT  (jar / dir):"), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(leftPath, g);
        g.gridx = 2;
        g.weightx = 0;
        JButton leftBrowse = new JButton("Browse");
        leftBrowse.addActionListener(e -> browse(leftPath));
        p.add(leftBrowse, g);

        g.gridy = 1;
        g.gridx = 0;
        g.weightx = 0;
        p.add(new JLabel("RIGHT (jar / dir):"), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(rightPath, g);
        g.gridx = 2;
        g.weightx = 0;
        JButton rightBrowse = new JButton("Browse");
        rightBrowse.addActionListener(e -> browse(rightPath));
        p.add(rightBrowse, g);

        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 3;
        g.weightx = 1;
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        runBtn.addActionListener(this::onRun);
        ctrl.add(runBtn);
        ctrl.add(hideEqual);
        ctrl.add(Box.createHorizontalStrut(10));
        ctrl.add(new JLabel("status:"));
        ctrl.add(statusLabel);
        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(220, 16));
        progressBar.setVisible(false);
        ctrl.add(progressBar);
        p.add(ctrl, g);

        hideEqual.addActionListener(e -> {
            applyRowFilter();
            rebuildTreeView();
        });
        return p;
    }

    private JComponent buildNavigatorPanel() {
        JTabbedPane tabs = new JTabbedPane();

        tree.setRootVisible(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new EntryTreeCellRenderer());
        tree.addTreeSelectionListener(e -> {
            TreePath p = e.getNewLeadSelectionPath();
            if (p == null) {
                return;
            }
            Object node = p.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode) {
                Object userObj = ((DefaultMutableTreeNode) node).getUserObject();
                if (userObj instanceof EntryNode) {
                    onEntrySelected(((EntryNode) userObj).entry);
                }
            }
        });
        tabs.addTab("Tree", new JScrollPane(tree));

        table.setAutoCreateRowSorter(false);
        table.setRowSorter(sorter);
        table.setRowHeight(20);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(560);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0 || entries == null) {
                    return;
                }
                int modelRow = table.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < entries.size()) {
                    onEntrySelected(entries.get(modelRow));
                }
            }
        });
        tabs.addTab("Table", new JScrollPane(table));

        return tabs;
    }

    private JComponent buildDiffPanel() {
        JTabbedPane tabs = new JTabbedPane();

        configureEditor(unifiedPane, SyntaxConstants.SYNTAX_STYLE_NONE);
        tabs.addTab("Unified Diff", unifiedScroll);

        configureEditor(leftPane, SyntaxConstants.SYNTAX_STYLE_JAVA);
        configureEditor(rightPane, SyntaxConstants.SYNTAX_STYLE_JAVA);

        rightScroll.getVerticalScrollBar().setModel(
                leftScroll.getVerticalScrollBar().getModel());
        rightScroll.getHorizontalScrollBar().setModel(
                leftScroll.getHorizontalScrollBar().getModel());

        JSplitPane side = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrap("LEFT", leftScroll), wrap("RIGHT", rightScroll));
        side.setResizeWeight(0.5);
        tabs.addTab("Side by Side", side);
        return tabs;
    }

    private static void configureEditor(RSyntaxTextArea area, String style) {
        area.setEditable(false);
        area.setSyntaxEditingStyle(style);
        area.setCodeFoldingEnabled(false);
        area.setHighlightCurrentLine(false);
        // Keep the editor's native (monospaced) family, only override the
        // size so it tracks the user's global preference (MainForm.FONT_SIZE).
        // Forcing a specific family caused glyphs to fall back to a font
        // that does not cover certain characters on macOS.
        area.setFont(area.getFont().deriveFont(MainForm.FONT_SIZE));
    }

    private static JComponent wrap(String title, RTextScrollPane scroll) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel header = new JLabel("  " + title);
        header.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        p.add(header, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }


    private void browse(JTextField target) {
        JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f != null) {
                target.setText(f.getAbsolutePath());
            }
        }
    }

    private void onRun(ActionEvent ev) {
        final String l = leftPath.getText().trim();
        final String r = rightPath.getText().trim();
        if (l.isEmpty() || r.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "please select both inputs first", "Jar Diff",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Path leftRoot = Paths.get(l);
        Path rightRoot = Paths.get(r);
        if (!Files.exists(leftRoot) || !Files.exists(rightRoot)) {
            JOptionPane.showMessageDialog(frame,
                    "one of the selected paths does not exist",
                    "Jar Diff", JOptionPane.WARNING_MESSAGE);
            return;
        }

        runBtn.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString("0%");
        statusLabel.setText("running...");
        tableModel.setRowCount(0);
        treeRoot.removeAllChildren();
        treeRoot.setUserObject("(running...)");
        treeModel.reload();
        unifiedPane.setText("");
        leftPane.setText("");
        rightPane.setText("");

        new SwingWorker<List<JarDiffEntry>, int[]>() {
            private volatile int total = 0;

            @Override
            protected List<JarDiffEntry> doInBackground() throws Exception {
                Path workDir = Paths.get(Const.tempDir, "diff");
                cleanWorkDir(workDir);
                Files.createDirectories(workDir);
                job = new DiffJob(leftRoot, rightRoot, workDir);
                job.setProgressListener(new JarDiffer.ProgressListener() {
                    @Override
                    public void onTotal(int t) {
                        total = t;
                        publish(new int[]{0, t});
                    }

                    @Override
                    public void onAdvance(int processed, String currentPath) {
                        publish(new int[]{processed, total});
                    }
                });
                job.run();
                return job.getEntries();
            }

            @Override
            protected void process(List<int[]> chunks) {
                if (chunks.isEmpty()) {
                    return;
                }
                int[] last = chunks.get(chunks.size() - 1);
                int processed = last[0];
                int t = last[1];
                int pct = (t > 0) ? (int) Math.min(100L, processed * 100L / t) : 0;
                progressBar.setValue(pct);
                progressBar.setString(processed + " / " + t + " (" + pct + "%)");
            }

            @Override
            protected void done() {
                runBtn.setEnabled(true);
                progressBar.setVisible(false);
                progressBar.setValue(0);
                progressBar.setString(null);
                try {
                    entries = get();
                    populateTable(entries);
                    rebuildTreeView();
                    statusLabel.setText("done. total: " + entries.size()
                            + (job.isDirectoryMode() ? "  (directory mode)" : ""));
                } catch (Exception ex) {
                    logger.error("jar diff failed", ex);
                    statusLabel.setText("error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(frame,
                            "diff failed: " + ex.getMessage(),
                            "Jar Diff", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


    private void populateTable(List<JarDiffEntry> es) {
        tableModel.setRowCount(0);
        for (JarDiffEntry e : es) {
            tableModel.addRow(new Object[]{
                    e.getDisplayPath(),
                    e.getKind().name().toLowerCase(),
                    e.getStatus().name(),
                    e.getSizeDelta()
            });
        }
        applyRowFilter();
    }

    private void applyRowFilter() {
        if (hideEqual.isSelected()) {
            sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                    Object v = e.getValue(2);
                    if (v == null) {
                        return false;
                    }
                    String s = v.toString();
                    return !"EQUAL".equals(s) && !"EQUAL_BYTES_DIFFER".equals(s);
                }
            });
        } else {
            sorter.setRowFilter(null);
        }
    }

    private static class EntryNode {
        final JarDiffEntry entry;
        final String label;

        EntryNode(JarDiffEntry e, String label) {
            this.entry = e;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void rebuildTreeView() {
        if (entries == null) {
            return;
        }
        treeRoot.setUserObject(job != null && job.isDirectoryMode()
                ? "diff (directory mode)" : "diff");
        treeRoot.removeAllChildren();


        Map<String, List<JarDiffEntry>> byJar = new LinkedHashMap<>();
        for (JarDiffEntry e : entries) {
            if (hideEqual.isSelected()
                    && (e.getStatus() == JarDiffEntry.Status.EQUAL
                    || e.getStatus() == JarDiffEntry.Status.EQUAL_BYTES_DIFFER)) {
                continue;
            }
            String key = e.getJarRelative() == null ? "" : e.getJarRelative();
            byJar.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        for (Map.Entry<String, List<JarDiffEntry>> bucket : byJar.entrySet()) {
            DefaultMutableTreeNode parent;
            if (bucket.getKey().isEmpty()) {
                parent = treeRoot;
            } else {
                parent = new DefaultMutableTreeNode(bucket.getKey());
                treeRoot.add(parent);
            }
            insertEntriesAsTree(parent, bucket.getValue());
        }
        treeModel.reload();

        expandToDepth(treeRoot, 2);
    }

    private void insertEntriesAsTree(DefaultMutableTreeNode parent,
                                     List<JarDiffEntry> es) {

        Map<String, DefaultMutableTreeNode> dirNodes = new HashMap<>();
        dirNodes.put("", parent);
        for (JarDiffEntry e : es) {
            String p = e.getEntryPath();
            String[] parts = p.split("/");
            StringBuilder dirKey = new StringBuilder();
            DefaultMutableTreeNode current = parent;
            for (int i = 0; i < parts.length - 1; i++) {
                if (dirKey.length() > 0) {
                    dirKey.append('/');
                }
                dirKey.append(parts[i]);
                String key = dirKey.toString();
                DefaultMutableTreeNode next = dirNodes.get(key);
                if (next == null) {
                    next = new DefaultMutableTreeNode(parts[i] + "/");
                    current.add(next);
                    dirNodes.put(key, next);
                }
                current = next;
            }
            String leafLabel = parts[parts.length - 1] + "  ["
                    + e.getStatus() + "]";
            current.add(new DefaultMutableTreeNode(new EntryNode(e, leafLabel)));
        }
    }

    private void expandToDepth(DefaultMutableTreeNode node, int depth) {
        if (depth <= 0) {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        tree.expandPath(path);
        for (int i = 0; i < node.getChildCount(); i++) {
            Object c = node.getChildAt(i);
            if (c instanceof DefaultMutableTreeNode) {
                expandToDepth((DefaultMutableTreeNode) c, depth - 1);
            }
        }
    }


    private void onEntrySelected(JarDiffEntry entry) {
        if (entry == null || job == null) {
            return;
        }
        statusLabel.setText("loading: " + entry.getDisplayPath());
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws IOException {
                String l = job.loadDisplayText(entry, 'L');
                String r = job.loadDisplayText(entry, 'R');
                return new String[]{l, r};
            }

            @Override
            protected void done() {
                try {
                    String[] both = get();
                    renderSideBySide(entry, both[0], both[1]);
                    renderUnified(entry, both[0], both[1]);
                    statusLabel.setText("loaded: " + entry.getDisplayPath());
                } catch (Exception ex) {
                    logger.error("load entry failed", ex);
                    statusLabel.setText("error: " + ex.getMessage());
                }
            }
        }.execute();
    }


    private void renderUnified(JarDiffEntry entry, String left, String right) {
        unifiedPane.removeAllLineHighlights();
        StringBuilder sb = new StringBuilder();
        sb.append("--- LEFT : ").append(entry.getDisplayPath()).append('\n');
        sb.append("+++ RIGHT: ").append(entry.getDisplayPath())
                .append("  [").append(entry.getStatus()).append("]\n");
        sb.append("================================================================\n");

        List<int[]> tints = new ArrayList<>();

        tints.add(new int[]{0, 2});
        tints.add(new int[]{1, 2});
        tints.add(new int[]{2, 2});

        JarDiffEntry.Status st = entry.getStatus();
        if (st == JarDiffEntry.Status.EQUAL) {
            sb.append("(content identical)\n");
        } else if (st == JarDiffEntry.Status.EQUAL_BYTES_DIFFER) {
            sb.append("(bytes differ but decompiled source is identical -- "
                    + "likely timestamp / constant pool noise)\n");
        } else if (st == JarDiffEntry.Status.ADDED) {
            int line = 3;
            for (String s : right.split("\\r?\\n", -1)) {
                sb.append('+').append(s).append('\n');
                tints.add(new int[]{line++, 0});
            }
        } else if (st == JarDiffEntry.Status.REMOVED) {
            int line = 3;
            for (String s : left.split("\\r?\\n", -1)) {
                sb.append('-').append(s).append('\n');
                tints.add(new int[]{line++, 1});
            }
        } else {

            int line = 3;
            for (MyersDiff.Op op : MyersDiff.diff(left, right)) {
                switch (op.type) {
                    case INSERT:
                        sb.append('+').append(op.line).append('\n');
                        tints.add(new int[]{line, 0});
                        break;
                    case DELETE:
                        sb.append('-').append(op.line).append('\n');
                        tints.add(new int[]{line, 1});
                        break;
                    case EQUAL:
                    default:
                        sb.append(' ').append(op.line).append('\n');
                        break;
                }
                line++;
            }
        }
        unifiedPane.setText(sb.toString());
        unifiedPane.setCaretPosition(0);

        for (int[] t : tints) {
            try {
                Color c;
                switch (t[1]) {
                    case 0:
                        c = C_ADD_LINE;
                        break;
                    case 1:
                        c = C_DEL_LINE;
                        break;
                    default:
                        c = new Color(0xEC, 0xF1, 0xFA);
                        break;
                }
                unifiedPane.addLineHighlight(t[0], c);
            } catch (Exception ignored) {

            }
        }
    }


    private void renderSideBySide(JarDiffEntry entry, String left, String right) {
        leftPane.removeAllLineHighlights();
        rightPane.removeAllLineHighlights();
        leftPane.setText("");
        rightPane.setText("");

        JarDiffEntry.Status st = entry.getStatus();
        if (st == JarDiffEntry.Status.EQUAL || st == JarDiffEntry.Status.EQUAL_BYTES_DIFFER) {
            leftPane.setText(left);
            rightPane.setText(right);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            return;
        }
        if (st == JarDiffEntry.Status.ADDED) {
            leftPane.setText(allPadding(right));
            rightPane.setText(right);
            tintAll(leftPane, C_PAD_LINE);
            tintAll(rightPane, C_ADD_LINE);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            return;
        }
        if (st == JarDiffEntry.Status.REMOVED) {
            leftPane.setText(left);
            rightPane.setText(allPadding(left));
            tintAll(leftPane, C_DEL_LINE);
            tintAll(rightPane, C_PAD_LINE);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            return;
        }


        StringBuilder lb = new StringBuilder();
        StringBuilder rb = new StringBuilder();
        List<int[]> lTints = new ArrayList<>();
        List<int[]> rTints = new ArrayList<>();

        int lLine = 0, rLine = 0;
        for (MyersDiff.Op op : MyersDiff.diff(left, right)) {
            switch (op.type) {
                case EQUAL:
                    lb.append(op.line).append('\n');
                    rb.append(op.line).append('\n');
                    lLine++;
                    rLine++;
                    break;
                case DELETE:
                    lb.append(op.line).append('\n');
                    rb.append('\n');
                    lTints.add(new int[]{lLine, 0});
                    rTints.add(new int[]{rLine, 2});
                    lLine++;
                    rLine++;
                    break;
                case INSERT:
                    lb.append('\n');
                    rb.append(op.line).append('\n');
                    lTints.add(new int[]{lLine, 2});
                    rTints.add(new int[]{rLine, 1});
                    lLine++;
                    rLine++;
                    break;
                default:
                    break;
            }
        }
        leftPane.setText(lb.toString());
        rightPane.setText(rb.toString());
        leftPane.setCaretPosition(0);
        rightPane.setCaretPosition(0);
        applyTints(leftPane, lTints);
        applyTints(rightPane, rTints);
    }

    private static String allPadding(String basis) {

        int n = (basis == null || basis.isEmpty())
                ? 0 : basis.split("\\r?\\n", -1).length;
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append('\n');
        }
        return sb.toString();
    }

    private static void tintAll(RSyntaxTextArea area, Color color) {
        int lines = area.getLineCount();
        for (int i = 0; i < lines; i++) {
            try {
                area.addLineHighlight(i, color);
            } catch (Exception ignored) {
            }
        }
    }

    private static void applyTints(RSyntaxTextArea area, List<int[]> tints) {
        for (int[] t : tints) {
            Color c;
            switch (t[1]) {
                case 0:
                    c = C_DEL_LINE;
                    break;
                case 1:
                    c = C_ADD_LINE;
                    break;
                default:
                    c = C_PAD_LINE;
                    break;
            }
            try {
                area.addLineHighlight(t[0], c);
            } catch (Exception ignored) {
            }
        }
    }


    private static void cleanWorkDir(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc) {
                    try {
                        Files.deleteIfExists(d);
                    } catch (IOException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            logger.warn("clean diff work dir failed: {}", ex.toString());
        }
    }


    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            String s = value == null ? "" : value.toString();
            if (!isSelected) {
                c.setForeground(colorFor(s));
            }
            return c;
        }
    }

    private static Color colorFor(String status) {
        switch (status) {
            case "ADDED":
                return C_ADD_FG;
            case "REMOVED":
                return C_DEL_FG;
            case "MODIFIED":
                return C_MOD_FG;
            case "EQUAL_BYTES_DIFFER":
                return C_BYTES_FG;
            default:
                return C_EQ_FG;
        }
    }

    private static class EntryTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf,
                                                      int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            if (!sel && value instanceof DefaultMutableTreeNode) {
                Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObj instanceof EntryNode) {
                    JarDiffEntry e = ((EntryNode) userObj).entry;
                    setForeground(colorFor(e.getStatus().name()));
                }
            }
            return c;
        }
    }
}
