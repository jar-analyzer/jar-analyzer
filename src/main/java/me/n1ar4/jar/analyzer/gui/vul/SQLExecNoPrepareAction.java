package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class SQLExecNoPrepareAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getSqlExecNoPrepareButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // java/sql/Statement execute
            SearchCondition ar = new SearchCondition();
            ar.setClassName("java/sql/Statement");
            ar.setMethodName("execute");
            conditions.add(ar);

            // java/sql/Statement executeQuery
            SearchCondition qr = new SearchCondition();
            qr.setClassName("java/sql/Statement");
            qr.setMethodName("executeQuery");
            conditions.add(qr);

            // java/sql/Statement executeUpdate
            SearchCondition ur = new SearchCondition();
            ur.setClassName("java/sql/Statement");
            ur.setMethodName("executeUpdate");
            conditions.add(ur);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}
