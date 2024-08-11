package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.CoreHelper;
import me.n1ar4.jar.analyzer.engine.SearchCondition;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.util.ArrayList;
import java.util.List;

public class JEXLAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();

        instance.getJEXLButton().addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                return;
            }

            List<SearchCondition> conditions = new ArrayList<>();

            // org/apache/commons/jexl3/JexlExpression.evaluate
            SearchCondition ar = new SearchCondition();
            ar.setClassName("org/apache/commons/jexl3/JexlExpression");
            ar.setMethodName("evaluate");
            conditions.add(ar);

            new Thread(() -> CoreHelper.refreshCallSearchList(conditions)).start();
        });
    }
}
