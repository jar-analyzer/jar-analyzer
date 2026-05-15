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
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // Stronger, saturated colors used exclusively on the scroll bar's diff
    // strip. The line backgrounds (C_*_LINE above) are intentionally pale so
    // they don't fight with syntax highlighting; on the narrow scroll-bar
    // strip those pastel colors disappear, so we use these "marker" tones
    // and add a thin border for extra contrast.
    private static final Color C_ADD_MARK = new Color(0x2E, 0xA0, 0x43);   // green
    private static final Color C_DEL_MARK = new Color(0xCF, 0x22, 0x2E);   // red
    private static final Color C_INFO_MARK = new Color(0x21, 0x6E, 0xDB);  // blue
    private static final Color C_PAD_MARK = new Color(0xB0, 0xB0, 0xB0);   // gray

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

    // Per-pane diff line markers shown on the vertical scroll bar's track
    // (similar to IDEA / VSCode mini-map). Updated each time a side-by-side
    // diff is rendered, then the corresponding scroll bar is repainted.
    private final DiffMarkScrollBarUI leftMarksUI = new DiffMarkScrollBarUI();
    private final DiffMarkScrollBarUI rightMarksUI = new DiffMarkScrollBarUI();
    private final DiffMarkScrollBarUI unifiedMarksUI = new DiffMarkScrollBarUI();

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
        setStatus(StatusLevel.IDLE, "idle");
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
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
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Inputs"),
                BorderFactory.createEmptyBorder(2, 6, 4, 6)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 4, 3, 4);

        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 0;
        p.add(makeFieldLabel("LEFT"), g);
        g.gridx = 1;
        g.weightx = 1;
        leftPath.setToolTipText("path to a jar / war file or a directory containing them");
        p.add(leftPath, g);
        g.gridx = 2;
        g.weightx = 0;
        JButton leftBrowse = new JButton("Browse");
        leftBrowse.setMnemonic('L');
        leftBrowse.addActionListener(e -> browse(leftPath));
        p.add(leftBrowse, g);

        g.gridy = 1;
        g.gridx = 0;
        g.weightx = 0;
        p.add(makeFieldLabel("RIGHT"), g);
        g.gridx = 1;
        g.weightx = 1;
        rightPath.setToolTipText("path to a jar / war file or a directory containing them");
        p.add(rightPath, g);
        g.gridx = 2;
        g.weightx = 0;
        JButton rightBrowse = new JButton("Browse");
        rightBrowse.setMnemonic('R');
        rightBrowse.addActionListener(e -> browse(rightPath));
        p.add(rightBrowse, g);

        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 3;
        g.weightx = 1;
        g.insets = new Insets(8, 4, 2, 4);
        p.add(buildControlBar(), g);

        hideEqual.addActionListener(e -> {
            applyRowFilter();
            rebuildTreeView();
        });
        return p;
    }

    private static JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        l.setPreferredSize(new Dimension(56, l.getPreferredSize().height));
        return l;
    }

    private JComponent buildControlBar() {
        JPanel ctrl = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 8);
        g.gridy = 0;

        runBtn.setMnemonic('D');
        runBtn.addActionListener(this::onRun);
        leftPath.addActionListener(this::onRun);
        rightPath.addActionListener(this::onRun);
        g.gridx = 0;
        g.weightx = 0;
        ctrl.add(runBtn, g);

        g.gridx = 1;
        ctrl.add(hideEqual, g);

        // separator that takes the slack so status / progress sit at the right
        g.gridx = 2;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        ctrl.add(Box.createHorizontalGlue(), g);

        // status: keeps a stable footprint and ellipsises long text instead
        // of pushing the progress bar off screen.
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        statusLabel.setForeground(C_EQ_FG);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setMinimumSize(new Dimension(120, statusLabel.getPreferredSize().height));
        statusLabel.setPreferredSize(new Dimension(360, statusLabel.getPreferredSize().height));
        g.gridx = 3;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        ctrl.add(statusLabel, g);

        progressBar.setIndeterminate(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(220, 16));
        progressBar.setVisible(false);
        g.gridx = 4;
        g.insets = new Insets(0, 0, 0, 0);
        ctrl.add(progressBar, g);

        return ctrl;
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
        installDiffMarks(unifiedScroll, unifiedPane, unifiedMarksUI);
        tabs.addTab("Unified Diff", unifiedScroll);

        configureEditor(leftPane, SyntaxConstants.SYNTAX_STYLE_JAVA);
        configureEditor(rightPane, SyntaxConstants.SYNTAX_STYLE_JAVA);

        installDiffMarks(leftScroll, leftPane, leftMarksUI);
        installDiffMarks(rightScroll, rightPane, rightMarksUI);

        rightScroll.getVerticalScrollBar().setModel(
                leftScroll.getVerticalScrollBar().getModel());
        rightScroll.getHorizontalScrollBar().setModel(
                leftScroll.getHorizontalScrollBar().getModel());

        JSplitPane side = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrap("LEFT", leftScroll, C_DEL_LINE, C_DEL_FG),
                wrap("RIGHT", rightScroll, C_ADD_LINE, C_ADD_FG));
        side.setResizeWeight(0.5);
        tabs.addTab("Side by Side", side);
        return tabs;
    }

    /**
     * Installs an IDEA / VSCode style diff marker UI on the vertical scroll
     * bar of the given scroll pane. The markers are tinted strips painted on
     * the scroll bar's track to indicate where added / removed / padding
     * lines are; clicking the strip jumps to that line.
     */
    private static void installDiffMarks(RTextScrollPane scroll,
                                         RSyntaxTextArea area,
                                         DiffMarkScrollBarUI ui) {
        JScrollBar vbar = scroll.getVerticalScrollBar();
        ui.bind(area);
        vbar.setUI(ui);
        // Make the bar a touch wider so the marker strip is visible without
        // visually clobbering the thumb.
        Dimension pref = vbar.getPreferredSize();
        vbar.setPreferredSize(new Dimension(Math.max(pref.width, 14),
                pref.height));
        vbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int line = ui.lineAt(e.getY(), vbar.getHeight());
                if (line < 0) {
                    return;
                }
                try {
                    int total = area.getLineCount();
                    if (line >= total) {
                        line = total - 1;
                    }
                    int offset = area.getLineStartOffset(line);
                    Rectangle r = area.modelToView(offset);
                    if (r != null) {
                        // Center the target line in the viewport.
                        Rectangle vis = area.getVisibleRect();
                        r.y = Math.max(0, r.y - vis.height / 2);
                        r.height = vis.height;
                        area.scrollRectToVisible(r);
                        area.setCaretPosition(offset);
                    }
                } catch (Exception ignored) {
                }
            }
        });
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

    private static JComponent wrap(String title, RTextScrollPane scroll,
                                   Color bg, Color fg) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(bg);
        header.setForeground(fg);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, fg.brighter()),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
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
        setStatus(StatusLevel.RUNNING, "running...");
        tableModel.setRowCount(0);
        treeRoot.removeAllChildren();
        treeRoot.setUserObject("(running...)");
        treeModel.reload();
        unifiedPane.setText("");
        leftPane.setText("");
        rightPane.setText("");
        leftMarksUI.clear();
        rightMarksUI.clear();
        unifiedMarksUI.clear();
        repaintMarks();
        unifiedScroll.getVerticalScrollBar().repaint();

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
                    setStatus(StatusLevel.DONE, "done. total: " + entries.size()
                            + (job.isDirectoryMode() ? "  (directory mode)" : ""));
                } catch (Exception ex) {
                    logger.error("jar diff failed", ex);
                    setStatus(StatusLevel.ERROR, "error: " + ex.getMessage());
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
        setStatus(StatusLevel.RUNNING, "loading: " + entry.getDisplayPath());
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
                    setStatus(StatusLevel.DONE, "loaded: " + entry.getDisplayPath());
                } catch (Exception ex) {
                    logger.error("load entry failed", ex);
                    setStatus(StatusLevel.ERROR, "error: " + ex.getMessage());
                }
            }
        }.execute();
    }


    private void renderUnified(JarDiffEntry entry, String left, String right) {
        unifiedPane.removeAllLineHighlights();
        unifiedMarksUI.clear();
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
        // Refresh the scroll bar markers (skip the header tints at indices 0..2).
        List<int[]> bodyTints = new ArrayList<>(tints.size());
        for (int[] t : tints) {
            if (t[0] >= 3) {
                bodyTints.add(t);
            }
        }
        unifiedMarksUI.setMarkers(toMarkers(bodyTints, /*unified=*/true));
        unifiedScroll.getVerticalScrollBar().repaint();
    }


    private void renderSideBySide(JarDiffEntry entry, String left, String right) {
        leftPane.removeAllLineHighlights();
        rightPane.removeAllLineHighlights();
        leftPane.setText("");
        rightPane.setText("");
        leftMarksUI.clear();
        rightMarksUI.clear();

        JarDiffEntry.Status st = entry.getStatus();
        if (st == JarDiffEntry.Status.EQUAL || st == JarDiffEntry.Status.EQUAL_BYTES_DIFFER) {
            leftPane.setText(left);
            rightPane.setText(right);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            repaintMarks();
            return;
        }
        if (st == JarDiffEntry.Status.ADDED) {
            leftPane.setText(allPadding(right));
            rightPane.setText(right);
            tintAll(leftPane, C_PAD_LINE);
            tintAll(rightPane, C_ADD_LINE);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            int lines = rightPane.getLineCount();
            leftMarksUI.setMarkersAllLines(lines, C_PAD_MARK);
            rightMarksUI.setMarkersAllLines(lines, C_ADD_MARK);
            repaintMarks();
            return;
        }
        if (st == JarDiffEntry.Status.REMOVED) {
            leftPane.setText(left);
            rightPane.setText(allPadding(left));
            tintAll(leftPane, C_DEL_LINE);
            tintAll(rightPane, C_PAD_LINE);
            leftPane.setCaretPosition(0);
            rightPane.setCaretPosition(0);
            int lines = leftPane.getLineCount();
            leftMarksUI.setMarkersAllLines(lines, C_DEL_MARK);
            rightMarksUI.setMarkersAllLines(lines, C_PAD_MARK);
            repaintMarks();
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

        leftMarksUI.setMarkers(toMarkers(lTints, /*unified=*/false));
        rightMarksUI.setMarkers(toMarkers(rTints, /*unified=*/false));
        repaintMarks();
    }

    private void repaintMarks() {
        leftScroll.getVerticalScrollBar().repaint();
        rightScroll.getVerticalScrollBar().repaint();
    }

    /**
     * Converts the [line, kind] tints into markers consumable by
     * DiffMarkScrollBarUI. The kind encoding differs slightly between
     * unified and side-by-side renders, so we map both into a uniform set
     * of colors here.
     * <p>
     * Side-by-side encoding: 0 = DEL, 1 = ADD, 2 = PAD
     * Unified encoding:      0 = ADD, 1 = DEL, other = info
     */
    private static List<DiffMarkScrollBarUI.Marker> toMarkers(List<int[]> tints,
                                                              boolean unified) {
        List<DiffMarkScrollBarUI.Marker> out = new ArrayList<>(tints.size());
        for (int[] t : tints) {
            Color c;
            if (unified) {
                switch (t[1]) {
                    case 0:
                        c = C_ADD_MARK;
                        break;
                    case 1:
                        c = C_DEL_MARK;
                        break;
                    default:
                        c = C_INFO_MARK;
                        break;
                }
            } else {
                switch (t[1]) {
                    case 0:
                        c = C_DEL_MARK;
                        break;
                    case 1:
                        c = C_ADD_MARK;
                        break;
                    default:
                        c = C_PAD_MARK;
                        break;
                }
            }
            out.add(new DiffMarkScrollBarUI.Marker(t[0], c));
        }
        return out;
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


    private enum StatusLevel {IDLE, RUNNING, DONE, ERROR}

    private void setStatus(StatusLevel level, String text) {
        statusLabel.setText(text);
        statusLabel.setToolTipText(text);
        Color c;
        switch (level) {
            case RUNNING:
                c = C_HEAD_FG;
                break;
            case DONE:
                c = C_ADD_FG;
                break;
            case ERROR:
                c = C_DEL_FG;
                break;
            case IDLE:
            default:
                c = C_EQ_FG;
                break;
        }
        statusLabel.setForeground(c);
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

    /**
     * Custom scroll bar UI that paints diff line markers on the track,
     * IDEA / VSCode style. Each marker maps a line index in the bound
     * text area to a colored strip drawn at the corresponding vertical
     * position of the track.
     * <p>
     * The thumb / arrow buttons keep the platform Basic look so this stays
     * lightweight and behaves predictably across Substance / FlatLaf / etc.
     */
    private static class DiffMarkScrollBarUI extends BasicScrollBarUI {

        static class Marker {
            final int line;
            final Color color;

            Marker(int line, Color color) {
                this.line = line;
                this.color = color;
            }
        }

        private RSyntaxTextArea area;
        private List<Marker> markers = Collections.emptyList();

        void bind(RSyntaxTextArea a) {
            this.area = a;
        }

        void setMarkers(List<Marker> m) {
            this.markers = (m == null) ? Collections.<Marker>emptyList() : m;
        }

        void setMarkersAllLines(int totalLines, Color c) {
            List<Marker> list = new ArrayList<>(totalLines);
            for (int i = 0; i < totalLines; i++) {
                list.add(new Marker(i, c));
            }
            this.markers = list;
        }

        void clear() {
            this.markers = Collections.emptyList();
        }

        /**
         * Maps a Y pixel within the scroll bar to a logical line index.
         * Returns -1 when no text area is bound or the bar has no usable
         * track height.
         */
        int lineAt(int y, int barHeight) {
            if (area == null) {
                return -1;
            }
            int total = area.getLineCount();
            if (total <= 0 || barHeight <= 0) {
                return -1;
            }
            int line = (int) ((long) y * total / Math.max(1, barHeight));
            if (line < 0) {
                return 0;
            }
            if (line >= total) {
                return total - 1;
            }
            return line;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            super.paintTrack(g, c, trackBounds);
            if (markers.isEmpty() || area == null) {
                return;
            }
            int total = Math.max(1, area.getLineCount());
            int h = trackBounds.height;
            if (h <= 0) {
                return;
            }
            // Strip on the right edge of the track, leaving 2px gutters so
            // it stays out of the thumb's way.
            int stripW = Math.max(3, trackBounds.width - 6);
            int stripX = trackBounds.x + (trackBounds.width - stripW) / 2;
            // Clamp marker height to at least 3 px so single-line diffs are
            // visible and noticeable even on very long files.
            int markerH = Math.max(3, h / total);

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                Color border = new Color(0, 0, 0, 60);
                for (Marker m : markers) {
                    int y = trackBounds.y + (int) ((long) m.line * h / total);
                    g2.setColor(m.color);
                    g2.fillRect(stripX, y, stripW, markerH);
                    // Thin per-marker outline boosts contrast against the
                    // scroll-bar track on light themes.
                    if (markerH >= 3 && stripW >= 3) {
                        g2.setColor(border);
                        g2.drawRect(stripX, y, stripW - 1, markerH - 1);
                    }
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
