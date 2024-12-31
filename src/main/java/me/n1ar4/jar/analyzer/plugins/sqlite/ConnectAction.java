/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
