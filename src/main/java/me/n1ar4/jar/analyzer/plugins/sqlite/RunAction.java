/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.plugins.sqlite;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

public class RunAction {
    public static void register() {
        SQLiteForm.getInstance().getRunButton().addActionListener(e -> {
            SQLiteForm.getInstance().getErrArea().setText(null);
            if (SQLiteForm.getHelper() == null) {
                JOptionPane.showMessageDialog(SQLiteForm.getInstance().getMasterPanel(),
                        "please connect first");
                return;
            }
            JTextArea area = SQLiteForm.getSqlArea();
            if (area == null || area.getText() == null || area.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(SQLiteForm.getInstance().getMasterPanel(),
                        "please input sql first");
                return;
            }
            DefaultTableModel model = new DefaultTableModel();
            SQLiteHelper helper = SQLiteForm.getHelper();
            helper.connect();
            try (ResultSet rs = helper.executeQuery(area.getText())) {
                ResultSetMetaData metaData = rs.getMetaData();
                Vector<String> columnNames = new Vector<>();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(metaData.getColumnName(i));
                }
                Vector<Vector<Object>> data = new Vector<>();
                while (rs.next()) {
                    Vector<Object> vector = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) {
                        vector.add(rs.getObject(i));
                    }
                    data.add(vector);
                }
                model.setDataVector(data, columnNames);
            } catch (Exception ex) {
                SQLiteForm.getInstance().getErrArea().setText(ex.getMessage());
            } finally {
                helper.close();
            }
            SQLiteForm.getInstance().getResultTable().setModel(model);
        });
    }
}
