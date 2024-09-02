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

import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;

public class ConnectAction {
    public static void register() {
        SQLiteForm.getInstance().getConnectButton().addActionListener(e -> {
            Path dbPath = Paths.get(Const.dbFile);
            if (!Files.exists(dbPath)) {
                JOptionPane.showMessageDialog(SQLiteForm.getInstance().getMasterPanel(),
                        String.format("need %s file", Const.dbFile));
                return;
            }
            SQLiteForm.getInstance().getSqliteText().setText(dbPath.toAbsolutePath().toString());
            SQLiteForm.setHelper(new SQLiteHelper());
            SQLiteHelper helper = SQLiteForm.getHelper();
            helper.connect();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            String getAllTablesSQL = "SELECT tbl_name FROM sqlite_master";
            try (ResultSet rs = helper.executeQuery(getAllTablesSQL)) {
                while (rs.next()) {
                    String tblName = rs.getString("tbl_name");
                    model.addElement(tblName);
                }
            } catch (Exception ex) {
                SQLiteForm.getInstance().getErrArea().setText(ex.getMessage());
            } finally {
                helper.close();
            }
            SQLiteForm.getInstance().getTablesBox().setModel(model);
        });
    }
}
