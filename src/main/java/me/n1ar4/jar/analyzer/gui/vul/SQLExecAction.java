package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class SQLExecAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getSqlExecButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // java/sql/PreparedStatement execute
            SearchCondition ar = new SearchCondition();
            ar.setClassName("java/sql/PreparedStatement");
            ar.setMethodName("execute");
            conditions.add(ar);

            // java/sql/PreparedStatement executeQuery
            SearchCondition qr = new SearchCondition();
            qr.setClassName("java/sql/PreparedStatement");
            qr.setMethodName("executeQuery");
            conditions.add(qr);

            // java/sql/PreparedStatement executeUpdate
            SearchCondition ur = new SearchCondition();
            ur.setClassName("java/sql/PreparedStatement");
            ur.setMethodName("executeUpdate");
            conditions.add(ur);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}
