/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gadget;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.io.File;
import java.util.List;

public class GadgetUIBuilder {
    private static GadgetTableModel tableModel;

    public static GadgetTableModel getTableModel() {
        return tableModel;
    }

    public static void init(MainForm form) {
        form.getGadgetDescValueLabel().setText("<html>" +
                "仅根据依赖 <b><font color='#007ACC'>jar</font></b> 文件名称进行 " +
                "<b><font color='#007ACC'>gadget</font></b> 分析，" +
                "暂不会进行精确 <b><font color='#007ACC'>class</font></b> 信息分析" +
                "</html>");
        form.getGadgetChoseBtn().setIcon(SvgManager.DirIcon);
        form.getGadgetFastjsonBox().setSelected(true);
        form.getGadgetHessianBox().setSelected(true);
        form.getGadgetNativeBox().setSelected(true);
        form.getGadgetJdbcBox().setSelected(true);
        form.getGadgetInputText().setText("请选择一个依赖目录（对于 jar/war 可以解压缩得到）");

        JTable table = form.getGadgetResultTable();

        String[] columnNames = {"ID", "JAR 名称", "存在风险"};
        tableModel = new GadgetTableModel(null, columnNames);
        table.setModel(tableModel);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new GadgetTableCellRender());
        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn idColumn = columnModel.getColumn(0);
        TableColumn jarNameColumn = columnModel.getColumn(1);
        TableColumn resultColumn = columnModel.getColumn(2);
        idColumn.setMaxWidth(40);
        jarNameColumn.setPreferredWidth(350);
        resultColumn.setPreferredWidth(500);

        form.getGadgetChoseBtn().addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setCurrentDirectory(new File("."));
            int result = chooser.showOpenDialog(form.getMasterPanel());
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();
                form.getGadgetInputText().setText(selectedDir.getAbsolutePath());
                form.getGadgetInputText().setCaretPosition(0);
            } else {
                JOptionPane.showMessageDialog(form.getMasterPanel(), "你必须选择一个目录才可以开始");
            }
        });

        form.getGadgetStartBtn().addActionListener(e -> {
            GadgetAnalyzer analyzer = new GadgetAnalyzer(form.getGadgetInputText().getText());
            List<GadgetInfo> resultList = analyzer.process();
            if (resultList == null || resultList.isEmpty()) {
                JOptionPane.showMessageDialog(form.getMasterPanel(), "没有分析出结果");
                return;
            }
            tableModel.removeAllRows();
            for (GadgetInfo result : resultList) {
                Object[] data = new Object[3];
                data[0] = result.getID();
                StringBuilder sb = new StringBuilder();
                for (String s : result.getJarsName()) {
                    sb.append(s);
                    sb.append(" ");
                }
                data[1] = sb.toString().trim();
                data[2] = result.getResult();
                tableModel.addRow(data);
            }
        });

        GadgetRule.build();
    }
}
