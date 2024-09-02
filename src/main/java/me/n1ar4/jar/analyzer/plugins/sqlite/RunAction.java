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
